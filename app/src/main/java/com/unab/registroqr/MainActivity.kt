package com.unab.registroqr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.unab.registroqr.navigation.Screen
import com.unab.registroqr.ui.screens.EditQRScreen
import com.unab.registroqr.ui.screens.MainMenuScreen
import com.unab.registroqr.ui.screens.ManualEntryScreen
import com.unab.registroqr.ui.screens.SaveQRScreen
import com.unab.registroqr.ui.screens.SavedQRsScreen
import com.unab.registroqr.ui.screens.ScanQRScreen
import com.unab.registroqr.ui.theme.RegistroQRUNABTheme
import com.unab.registroqr.viewmodel.QRViewModel

class MainActivity : ComponentActivity() {
    
    // Launcher para solicitar permiso de notificaciones
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // El permiso fue concedido o denegado
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Verificar si se lanzó desde una notificación
        handleNotificationIntent(intent)
        
        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent {
            RegistroQRUNABTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: QRViewModel = viewModel()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.MainMenu.route
                    ) {
                        composable(Screen.MainMenu.route) {
                            MainMenuScreen(navController = navController)
                        }
                        
                        composable(Screen.ScanQR.route) {
                            ScanQRScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        
                        composable(Screen.ManualEntry.route) {
                            ManualEntryScreen(
                                navController = navController
                            )
                        }
                        
                        composable(Screen.SavedQRs.route) {
                            SavedQRsScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                        
                        composable(
                            route = Screen.SaveQR.route,
                            arguments = listOf(
                                navArgument("qrUrl") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val qrUrl = backStackEntry.arguments?.getString("qrUrl") ?: ""
                            SaveQRScreen(
                                navController = navController,
                                qrUrl = qrUrl,
                                viewModel = viewModel
                            )
                        }
                        
                        composable(
                            route = Screen.EditQR.route,
                            arguments = listOf(
                                navArgument("qrId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val qrId = backStackEntry.arguments?.getString("qrId") ?: ""
                            EditQRScreen(
                                navController = navController,
                                qrId = qrId,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleNotificationIntent(it) }
    }
    
    /**
     * Maneja el Intent cuando se abre la app desde una notificación
     */
    private fun handleNotificationIntent(intent: Intent) {
        val qrLink = intent.getStringExtra("qr_link")
        if (!qrLink.isNullOrEmpty()) {
            // Abrir el link en el navegador
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(qrLink))
                startActivity(browserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
