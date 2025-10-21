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
     * Programa una alarma para mostrar notificación a la hora de clase
     */
    fun scheduleClassNotification(context: Context, qr: SavedQR) {
        if (qr.dayOfWeek == null || qr.classTime == null || !qr.notificationEnabled) {
            return
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Crear el intent para el BroadcastReceiver
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.unab.registroqr.CLASS_NOTIFICATION"
            putExtra("qr_id", qr.id)
            putExtra("qr_link", qr.link)
            putExtra("course_name", qr.courseName)
            putExtra("day_of_week", qr.dayOfWeek.value)
            putExtra("class_time", qr.classTime.toString())
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            qr.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calcular el próximo momento en que se debe mostrar la notificación
        val triggerTime = getNextTriggerTime(qr.dayOfWeek, qr.classTime)
        
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
     * Cancela una alarma programada
     */
    fun cancelClassNotification(context: Context, qrId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.unab.registroqr.CLASS_NOTIFICATION"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            qrId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
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
     * Calcula el próximo momento en que se debe activar la alarma
     */
    private fun getNextTriggerTime(dayOfWeek: DayOfWeek, classTime: LocalTime): Long {
        val now = LocalDateTime.now()
        var nextDate = LocalDate.now()
        
        // Encontrar la próxima ocurrencia del día de la semana
        while (nextDate.dayOfWeek != dayOfWeek || 
               (nextDate == now.toLocalDate() && classTime <= now.toLocalTime())) {
            nextDate = nextDate.plusDays(1)
        }
        
        // Combinar fecha y hora
        val nextDateTime = LocalDateTime.of(nextDate, classTime)
        
        // Convertir a milisegundos desde epoch
        return nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
