package com.unab.registroqr.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unab.registroqr.R
import com.unab.registroqr.navigation.Screen
import com.unab.registroqr.viewmodel.QRViewModel
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.util.Log
import kotlinx.coroutines.delay

/**
 * Estados del widget de clase
 */
enum class ClassState {
    BEFORE_CLASS,    // 5 minutos antes
    DURING_CLASS     // Durante la clase (0-15 min)
}

/**
 * Información de una clase para mostrar en el widget
 */
data class ClassInfo(
    val name: String,
    val day: String,
    val time: String,
    val state: ClassState
)

/**
 * Pantalla del menú principal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    navController: NavController,
    viewModel: QRViewModel = viewModel()
) {
    // Recargar QRs cada vez que se muestra la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadQRs()
    }
    
    val qrList by viewModel.qrList.collectAsState()
    
    // Estado que se actualiza cada minuto para recalcular el widget
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    
    // Timer que actualiza currentTime cada 30 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000) // 30 segundos
            currentTime = LocalDateTime.now()
            Log.d("MainMenuScreen", "⏰ Timer: Actualizando widget - ${currentTime}")
        }
    }
    
    // Calcular próxima clase con 3 estados:
    // 1. 5 min antes: "Dentro de 5 minutos registra tu asistencia"
    // 2. Durante clase (0-15 min): "Ahora puedes registrar tu asistencia"
    // 3. Después de 15 min: null (widget vacío)
    
    val nextClass = remember(qrList, currentTime) {
        val now = currentTime
        val currentDay = now.dayOfWeek
        
        Log.d("MainMenuScreen", "=== Calculando próxima clase ===")
        Log.d("MainMenuScreen", "Hora actual: $now")
        
        // Expandir todas las notificaciones de todos los QRs
        val allClasses = qrList.flatMap { qr ->
            qr.notifications.filter { it.enabled }.map { notification ->
                Triple(qr.name, notification.dayOfWeek, notification.classTime)
            }
        }
        
        Log.d("MainMenuScreen", "Total clases activas: ${allClasses.size}")
        
        // Buscar la clase actual o próxima
        val candidates = allClasses.mapNotNull { (name, day, time) ->
            // Calcular la fecha/hora de la clase
            val classDateTime = if (day == currentDay) {
                val todayClass = now
                    .withHour(time.hour)
                    .withMinute(time.minute)
                    .withSecond(0)
                    .withNano(0)
                
                if (todayClass.plusMinutes(15).isAfter(now)) todayClass
                else todayClass.plusWeeks(1)
            } else {
                var nextOccurrence = now
                    .with(day)
                    .withHour(time.hour)
                    .withMinute(time.minute)
                    .withSecond(0)
                    .withNano(0)
                
                if (nextOccurrence.isBefore(now)) {
                    nextOccurrence = nextOccurrence.plusWeeks(1)
                }
                nextOccurrence
            }
            
            val minutesUntilClass = ChronoUnit.MINUTES.between(now, classDateTime)
            val minutesAfterClass = ChronoUnit.MINUTES.between(classDateTime, now)
            
            Log.d("MainMenuScreen", "$name - Minutos hasta clase: $minutesUntilClass")
            
            // Determinar el estado
            when {
                // Durante la clase (0-15 min después de empezar)
                minutesAfterClass in 0..15 -> {
                    Log.d("MainMenuScreen", "✓ DURANTE LA CLASE (${minutesAfterClass} min después)")
                    
                    val dayName = when (day) {
                        DayOfWeek.MONDAY -> "Lunes"
                        DayOfWeek.TUESDAY -> "Martes"
                        DayOfWeek.WEDNESDAY -> "Miércoles"
                        DayOfWeek.THURSDAY -> "Jueves"
                        DayOfWeek.FRIDAY -> "Viernes"
                        DayOfWeek.SATURDAY -> "Sábado"
                        DayOfWeek.SUNDAY -> "Domingo"
                    }
                    val timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                    
                    ClassInfo(name, dayName, timeStr, ClassState.DURING_CLASS) to minutesUntilClass
                }
                // 5 minutos antes
                minutesUntilClass in 0..5 -> {
                    Log.d("MainMenuScreen", "✓ 5 MIN ANTES (${minutesUntilClass} min)")
                    
                    val dayName = when (day) {
                        DayOfWeek.MONDAY -> "Lunes"
                        DayOfWeek.TUESDAY -> "Martes"
                        DayOfWeek.WEDNESDAY -> "Miércoles"
                        DayOfWeek.THURSDAY -> "Jueves"
                        DayOfWeek.FRIDAY -> "Viernes"
                        DayOfWeek.SATURDAY -> "Sábado"
                        DayOfWeek.SUNDAY -> "Domingo"
                    }
                    val timeStr = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                    
                    ClassInfo(name, dayName, timeStr, ClassState.BEFORE_CLASS) to minutesUntilClass
                }
                else -> {
                    Log.d("MainMenuScreen", "✗ Fuera de rango")
                    null
                }
            }
        }
        
        val result = candidates.minByOrNull { it.second }?.first
        
        if (result != null) {
            Log.d("MainMenuScreen", "✓✓✓ Clase seleccionada: ${result.name} - Estado: ${result.state}")
        } else {
            Log.d("MainMenuScreen", "✗✗✗ No hay clases en rango")
        }
        
        result
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Espacio superior
            Spacer(modifier = Modifier.height(16.dp))
            
            // Widget de próxima clase
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (nextClass != null) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (nextClass != null) {
                        Text(
                            text = "📚 Próxima clase",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nextClass.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${nextClass.day} - ${nextClass.time}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (nextClass.state) {
                                ClassState.BEFORE_CLASS -> "Dentro de 5 minutos registra tu asistencia"
                                ClassState.DURING_CLASS -> "Ahora puedes registrar tu asistencia"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        // Widget vacío cuando no hay clases en rango
                        Text(
                            text = "No hay clases próximas",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_qr),
                    contentDescription = "Logo Registro QR UNAB",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 60.dp)
                )
                
                // Botón Escanear QR
                Button(
                    onClick = { navController.navigate(Screen.ScanQR.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Escanear QR",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón Ingresar link manualmente
                OutlinedButton(
                    onClick = { navController.navigate(Screen.ManualEntry.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Ingresar link manualmente",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón QRs Guardados
                Button(
                    onClick = { navController.navigate(Screen.SavedQRs.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "QRs guardados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Textos de créditos en la parte inferior
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Idea por estudiantes de informática",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Facilitando el proceso de asistencias",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
