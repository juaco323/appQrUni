package com.unab.registroqr.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.DayOfWeek

/**
 * Receiver que recibe las alarmas programadas y muestra las notificaciones
 */
class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.unab.registroqr.CLASS_NOTIFICATION") {
            val qrId = intent.getStringExtra("qr_id") ?: return
            val qrLink = intent.getStringExtra("qr_link") ?: ""
            val courseName = intent.getStringExtra("course_name") ?: "Clase"
            val dayOfWeekValue = intent.getIntExtra("day_of_week", 1)
            val classTime = intent.getStringExtra("class_time") ?: "00:00"
            
            val dayOfWeek = DayOfWeek.of(dayOfWeekValue)
            
            // Mostrar la notificación
            NotificationHelper.showClassNotification(
                context,
                qrId,
                qrLink,
                courseName,
                dayOfWeek,
                classTime
            )
            
            // Reprogramar la alarma para la próxima semana
            // (Esto se hace automáticamente al cargar los QRs desde el Repository)
        }
    }
}
