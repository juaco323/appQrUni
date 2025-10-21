# Registro QR UNAB

Una aplicación Android para escanear y guardar códigos QR de asistencia de clases de la Universidad Andrés Bello.

## 🎯 Características

- **Escaneo de QR**: Escanea códigos QR de asistencia de clase utilizando la cámara del dispositivo
- **Validación**: Solo acepta QRs que comiencen con `https://registroasistenciaqr.unab.cl/`
- **Almacenamiento local**: Guarda hasta 20 QRs con nombres personalizados y colores
- **Acceso rápido**: Crea botones de acceso rápido para abrir los enlaces en el navegador
- **Personalización**: Elige entre 16 colores diferentes para cada botón
- **Organización**: Reordena tus QRs guardados mediante arrastrar y soltar
- **Gestión**: Elimina QRs guardados cuando ya no los necesites

## 🛠️ Tecnologías utilizadas

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Navegación**: Navigation Compose
- **Cámara**: CameraX
- **Escaneo QR**: ML Kit Barcode Scanning
- **Persistencia**: SharedPreferences con Gson
- **Permisos**: Accompanist Permissions
- **Reordenamiento**: Compose Reorderable

## 📋 Requisitos

- Android Studio Hedgehog o superior
- Android SDK 24 o superior (Android 7.0+)
- Dispositivo físico o emulador con cámara

## 🚀 Instalación

1. Clona o descarga este repositorio
2. Abre el proyecto en Android Studio
3. Espera a que Gradle sincronice las dependencias
4. Conecta un dispositivo Android o inicia un emulador
5. Ejecuta la aplicación (Shift + F10)

## 📱 Uso

1. **Pantalla principal**: 
   - Toca "Escanear QR" para abrir la cámara
   - Toca "QRs guardados" para ver tus QRs almacenados

2. **Escanear QR**:
   - Apunta la cámara al código QR
   - Toca el botón de cámara para capturar
   - Si el QR es válido, serás llevado a la pantalla de guardado

3. **Guardar QR**:
   - Ingresa un nombre para el botón (máx. 30 caracteres)
   - Selecciona un color de la paleta
   - Toca "Guardar" para confirmar o "Cancelar" para volver

4. **QRs guardados**:
   - Toca un botón para abrir el enlace en el navegador
   - Mantén presionado para entrar en modo edición
   - En modo edición: arrastra para reordenar, toca el icono de basura para eliminar

## 🎨 Paleta de colores

La aplicación ofrece 16 colores vibrantes para personalizar tus botones:
- Rojo, Rosa, Púrpura, Púrpura oscuro
- Índigo, Azul, Azul claro, Cian
- Turquesa, Verde, Verde claro, Lima
- Amarillo, Ámbar, Naranja, Naranja oscuro

## 🔒 Permisos

- **Cámara**: Necesario para escanear códigos QR
- **Internet**: Necesario para abrir los enlaces en el navegador

## 📦 Estructura del proyecto

```
app/src/main/java/com/unab/registroqr/
├── data/
│   ├── SavedQR.kt          # Modelo de datos
│   └── QRRepository.kt     # Repositorio para persistencia
├── ui/
│   ├── screens/
│   │   ├── MainMenuScreen.kt    # Pantalla principal
│   │   ├── ScanQRScreen.kt      # Pantalla de escaneo
│   │   ├── SaveQRScreen.kt      # Pantalla de guardado
│   │   └── SavedQRsScreen.kt    # Pantalla de QRs guardados
│   └── theme/              # Tema y colores
├── utils/
│   └── QRScanner.kt        # Utilidades de escaneo
├── viewmodel/
│   └── QRViewModel.kt      # ViewModel principal
├── navigation/
│   └── Navigation.kt       # Rutas de navegación
└── MainActivity.kt         # Actividad principal
```

## 🔄 Flujo de la aplicación

```
Menú Principal
    │
    ├─→ Escanear QR
    │       │
    │       ├─→ QR válido → Guardar QR → QRs guardados
    │       └─→ QR inválido → Mensaje de error
    │
    └─→ QRs guardados
            │
            ├─→ Tocar → Abrir en navegador
            └─→ Mantener → Modo edición (reordenar/eliminar)
```

## 🎯 Limitaciones

- Máximo 20 QRs guardados
- Solo acepta QRs de `https://registroasistenciaqr.unab.cl/`
- No se permiten duplicados

## 👨‍💻 Créditos

- **Idea**: Trickster
- **Desarrollo**: Asistido principalmente por IA

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

## 🐛 Reporte de errores

Si encuentras algún error o tienes sugerencias, por favor crea un issue en el repositorio.

## 🔮 Futuras mejoras

- Escaneo automático (sin botón de captura)
- Modo oscuro/claro configurable
- Exportar/importar QRs guardados
- Estadísticas de uso
- Widget de acceso rápido
- Búsqueda de QRs guardados
