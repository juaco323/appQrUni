package com.unab.registroqr.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.unab.registroqr.MainActivity
import com.unab.registroqr.R
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

/**
 * Gestor de notificaciones para recordatorios de clases
 */
object NotificationHelper {
    
    private const val CHANNEL_ID = "class_reminders"
    private const val CHANNEL_NAME = "Recordatorios de Clase"
    private const val CHANNEL_DESCRIPTION = "Notificaciones para recordar registrar asistencia"
    
    /**
     * Crea el canal de notificaciones (necesario para Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Muestra una notificación de recordatorio de clase
     */
    fun showClassNotification(
        context: Context,
        qrId: String,
        qrLink: String,
        courseName: String,
        dayOfWeek: DayOfWeek,
        classTime: String
    ) {
        // Intent para abrir la app al tocar la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("qr_id", qrId)
            putExtra("qr_link", qrLink)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            qrId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Obtener el día en español
        val dayInSpanish = getDayInSpanish(dayOfWeek)
        
        // Construir la notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏰ Registra tu asistencia")
            .setContentText("Clase: $courseName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Recuerda registrar tu asistencia dentro de 5 minutos\n\nClase: $courseName\nDía: $dayInSpanish\nHora: $classTime"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()
        
        // Mostrar la notificación
        try {
            NotificationManagerCompat.from(context).notify(qrId.hashCode(), notification)
        } catch (e: SecurityException) {
            // El usuario no ha dado permiso de notificaciones
            e.printStackTrace()
        }
    }
    
    /**
     * Convierte DayOfWeek a español
     */
    private fun getDayInSpanish(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
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
     * Cancela una notificación programada
     */
    fun cancelNotification(context: Context, qrId: String) {
        NotificationManagerCompat.from(context).cancel(qrId.hashCode())
    }
}
