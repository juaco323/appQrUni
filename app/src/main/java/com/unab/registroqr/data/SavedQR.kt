package com.unab.registroqr.data

import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Modelo de datos para una notificación de clase
 */
data class ClassNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val courseName: String,               // Nombre del ramo/asignatura
    val dayOfWeek: DayOfWeek,             // Día de la semana (Lun, Mar, Mié, etc.)
    val classTime: LocalTime,             // Hora de inicio de la clase
    val enabled: Boolean = true           // Si esta notificación está activa
)

/**
 * Modelo de datos para un QR guardado
 */
data class SavedQR(
    val id: String = java.util.UUID.randomUUID().toString(),
    val link: String,
    val name: String,
    val color: QRColor,
    val position: Int,
    // Lista de notificaciones para este QR
    val notifications: List<ClassNotification> = emptyList(),
    
    // Campos legacy para compatibilidad (se convertirán a notificaciones)
    val courseName: String = "",
    val dayOfWeek: DayOfWeek? = null,
    val classTime: LocalTime? = null,
    val notificationEnabled: Boolean = true
)

/**
 * Colores disponibles para los botones de QR
 */
enum class QRColor(val color: Color) {
    RED(Color(0xFFE57373)),
    PINK(Color(0xFFF06292)),
    PURPLE(Color(0xFFBA68C8)),
    DEEP_PURPLE(Color(0xFF9575CD)),
    INDIGO(Color(0xFF7986CB)),
    BLUE(Color(0xFF64B5F6)),
    LIGHT_BLUE(Color(0xFF4FC3F7)),
    CYAN(Color(0xFF4DD0E1)),
    TEAL(Color(0xFF4DB6AC)),
    GREEN(Color(0xFF81C784)),
    LIGHT_GREEN(Color(0xFFAED581)),
    LIME(Color(0xFFDCE775)),
    YELLOW(Color(0xFFFFF176)),
    AMBER(Color(0xFFFFD54F)),
    ORANGE(Color(0xFFFFB74D)),
    DEEP_ORANGE(Color(0xFFFF8A65))
}
