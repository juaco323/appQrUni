package com.unab.registroqr.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.unab.registroqr.data.QRRepository

/**
 * Receiver que se ejecuta al iniciar el dispositivo para reprogramar las alarmas
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reprogramar todas las alarmas
            val repository = QRRepository(context)
            val qrList = repository.getAllQRs()
            
            AlarmScheduler.rescheduleAllAlarms(context, qrList)
        }
    }
}
