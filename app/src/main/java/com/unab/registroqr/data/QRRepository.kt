package com.unab.registroqr.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.unab.registroqr.utils.QRScanner

/**
 * Repositorio para gestionar los QR guardados usando SharedPreferences
 */
class QRRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "qr_storage"
        private const val KEY_QR_LIST = "qr_list"
        const val MAX_QR_COUNT = 20
    }
    
    /**
     * Obtiene todos los QR guardados
     */
    fun getAllQRs(): List<SavedQR> {
        val json = sharedPreferences.getString(KEY_QR_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedQR>>() {}.type
        return gson.fromJson(json, type)
    }
    
    /**
     * Guarda un nuevo QR
     * @return SaveQRResult indicando el resultado de la operación
     */
    fun saveQR(qr: SavedQR): SaveQRResult {
        // Validación de seguridad: verificar que el link sea válido
        if (!QRScanner.isValidUNABQR(qr.link)) {
            return SaveQRResult.InvalidURL
        }
        
        val currentList = getAllQRs().toMutableList()
        
        // Verificar límite
        if (currentList.size >= MAX_QR_COUNT) {
            return SaveQRResult.LimitReached
        }
        
        // Verificar duplicado
        if (currentList.any { it.link == qr.link }) {
            return SaveQRResult.Duplicate
        }
        
        currentList.add(qr)
        saveList(currentList)
        return SaveQRResult.Success
    }
    
    /**
     * Elimina un QR por su ID
     */
    fun deleteQR(id: String) {
        val currentList = getAllQRs().toMutableList()
        currentList.removeIf { it.id == id }
        // Reajustar posiciones
        currentList.forEachIndexed { index, qr ->
            currentList[index] = qr.copy(position = index)
        }
        saveList(currentList)
    }
    
    /**
     * Actualiza el orden de los QR
     */
    fun updateQROrder(qrList: List<SavedQR>) {
        val updatedList = qrList.mapIndexed { index, qr ->
            qr.copy(position = index)
        }
        saveList(updatedList)
    }
    
    /**
     * Actualiza un QR existente
     */
    fun updateQR(updatedQR: SavedQR) {
        val currentList = getAllQRs().toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedQR.id }
        if (index != -1) {
            currentList[index] = updatedQR
            saveList(currentList)
        }
    }
    
    /**
     * Verifica si un link ya existe
     */
    fun linkExists(link: String): Boolean {
        return getAllQRs().any { it.link == link }
    }
    
    /**
     * Guarda la lista completa en SharedPreferences
     */
    private fun saveList(list: List<SavedQR>) {
        val json = gson.toJson(list)
        sharedPreferences.edit().putString(KEY_QR_LIST, json).apply()
    }
    
    /**
     * Limpia todos los QR guardados (útil para testing)
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}

/**
 * Resultado de intentar guardar un QR
 */
sealed class SaveQRResult {
    object Success : SaveQRResult()
    object Duplicate : SaveQRResult()
    object LimitReached : SaveQRResult()
    object InvalidURL : SaveQRResult()
}
