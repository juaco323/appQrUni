package com.unab.registroqr.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.unab.registroqr.utils.QRScanner
import android.util.Log
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Repositorio para gestionar los QR guardados usando SharedPreferences
 */
class QRRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
        .registerTypeAdapter(DayOfWeek::class.java, DayOfWeekAdapter())
        .create()
    
    companion object {
        private const val PREFS_NAME = "qr_storage"
        private const val KEY_QR_LIST = "qr_list"
        const val MAX_QR_COUNT = 20
    }
    
    /**
     * Obtiene todos los QR guardados
     */
    fun getAllQRs(): List<SavedQR> {
        val json = sharedPreferences.getString(KEY_QR_LIST, null)
        Log.d("QRRepository", "=== getAllQRs ===")
        Log.d("QRRepository", "JSON recuperado: ${json?.take(200) ?: "null"}")
        
        if (json == null) {
            Log.d("QRRepository", "JSON es null, retornando lista vacía")
            return emptyList()
        }
        
        val type = object : TypeToken<List<SavedQR>>() {}.type
        return try {
            val list = gson.fromJson<List<SavedQR>>(json, type)
            Log.d("QRRepository", "QRs deserializados: ${list.size}")
            list
        } catch (e: Exception) {
            Log.e("QRRepository", "ERROR al deserializar: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Guarda un nuevo QR
     * @return SaveQRResult indicando el resultado de la operación
     */
    fun saveQR(qr: SavedQR): SaveQRResult {
        Log.d("QRRepository", "=== saveQR llamado ===")
        Log.d("QRRepository", "URL a validar: ${qr.link}")
        
        // Validación de seguridad: verificar que el link sea válido
        if (!QRScanner.isValidUNABQR(qr.link)) {
            Log.e("QRRepository", "✗ URL INVÁLIDA - No es un QR de UNAB")
            return SaveQRResult.InvalidURL
        }
        Log.d("QRRepository", "✓ URL válida")
        
        val currentList = getAllQRs().toMutableList()
        Log.d("QRRepository", "QRs actuales: ${currentList.size}")
        
        // Verificar límite
        if (currentList.size >= MAX_QR_COUNT) {
            Log.e("QRRepository", "✗ LÍMITE ALCANZADO ($MAX_QR_COUNT)")
            return SaveQRResult.LimitReached
        }
        
        // Verificar duplicado
        if (currentList.any { it.link == qr.link }) {
            Log.e("QRRepository", "✗ QR DUPLICADO")
            return SaveQRResult.Duplicate
        }
        
        currentList.add(qr)
        saveList(currentList)
        Log.d("QRRepository", "✓ QR guardado. Total ahora: ${currentList.size}")
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
        Log.d("QRRepository", "=== saveList ===")
        Log.d("QRRepository", "Guardando ${list.size} QRs")
        list.forEachIndexed { index, qr ->
            Log.d("QRRepository", "  QR $index: ${qr.name}")
            Log.d("QRRepository", "    notifications.size: ${qr.notifications.size}")
            qr.notifications.forEachIndexed { i, notif ->
                Log.d("QRRepository", "      [$i] ${notif.courseName} - ${notif.dayOfWeek} ${notif.classTime}")
            }
        }
        
        val json = gson.toJson(list)
        Log.d("QRRepository", "JSON a guardar (primeros 500 chars): ${json.take(500)}")
        sharedPreferences.edit().putString(KEY_QR_LIST, json).apply()
        Log.d("QRRepository", "✓ Guardado en SharedPreferences")
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
