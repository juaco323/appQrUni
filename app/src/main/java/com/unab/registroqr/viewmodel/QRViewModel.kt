package com.unab.registroqr.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unab.registroqr.data.QRRepository
import com.unab.registroqr.data.SaveQRResult
import com.unab.registroqr.data.SavedQR
import com.unab.registroqr.notifications.AlarmScheduler
import com.unab.registroqr.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel principal de la aplicación
 */
class QRViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = QRRepository(application)
    
    private val _qrList = MutableStateFlow<List<SavedQR>>(emptyList())
    val qrList: StateFlow<List<SavedQR>> = _qrList.asStateFlow()
    
    private val _saveResult = MutableStateFlow<SaveQRResult?>(null)
    val saveResult: StateFlow<SaveQRResult?> = _saveResult.asStateFlow()
    
    init {
        // Crear canal de notificaciones
        NotificationHelper.createNotificationChannel(application)
        
        // Cargar QRs y reprogramar notificaciones
        loadQRs()
        rescheduleNotifications()
    }
    
    /**
     * Carga los QR desde el repositorio
     * Los ordena por día de la semana y hora de clase
     */
    fun loadQRs() {
        viewModelScope.launch {
            _qrList.value = repository.getAllQRs()
                .sortedWith(compareBy(
                    // Primero por día de la semana (lunes=1, domingo=7)
                    { it.dayOfWeek?.value ?: Int.MAX_VALUE },
                    // Luego por hora de clase
                    { it.classTime?.toSecondOfDay() ?: Int.MAX_VALUE }
                ))
        }
    }
    
    /**
     * Guarda un nuevo QR
     */
    fun saveQR(qr: SavedQR) {
        viewModelScope.launch {
            Log.d("QRViewModel", "=== saveQR llamado ===")
            Log.d("QRViewModel", "QR ID: ${qr.id}")
            Log.d("QRViewModel", "QR Name: ${qr.name}")
            Log.d("QRViewModel", "QR Link: ${qr.link}")
            Log.d("QRViewModel", "Notificaciones: ${qr.notifications.size}")
            
            val result = repository.saveQR(qr.copy(position = _qrList.value.size))
            Log.d("QRViewModel", "Resultado del guardado: $result")
            
            _saveResult.value = result
            if (result is SaveQRResult.Success) {
                Log.d("QRViewModel", "✓ QR guardado exitosamente")
                // Programar notificación si está habilitada
                if (qr.notificationEnabled) {
                    Log.d("QRViewModel", "Programando notificaciones...")
                    AlarmScheduler.scheduleClassNotification(getApplication(), qr)
                }
                loadQRs()
                Log.d("QRViewModel", "Lista recargada. Total QRs: ${_qrList.value.size}")
            } else {
                Log.e("QRViewModel", "✗ Error al guardar: $result")
            }
        }
    }
    
    /**
     * Elimina un QR
     */
    fun deleteQR(id: String) {
        viewModelScope.launch {
            // Cancelar notificación programada
            AlarmScheduler.cancelClassNotification(getApplication(), id)
            repository.deleteQR(id)
            loadQRs()
        }
    }
    
    /**
     * Actualiza un QR existente
     */
    fun updateQR(qr: SavedQR) {
        viewModelScope.launch {
            repository.updateQR(qr)
            loadQRs()
        }
    }
    
    /**
     * Verifica si un link ya existe
     */
    fun linkExists(link: String): Boolean {
        return repository.linkExists(link)
    }
    
    /**
     * Resetea el resultado de guardado
     */
    fun resetSaveResult() {
        _saveResult.value = null
    }
    
    /**
     * Obtiene el número actual de QR guardados
     */
    fun getQRCount(): Int {
        return _qrList.value.size
    }
    
    /**
     * Reprograma todas las notificaciones
     */
    private fun rescheduleNotifications() {
        viewModelScope.launch {
            val qrs = repository.getAllQRs()
            AlarmScheduler.rescheduleAllAlarms(getApplication(), qrs)
        }
    }
}
