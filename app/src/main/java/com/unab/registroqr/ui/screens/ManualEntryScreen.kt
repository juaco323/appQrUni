package com.unab.registroqr.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unab.registroqr.navigation.Screen
import com.unab.registroqr.utils.QRScanner

/**
 * Pantalla para ingresar links manualmente
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ManualEntryScreen(
    navController: NavController,
    viewModel: com.unab.registroqr.viewmodel.QRViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var linkText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    val handleContinue = {
        keyboardController?.hide()
        val trimmedLink = linkText.trim()
        
        when {
            trimmedLink.isEmpty() -> {
                showError = true
                errorMessage = "Por favor ingresa un link"
            }
            !QRScanner.isValidUNABQR(trimmedLink) -> {
                showError = true
                errorMessage = "El link debe comenzar con:\nhttps://registroasistenciaqr.unab.cl"
            }
            viewModel.linkExists(trimmedLink) -> {
                showError = true
                errorMessage = "Este QR ya está guardado.\nNo se permiten URLs duplicadas."
            }
            else -> {
                isProcessing = true
                try {
                    // Link válido, navegar a la pantalla de guardado
                    val encodedUrl = Uri.encode(trimmedLink)
                    navController.navigate(Screen.SaveQR.createRoute(encodedUrl))
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Error al procesar el link: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    isProcessing = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingresar link manualmente") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ingresa el link del código QR de asistencia",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            OutlinedTextField(
                value = linkText,
                onValueChange = { 
                    linkText = it
                    showError = false
                },
                label = { Text("Link del QR") },
                placeholder = { Text("Ej: https://registroasistenciaqr.unab.cl?validate=U2FsdGVk...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { handleContinue() }
                ),
                singleLine = true,
                isError = showError,
                enabled = !isProcessing
            )
            
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Button(
                onClick = handleContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Continuar",
                        fontSize = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isProcessing
            ) {
                Text(
                    text = "Cancelar",
                    fontSize = 18.sp
                )
            }
        }
    }
}
