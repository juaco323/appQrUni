package com.unab.registroqr.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utilidades para escaneo de códigos QR
 */
object QRScanner {
    
    // Prefijo válido para QRs de asistencia UNAB (sin barra final)
    const val VALID_QR_PREFIX = "https://registroasistenciaqr.unab.cl"
    
    // Scanner optimizado para QR codes personalizados con logo
    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE) // Solo escanear QR codes
        .enableAllPotentialBarcodes() // Detectar todos los códigos posibles, incluso parcialmente dañados
        .build()
    
    private val optimizedScanner = BarcodeScanning.getClient(scannerOptions)
    
    /**
     * Valida si una URL es un QR válido de asistencia UNAB
     * Acepta URLs que comiencen con: https://registroasistenciaqr.unab.cl
     * Ejemplo válido: https://registroasistenciaqr.unab.cl/?validate=U2FsdGVkX1+sjm7UJLrU...
     */
    fun isValidUNABQR(url: String): Boolean {
        return url.startsWith(VALID_QR_PREFIX, ignoreCase = false)
    }
    
    /**
     * Escanea una imagen en busca de códigos QR
     * Optimizado para QR codes personalizados con logo en el centro
     */
    suspend fun scanQRFromImage(imageProxy: ImageProxy): QRScanResult {
        return suspendCancellableCoroutine { continuation ->
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                continuation.resume(QRScanResult.NoQRFound)
                imageProxy.close()
                return@suspendCancellableCoroutine
            }
            
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            optimizedScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Intentar encontrar cualquier QR code, incluso si está parcialmente oscurecido
                    val validQR = barcodes.firstOrNull { barcode ->
                        barcode.format == Barcode.FORMAT_QR_CODE && 
                        barcode.rawValue != null &&
                        barcode.rawValue!!.isNotEmpty()
                    }
                    
                    if (validQR != null) {
                        val rawValue = validQR.rawValue!!
                        // Validación estricta: solo URLs de registroasistenciaqr.unab.cl
                        if (isValidUNABQR(rawValue)) {
                            continuation.resume(QRScanResult.Success(rawValue))
                        } else {
                            continuation.resume(QRScanResult.InvalidQR)
                        }
                    } else {
                        continuation.resume(QRScanResult.NoQRFound)
                    }
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    // Si falla, intentar de nuevo con configuración alternativa
                    android.util.Log.w("QRScanner", "Error scanning QR: ${e.message}")
                    continuation.resume(QRScanResult.NoQRFound)
                    imageProxy.close()
                }
        }
    }
    
    /**
     * Escanea un Bitmap en busca de códigos QR
     */
    suspend fun scanQRFromBitmap(bitmap: Bitmap): QRScanResult {
        return suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            
            optimizedScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isEmpty()) {
                        continuation.resume(QRScanResult.NoQRFound)
                    } else {
                        val qrCode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                        if (qrCode != null) {
                            val rawValue = qrCode.rawValue ?: ""
                            // Validación estricta: solo URLs de registroasistenciaqr.unab.cl
                            if (isValidUNABQR(rawValue)) {
                                continuation.resume(QRScanResult.Success(rawValue))
                            } else {
                                continuation.resume(QRScanResult.InvalidQR)
                            }
                        } else {
                            continuation.resume(QRScanResult.NoQRFound)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }
    
    /**
     * Escanea una imagen desde URI (galería) en busca de códigos QR
     * Optimizado para imágenes con mejor procesamiento
     */
    suspend fun scanQRFromUri(context: Context, uri: android.net.Uri): QRScanResult {
        return try {
            // Usar scanner sin restricciones para imágenes estáticas
            val imageScanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
            )
            
            val image = InputImage.fromFilePath(context, uri)
            
            suspendCancellableCoroutine { continuation ->
                imageScanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        android.util.Log.d("QRScanner", "Barcodes encontrados: ${barcodes.size}")
                        barcodes.forEach { barcode ->
                            android.util.Log.d("QRScanner", "Barcode: ${barcode.rawValue}")
                        }
                        
                        val validQR = barcodes.firstOrNull { barcode ->
                            barcode.format == Barcode.FORMAT_QR_CODE && 
                            barcode.rawValue != null &&
                            barcode.rawValue!!.isNotEmpty()
                        }
                        
                        if (validQR != null) {
                            val rawValue = validQR.rawValue!!
                            android.util.Log.d("QRScanner", "QR válido encontrado: $rawValue")
                            if (isValidUNABQR(rawValue)) {
                                continuation.resume(QRScanResult.Success(rawValue))
                            } else {
                                android.util.Log.d("QRScanner", "QR no es de UNAB")
                                continuation.resume(QRScanResult.InvalidQR)
                            }
                        } else {
                            android.util.Log.d("QRScanner", "No se encontró ningún QR")
                            continuation.resume(QRScanResult.NoQRFound)
                        }
                        
                        // Liberar recursos
                        imageScanner.close()
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("QRScanner", "Error scanning from URI: ${e.message}", e)
                        continuation.resume(QRScanResult.NoQRFound)
                        imageScanner.close()
                    }
            }
        } catch (e: Exception) {
            android.util.Log.e("QRScanner", "Error loading image from URI: ${e.message}", e)
            QRScanResult.NoQRFound
        }
    }
    
    /**
     * Obtiene el provider de la cámara
     */
    suspend fun getCameraProvider(context: Context): ProcessCameraProvider {
        return suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                continuation.resume(cameraProviderFuture.get())
            }, ContextCompat.getMainExecutor(context))
        }
    }
}

/**
 * Resultado del escaneo de QR
 */
sealed class QRScanResult {
    data class Success(val url: String) : QRScanResult()
    object NoQRFound : QRScanResult()
    object InvalidQR : QRScanResult()
}
