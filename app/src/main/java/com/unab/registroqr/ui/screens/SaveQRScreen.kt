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
import androidx.compose.material.icons.filled.Delete
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
import java.util.UUID
import android.util.Log

/**
 * Clase de datos para representar un horario individual
 */
data class ScheduleItem(
    val id: String = UUID.randomUUID().toString(),
    val day: DayOfWeek,
    val time: LocalTime
)

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
    
    // Campos temporales para agregar un horario
    var selectedDay by remember { mutableStateOf<DayOfWeek?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    
    // Lista de horarios agregados
    var scheduleList by remember { mutableStateOf<List<ScheduleItem>>(emptyList()) }
    
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
            
            // Sección de horarios
            Text(
                text = "Horarios de clase",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Mostrar horarios agregados
            if (scheduleList.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scheduleList.forEach { schedule ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = getDayInSpanish(schedule.day),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = schedule.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        scheduleList = scheduleList.filter { it.id != schedule.id }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar horario",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Selectores para agregar nuevo horario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDayPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = selectedDay?.let { getDayInSpanish(it) } ?: "Día",
                        color = if (selectedDay == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }
                
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Hora",
                        color = if (selectedTime == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón para agregar horario
            Button(
                onClick = {
                    if (selectedDay != null && selectedTime != null) {
                        val newSchedule = ScheduleItem(
                            day = selectedDay!!,
                            time = selectedTime!!
                        )
                        scheduleList = scheduleList + newSchedule
                        // Limpiar selección
                        selectedDay = null
                        selectedTime = null
                    } else {
                        Toast.makeText(
                            context,
                            "Selecciona día y hora para agregar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedDay != null && selectedTime != null
            ) {
                Text("+ Agregar horario")
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
                            scheduleList.isEmpty() -> {
                                Toast.makeText(
                                    context,
                                    "Por favor agrega al menos un horario de clase",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> {
                                // Crear la lista de notificaciones desde scheduleList
                                Log.d("SaveQRScreen", "=== Intentando guardar QR ===")
                                Log.d("SaveQRScreen", "Nombre: $qrName")
                                Log.d("SaveQRScreen", "URL: $decodedUrl")
                                Log.d("SaveQRScreen", "Horarios agregados: ${scheduleList.size}")
                                scheduleList.forEachIndexed { index, schedule ->
                                    Log.d("SaveQRScreen", "  Horario $index: ${schedule.day} a las ${schedule.time}")
                                }
                                Log.d("SaveQRScreen", "Notificaciones habilitadas: $notificationEnabled")
                                
                                val notifications = scheduleList.map { schedule ->
                                    com.unab.registroqr.data.ClassNotification(
                                        courseName = qrName,
                                        dayOfWeek = schedule.day,
                                        classTime = schedule.time,
                                        enabled = notificationEnabled
                                    )
                                }
                                
                                Log.d("SaveQRScreen", "Notificaciones creadas: ${notifications.size}")
                                
                                val newQR = SavedQR(
                                    link = decodedUrl,
                                    name = qrName,
                                    color = selectedColor,
                                    position = 0, // Se ajustará en el ViewModel
                                    notifications = notifications,
                                    // Campos legacy para compatibilidad
                                    courseName = qrName,
                                    dayOfWeek = scheduleList.firstOrNull()?.day,
                                    classTime = scheduleList.firstOrNull()?.time,
                                    notificationEnabled = notificationEnabled
                                )
                                
                                Log.d("SaveQRScreen", "QR creado con ID: ${newQR.id}")
                                Log.d("SaveQRScreen", "Llamando a viewModel.saveQR()...")
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
 * Diálogo con reloj visual para seleccionar hora
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { 
                onTimeSelected(timePickerState.hour, timePickerState.minute)
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Seleccionar hora de clase",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp)
                )
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
