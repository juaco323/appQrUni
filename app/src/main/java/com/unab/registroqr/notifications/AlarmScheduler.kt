package com.unab.registroqr.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.unab.registroqr.data.SavedQR
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

/**
 * Gestor de alarmas para programar notificaciones
 */
object AlarmScheduler {
    
    /**
     * Programa alarmas para todas las notificaciones de un QR
     */
    fun scheduleClassNotification(context: Context, qr: SavedQR) {
        // Cancelar alarmas anteriores de este QR
        cancelClassNotification(context, qr.id)
        
        // Programar una alarma por cada notificación activa
        qr.notifications.forEachIndexed { index, notification ->
            if (notification.enabled) {
                scheduleNotification(context, qr, notification, index)
            }
        }
    }
    
    /**
     * Programa una notificación individual
     */
    private fun scheduleNotification(
        context: Context, 
        qr: SavedQR, 
        notification: com.unab.registroqr.data.ClassNotification,
        index: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Crear el intent para el BroadcastReceiver
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.unab.registroqr.CLASS_NOTIFICATION"
            putExtra("qr_id", qr.id)
            putExtra("qr_link", qr.link)
            putExtra("qr_name", qr.name)
            putExtra("course_name", notification.courseName)
            putExtra("day_of_week", notification.dayOfWeek.value)
            putExtra("class_time", notification.classTime.toString())
        }
        
        // Usar un código único para cada notificación (qr.id + índice)
        val requestCode = (qr.id + index.toString()).hashCode()
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calcular el próximo momento en que se debe mostrar la notificación (30 min antes)
        val triggerTime = getNextTriggerTime(notification.dayOfWeek, notification.classTime)
        
        // Programar la alarma
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requiere permisos especiales para alarmas exactas
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                // Fallback a alarma no exacta
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancela todas las alarmas programadas para un QR
     */
    fun cancelClassNotification(context: Context, qrId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancelar hasta 10 posibles notificaciones (suficiente para múltiples horarios)
        for (index in 0..9) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "com.unab.registroqr.CLASS_NOTIFICATION"
            }
            
            val requestCode = (qrId + index.toString()).hashCode()
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
    
    /**
     * Reprograma todas las alarmas (útil después de reiniciar el dispositivo)
     */
    fun rescheduleAllAlarms(context: Context, qrList: List<SavedQR>) {
        qrList.forEach { qr ->
            scheduleClassNotification(context, qr)
        }
    }
    
    /**
     * Calcula el próximo momento en que se debe activar la alarma (5 minutos antes de la clase)
     */
    private fun getNextTriggerTime(dayOfWeek: DayOfWeek, classTime: LocalTime): Long {
        val now = LocalDateTime.now()
        var nextDate = LocalDate.now()
        
        // Calcular la hora de notificación (5 minutos antes)
        val notificationTime = classTime.minusMinutes(5)
        
        // Encontrar la próxima ocurrencia del día de la semana
        while (nextDate.dayOfWeek != dayOfWeek || 
               (nextDate == now.toLocalDate() && notificationTime <= now.toLocalTime())) {
            nextDate = nextDate.plusDays(1)
        }
        
        // Combinar fecha y hora de notificación
        val nextDateTime = LocalDateTime.of(nextDate, notificationTime)
        
        // Convertir a milisegundos desde epoch
        return nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
