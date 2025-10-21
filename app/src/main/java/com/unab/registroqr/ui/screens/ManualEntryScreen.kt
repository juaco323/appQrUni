package com.unab.registroqr.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unab.registroqr.navigation.Screen
import com.unab.registroqr.utils.QRScanner

/**
 * Pantalla para ingresar links manualmente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    navController: NavController
) {
    var linkText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
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
                placeholder = { Text("https://registroasistenciaqr.unab.cl/...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri
                ),
                singleLine = true,
                isError = showError
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
                onClick = {
                    val trimmedLink = linkText.trim()
                    
                    when {
                        trimmedLink.isEmpty() -> {
                            showError = true
                            errorMessage = "Por favor ingresa un link"
                        }
                        !QRScanner.isValidUNABQR(trimmedLink) -> {
                            showError = true
                            errorMessage = "El link debe comenzar con:\nhttps://registroasistenciaqr.unab.cl/"
                        }
                        else -> {
                            // Link válido, navegar a la pantalla de guardado
                            val encodedUrl = Uri.encode(trimmedLink)
                            navController.navigate(Screen.SaveQR.createRoute(encodedUrl))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Continuar",
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Cancelar",
                    fontSize = 18.sp
                )
            }
        }
    }
}
