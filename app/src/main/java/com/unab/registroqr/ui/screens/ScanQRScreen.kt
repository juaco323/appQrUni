package com.unab.registroqr.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.unab.registroqr.navigation.Screen
import com.unab.registroqr.utils.QRScanResult
import com.unab.registroqr.utils.QRScanner
import com.unab.registroqr.viewmodel.QRViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

/**
 * Pantalla de escaneo de códigos QR
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanQRScreen(
    navController: NavController,
    viewModel: QRViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    val scope = rememberCoroutineScope()
    var hasScanned by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear QR") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        when {
            cameraPermissionState.status.isGranted -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Vista previa de la cámara
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            
                            scope.launch {
                                val cameraProvider = QRScanner.getCameraProvider(ctx)
                                val preview = Preview.Builder()
                                    .build()
                                    .also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                
                                // Análisis de imagen en tiempo real para detectar QR - SUPER OPTIMIZADO
                                val imageAnalyzer = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .setTargetResolution(android.util.Size(1280, 720)) // Resolución óptima
                                    .build()
                                    .also {
                                        it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                            // Procesar cada frame sin bloquear
                                            if (!hasScanned) {
                                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                    try {
                                                        when (val result = QRScanner.scanQRFromImage(imageProxy)) {
                                                            is QRScanResult.Success -> {
                                                                if (!hasScanned) { // Doble verificación
                                                                    hasScanned = true
                                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                        try {
                                                                            val encodedUrl = URLEncoder.encode(
                                                                                result.url,
                                                                                StandardCharsets.UTF_8.toString()
                                                                            )
                                                                            navController.navigate(Screen.SaveQR.createRoute(encodedUrl))
                                                                        } catch (e: Exception) {
                                                                            Toast.makeText(
                                                                                context,
                                                                                "Error al procesar el QR: ${e.message}",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            hasScanned = false
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            is QRScanResult.InvalidQR -> {
                                                                if (!hasScanned) {
                                                                    hasScanned = true
                                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Este QR no es válido para registro de asistencia UNAB",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                    }
                                                                    // Resetear más rápido
                                                                    kotlinx.coroutines.delay(1500)
                                                                    hasScanned = false
                                                                }
                                                            }
                                                            is QRScanResult.NoQRFound -> {
                                                                // No hace nada, sigue escaneando continuamente
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        // Log del error para debugging
                                                        android.util.Log.e("ScanQRScreen", "Error scanning: ${e.message}", e)
                                                    }
                                                }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }
                                    }
                                
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                
                                try {
                                    cameraProvider.unbindAll()
                                    val camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalyzer
                                    )
                                    
                                    // Guardar referencia al control de cámara para el flash
                                    cameraControl = camera.cameraControl
                                    
                                    // Habilitar tap-to-focus y auto-enfoque continuo
                                    camera.cameraControl.enableTorch(false) // Apagar flash por defecto
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "Error al iniciar la cámara", Toast.LENGTH_SHORT).show()
                                }
                            }
                            
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Recuadro de escaneo (visual, no limita la detección)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        // Tamaño del recuadro más grande (85% del ancho de la pantalla)
                        val scanAreaSize = canvasWidth * 0.85f
                        val left = (canvasWidth - scanAreaSize) / 2
                        val top = (canvasHeight - scanAreaSize) / 2
                        
                        // Fondo oscuro MUY transparente (no molesta)
                        drawRect(
                            color = Color.Black.copy(alpha = 0.3f),
                            size = size
                        )
                        
                        // Área transparente del recuadro
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = Offset(left, top),
                            size = Size(scanAreaSize, scanAreaSize),
                            cornerRadius = CornerRadius(20.dp.toPx()),
                            blendMode = BlendMode.Clear
                        )
                        
                        // Bordes del recuadro más suaves
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.8f),
                            topLeft = Offset(left, top),
                            size = Size(scanAreaSize, scanAreaSize),
                            cornerRadius = CornerRadius(20.dp.toPx()),
                            style = Stroke(width = 3.dp.toPx())
                        )
                        
                        // Esquinas destacadas más grandes y visibles
                        val cornerLength = 50.dp.toPx()
                        val cornerWidth = 5.dp.toPx()
                        
                        // Esquina superior izquierda
                        drawLine(
                            color = Color(0xFF64B5F6),
                            start = Offset(left, top),
                            end = Offset(left + cornerLength, top),
                            strokeWidth = cornerWidth
                        )
                        drawLine(
                            color = Color(0xFF64B5F6),
                            start = Offset(left, top),
                            end = Offset(left, top + cornerLength),
                            strokeWidth = cornerWidth
                        )
                        
                        // Esquina superior derecha
                        drawLine(
                            color = Color(0xFF64B5F6),
                            start = Offset(left + scanAreaSize, top),
                            end = Offset(left + scanAreaSize - cornerLength, top),
                            strokeWidth = cornerWidth
                        )
                        drawLine(
                            color = Color(0xFF64B5F6),
                            start = Offset(left + scanAreaSize, top),
                            end = Offset(left + scanAreaSize, top + cornerLength),
                            strokeWidth = cornerWidth
                        )
                        
                        // Esquina inferior izquierda
                        drawLine(
                            color = Color(0xFF64B5F6),
                            start = Offset(left, top + scanAreaSize),
                            end = Offset(left + cornerLength, top + scanAreaSize),
                            strokeWidth = cornerWidth
                        )
                        drawLine(
                            color = Color(0xFF64B5F6),
                            start = Offset(left, top + scanAreaSize),
                            end = Offset(left, top + scanAreaSize - cornerLength),
                            strokeWidth = cornerWidth
                        )
                        
                        // Esquina inferior derecha
                        drawLine(
                            color = Color(0xFF64B5F6),
                            start = Offset(left + scanAreaSize, top + scanAreaSize),
                            end = Offset(left + scanAreaSize - cornerLength, top + scanAreaSize),
                            strokeWidth = cornerWidth
                        )
                        drawLine(
                            color = Color(0xFF64B5F6),
                            start = Offset(left + scanAreaSize, top + scanAreaSize),
                            end = Offset(left + scanAreaSize, top + scanAreaSize - cornerLength),
                            strokeWidth = cornerWidth
                        )
                    }
                    
                    // Texto de instrucción
                    Text(
                        text = "Apunta la cámara hacia el código QR",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp)
                    )
                    
                    // Botón de linterna (flash)
                    FloatingActionButton(
                        onClick = {
                            flashEnabled = !flashEnabled
                            cameraControl?.enableTorch(flashEnabled)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        containerColor = if (flashEnabled) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = if (flashEnabled) "Apagar linterna" else "Encender linterna",
                            tint = if (flashEnabled) Color.White else Color.Gray
                        )
                    }
                }
            }
            cameraPermissionState.status.shouldShowRationale -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "La aplicación necesita permiso para usar la cámara y escanear códigos QR.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Conceder permiso")
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Permiso de cámara denegado. Por favor, habilítalo en la configuración.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
