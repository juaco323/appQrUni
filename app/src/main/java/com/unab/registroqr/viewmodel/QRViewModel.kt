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
            val result = repository.saveQR(qr.copy(position = _qrList.value.size))
            _saveResult.value = result
            if (result is SaveQRResult.Success) {
                // Programar notificación si está habilitada
                if (qr.notificationEnabled) {
                    AlarmScheduler.scheduleClassNotification(getApplication(), qr)
                }
                loadQRs()
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
     * Actualiza el orden de los QR
     */
    fun updateQROrder(newOrder: List<SavedQR>) {
        viewModelScope.launch {
            repository.updateQROrder(newOrder)
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
