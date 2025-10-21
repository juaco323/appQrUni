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
    
    // Prefijo válido para QRs de asistencia UNAB
    const val VALID_QR_PREFIX = "https://registroasistenciaqr.unab.cl/"
    
    // Scanner optimizado para QR codes
    private val scannerOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE) // Solo escanear QR codes
        .build()
    
    private val optimizedScanner = BarcodeScanning.getClient(scannerOptions)
    
    /**
     * Valida si una URL es un QR válido de asistencia UNAB
     * Acepta URLs que comiencen con: https://registroasistenciaqr.unab.cl/
     * Ejemplo válido: https://registroasistenciaqr.unab.cl/?validate=U2FsdGVkX1+sjm7UJLrU...
     */
    fun isValidUNABQR(url: String): Boolean {
        return url.startsWith(VALID_QR_PREFIX, ignoreCase = false)
    }
    
    /**
     * Escanea una imagen en busca de códigos QR
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
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
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
