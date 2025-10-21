package com.unab.registroqr.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unab.registroqr.data.QRColor
import com.unab.registroqr.data.SaveQRResult
import com.unab.registroqr.data.SavedQR
import com.unab.registroqr.navigation.Screen
import com.unab.registroqr.viewmodel.QRViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Pantalla para guardar un QR escaneado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveQRScreen(
    navController: NavController,
    qrUrl: String,
    viewModel: QRViewModel = viewModel()
) {
    val context = LocalContext.current
    val decodedUrl = remember {
        URLDecoder.decode(qrUrl, StandardCharsets.UTF_8.toString())
    }
    
    var qrName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(QRColor.BLUE) }
    
    // Campos para información de la clase
    var selectedDay by remember { mutableStateOf<DayOfWeek?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var notificationEnabled by remember { mutableStateOf(true) }
    var showDayPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val saveResult by viewModel.saveResult.collectAsState()
    
    // Observar resultado del guardado
    LaunchedEffect(saveResult) {
        when (saveResult) {
            is SaveQRResult.Success -> {
                Toast.makeText(context, "QR guardado exitosamente", Toast.LENGTH_SHORT).show()
                viewModel.resetSaveResult()
                navController.navigate(Screen.SavedQRs.route) {
                    popUpTo(Screen.MainMenu.route)
                }
            }
            is SaveQRResult.Duplicate -> {
                Toast.makeText(context, "Este QR ya está guardado", Toast.LENGTH_SHORT).show()
                viewModel.resetSaveResult()
            }
            is SaveQRResult.LimitReached -> {
                Toast.makeText(context, "Límite de 20 QRs alcanzado", Toast.LENGTH_LONG).show()
                viewModel.resetSaveResult()
            }
            is SaveQRResult.InvalidURL -> {
                Toast.makeText(
                    context,
                    "URL inválida. Solo se permiten QRs de registroasistenciaqr.unab.cl",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetSaveResult()
                navController.navigateUp()
            }
            null -> { /* No hacer nada */ }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guardar QR") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Personaliza tu botón de acceso rápido",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Campo de nombre
            OutlinedTextField(
                value = qrName,
                onValueChange = { if (it.length <= 30) qrName = it },
                label = { Text("Nombre del botón") },
                placeholder = { Text("Ej: Sala 101") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Text(
                text = "${qrName.length}/30",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selector de día de la semana
            OutlinedButton(
                onClick = { showDayPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedDay?.let { getDayInSpanish(it) } ?: "Seleccionar día de clase",
                    color = if (selectedDay == null) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Selector de hora
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Seleccionar hora de clase",
                    color = if (selectedTime == null) Color.Gray else MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Switch para habilitar notificaciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recordatorio de clase",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Recibe una notificación al iniciar la clase",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { notificationEnabled = it }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Selecciona un color",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )
            
            // Paleta de colores
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(QRColor.values().toList()) { color ->
                    ColorCircle(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { selectedColor = color }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Preview del botón
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = CardDefaults.cardColors(
                    containerColor = selectedColor.color
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = qrName.ifEmpty { "Vista previa" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = {
                        when {
                            qrName.isBlank() -> {
                                Toast.makeText(
                                    context,
                                    "Por favor ingresa un nombre",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            selectedDay == null -> {
                                Toast.makeText(
                                    context,
                                    "Por favor selecciona el día de clase",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            selectedTime == null -> {
                                Toast.makeText(
                                    context,
                                    "Por favor selecciona la hora de clase",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                val newQR = SavedQR(
                                    link = decodedUrl,
                                    name = qrName,
                                    color = selectedColor,
                                    position = 0, // Se ajustará en el ViewModel
                                    courseName = qrName, // Usamos el nombre del botón como nombre del curso
                                    dayOfWeek = selectedDay,
                                    classTime = selectedTime,
                                    notificationEnabled = notificationEnabled
                                )
                                viewModel.saveQR(newQR)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = qrName.isNotBlank()
                ) {
                    Text("Guardar")
                }
            }
        }
    }
    
    // Diálogo para seleccionar día
    if (showDayPicker) {
        AlertDialog(
            onDismissRequest = { showDayPicker = false },
            title = { Text("Seleccionar día de clase") },
            text = {
                Column {
                    DayOfWeek.values().forEach { day ->
                        TextButton(
                            onClick = {
                                selectedDay = day
                                showDayPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = getDayInSpanish(day),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDayPicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para seleccionar hora
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { hour, minute ->
                selectedTime = LocalTime.of(hour, minute)
                showTimePicker = false
            }
        )
    }
}

/**
 * Convierte DayOfWeek a español
 */
private fun getDayInSpanish(day: DayOfWeek): String {
    return when (day) {
        DayOfWeek.MONDAY -> "Lunes"
        DayOfWeek.TUESDAY -> "Martes"
        DayOfWeek.WEDNESDAY -> "Miércoles"
        DayOfWeek.THURSDAY -> "Jueves"
        DayOfWeek.FRIDAY -> "Viernes"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
    }
}

/**
 * Diálogo simplificado para seleccionar hora
 */
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar hora de clase") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector de hora
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { selectedHour = (selectedHour + 1) % 24 }) {
                        Text("▲")
                    }
                    Text(
                        text = String.format("%02d", selectedHour),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedHour = if (selectedHour == 0) 23 else selectedHour - 1 }) {
                        Text("▼")
                    }
                }
                
                Text(" : ", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                
                // Selector de minutos
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { selectedMinute = (selectedMinute + 15) % 60 }) {
                        Text("▲")
                    }
                    Text(
                        text = String.format("%02d", selectedMinute),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { selectedMinute = if (selectedMinute == 0) 45 else selectedMinute - 15 }) {
                        Text("▼")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onTimeSelected(selectedHour, selectedMinute) }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ColorCircle(
    color: QRColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(color.color)
            .border(
                BorderStroke(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) Color.Black else Color.Transparent
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
