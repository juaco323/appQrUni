package com.unab.registroqr.navigation

/**
 * Definición de rutas de navegación
 */
sealed class Screen(val route: String) {
    object MainMenu : Screen("main_menu")
    object ScanQR : Screen("scan_qr")
    object ManualEntry : Screen("manual_entry")
    object SavedQRs : Screen("saved_qrs")
    object SaveQR : Screen("save_qr/{qrUrl}") {
        fun createRoute(qrUrl: String) = "save_qr/$qrUrl"
    }
}
