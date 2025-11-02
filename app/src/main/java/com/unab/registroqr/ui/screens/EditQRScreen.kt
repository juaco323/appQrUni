package com.unab.registroqr.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unab.registroqr.data.ClassNotification
import com.unab.registroqr.data.QRColor
import com.unab.registroqr.data.ScheduleItem
import com.unab.registroqr.utils.getDayInSpanish
import com.unab.registroqr.viewmodel.QRViewModel
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQRScreen(
    navController: NavController,
    qrId: String,
    viewModel: QRViewModel = viewModel()
) {
    val context = LocalContext.current
    val qrList by viewModel.qrList.collectAsState()

    val currentQR = remember(qrList, qrId) {
        qrList.find { it.id == qrId }
    }

    LaunchedEffect(currentQR) {
        if (currentQR == null) {
            Toast.makeText(context, "QR no encontrado", Toast.LENGTH_SHORT).show()
            navController.navigateUp()
        }
    }

    var qrName by remember(currentQR) { mutableStateOf(currentQR?.name ?: "") }
    var selectedColor by remember(currentQR) { mutableStateOf(currentQR?.color ?: QRColor.BLUE) }

    var scheduleList by remember(currentQR) {
        mutableStateOf(
            currentQR?.notifications?.map { notification ->
                ScheduleItem(
                    id = notification.id,
                    day = notification.dayOfWeek,
                    time = notification.classTime
                )
            } ?: emptyList()
        )
    }

    var notificationEnabled by remember(currentQR) {
        mutableStateOf(currentQR?.notifications?.firstOrNull()?.enabled ?: true)
    }

    var selectedDay by remember { mutableStateOf<DayOfWeek?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var showDayPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (currentQR == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar QR") },
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

            Text(
                text = "Horarios de clase",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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

            Button(
                onClick = {
                    if (selectedDay != null && selectedTime != null) {
                        val newSchedule = ScheduleItem(
                            day = selectedDay!!,
                            time = selectedTime!!
                        )
                        scheduleList = scheduleList + newSchedule
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

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(QRColor.values().toList()) { color ->
                    EditColorCircle(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                                val updatedNotifications = scheduleList.map { schedule ->
                                    ClassNotification(
                                        id = schedule.id,
                                        courseName = qrName,
                                        dayOfWeek = schedule.day,
                                        classTime = schedule.time,
                                        enabled = notificationEnabled
                                    )
                                }

                                val updatedQR = currentQR.copy(
                                    name = qrName,
                                    color = selectedColor,
                                    notifications = updatedNotifications,
                                    courseName = qrName,
                                    dayOfWeek = scheduleList.firstOrNull()?.day,
                                    classTime = scheduleList.firstOrNull()?.time,
                                    notificationEnabled = notificationEnabled
                                )

                                viewModel.updateQR(updatedQR)
                                Toast.makeText(context, "QR actualizado", Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = qrName.isNotBlank()
                ) {
                    Text("Guardar cambios")
                }
            }
        }
    }

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
                                textAlign = TextAlign.Start
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

    if (showTimePicker) {
        EditTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { hour, minute ->
                selectedTime = LocalTime.of(hour, minute)
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimePickerDialog(
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
fun EditColorCircle(
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