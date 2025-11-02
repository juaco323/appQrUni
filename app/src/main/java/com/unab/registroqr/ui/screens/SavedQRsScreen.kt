package com.unab.registroqr.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unab.registroqr.data.SavedQR
import com.unab.registroqr.navigation.Screen
import com.unab.registroqr.utils.getDayInSpanish
import com.unab.registroqr.viewmodel.QRViewModel
import org.burnoutcrew.reorderable.*
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Opciones de ordenamiento para los QRs guardados
 */
enum class SortOption(val displayName: String) {
    BY_DAY("Por día de la semana"),
    CUSTOM("Personalizado")
}

/**
 * Pantalla de QRs guardados
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SavedQRsScreen(
    navController: NavController,
    viewModel: QRViewModel = viewModel()
) {
    val context = LocalContext.current
    val qrList by viewModel.qrList.collectAsState()
    
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var qrToDelete by remember { mutableStateOf<SavedQR?>(null) }
    var showOpenDialog by remember { mutableStateOf(false) }
    var qrToOpen by remember { mutableStateOf<SavedQR?>(null) }
    
    // Estado para ordenamiento
    var selectedSortOption by remember { mutableStateOf(SortOption.BY_DAY) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Estado para reordenamiento
    var reorderableList by remember { mutableStateOf(qrList) }
    
    LaunchedEffect(qrList, selectedSortOption) {
        reorderableList = when (selectedSortOption) {
            SortOption.BY_DAY -> {
                qrList.sortedWith(compareBy(
                    { it.notifications.firstOrNull()?.dayOfWeek ?: it.dayOfWeek ?: DayOfWeek.MONDAY },
                    { it.notifications.firstOrNull()?.classTime ?: it.classTime ?: LocalTime.MIN }
                ))
            }
            SortOption.CUSTOM -> qrList.sortedBy { it.position }
        }
    }
    
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            if (selectedSortOption == SortOption.CUSTOM) {
                reorderableList = reorderableList.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                }
            }
        },
        onDragEnd = { _, _ ->
            if (selectedSortOption == SortOption.CUSTOM) {
                viewModel.updateQROrder(reorderableList)
            }
        }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QRs guardados") },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (isEditMode) {
                            isEditMode = false
                        } else {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (qrList.isNotEmpty()) {
                        // Botón de editar/listo (solo si no está en modo CUSTOM)
                        if (selectedSortOption != SortOption.CUSTOM) {
                            TextButton(
                                onClick = { isEditMode = !isEditMode }
                            ) {
                                Text(if (isEditMode) "Listo" else "Editar")
                            }
                        }
                        
                        // Botón de ordenar
                        Box {
                            TextButton(
                                onClick = { showSortMenu = true }
                            ) {
                                Text("Ordenar")
                            }
                            
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(option.displayName)
                                                if (option == selectedSortOption) {
                                                    Spacer(Modifier.width(8.dp))
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedSortOption = option
                                            showSortMenu = false
                                            if (option != SortOption.CUSTOM) {
                                                isEditMode = false
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (qrList.isEmpty()) {
            // Mensaje cuando no hay QRs guardados
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay QRs guardados.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Agrupar QRs por día si se selecciona ordenamiento BY_DAY
            val groupedQRs = if (selectedSortOption == SortOption.BY_DAY) {
                reorderableList.groupBy { qr ->
                    qr.notifications.firstOrNull()?.dayOfWeek ?: qr.dayOfWeek ?: DayOfWeek.MONDAY
                }.toList().sortedBy { it.first.value }
            } else {
                emptyList()
            }
            
            // Lista de QRs
            LazyColumn(
                state = reorderableState.listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .then(
                        if (selectedSortOption == SortOption.CUSTOM) {
                            Modifier
                                .reorderable(reorderableState)
                                .detectReorderAfterLongPress(reorderableState)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Mensaje informativo en modo CUSTOM
                if (selectedSortOption == SortOption.CUSTOM) {
                    item(key = "info_message") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Mantén pulsado un botón y arrástralo para cambiar el orden",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
                
                if (selectedSortOption == SortOption.BY_DAY) {
                    // Mostrar con headers por día
                    groupedQRs.forEach { (dayOfWeek, qrsForDay) ->
                        // Header del día
                        item(key = "header_$dayOfWeek") {
                            Text(
                                text = getDayInSpanish(dayOfWeek),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                        
                        // QRs de ese día ordenados por hora
                        items(
                            qrsForDay.sortedBy { it.notifications.firstOrNull()?.classTime ?: it.classTime },
                            key = { it.id }
                        ) { qr ->
                            QRButton(
                                qr = qr,
                                isEditMode = isEditMode,
                                isDragging = false,
                                onTap = {
                                    if (!isEditMode) {
                                        qrToOpen = qr
                                        showOpenDialog = true
                                    }
                                },
                                onLongPress = {
                                    if (!isEditMode) {
                                        isEditMode = true
                                    }
                                },
                                onDelete = {
                                    qrToDelete = qr
                                    showDeleteDialog = true
                                },
                                onEdit = {
                                    // Navegar a pantalla de edición completa
                                    navController.navigate(Screen.EditQR.createRoute(qr.id))
                                }
                            )
                        }
                    }
                } else {
                    // Mostrar lista normal con reordenamiento
                    items(reorderableList, key = { it.id }) { qr ->
                        ReorderableItem(reorderableState, key = qr.id) { isDragging ->
                            QRButton(
                                qr = qr,
                                isEditMode = isEditMode,
                                isDragging = isDragging,
                                onTap = {
                                    if (!isEditMode) {
                                        qrToOpen = qr
                                        showOpenDialog = true
                                    }
                                },
                                onLongPress = {
                                    // Solo activar modo edición si NO está en modo CUSTOM
                                    if (!isEditMode && selectedSortOption != SortOption.CUSTOM) {
                                        isEditMode = true
                                    }
                                },
                                onDelete = {
                                    qrToDelete = qr
                                    showDeleteDialog = true
                                },
                                onEdit = {
                                    // Navegar a pantalla de edición completa
                                    navController.navigate(Screen.EditQR.createRoute(qr.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de confirmación para abrir
    if (showOpenDialog && qrToOpen != null) {
        AlertDialog(
            onDismissRequest = { showOpenDialog = false },
            title = { Text("Abrir QR") },
            text = { Text("¿Abrir en el navegador el QR de ${qrToOpen?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrToOpen?.link))
                    context.startActivity(intent)
                    showOpenDialog = false
                }) {
                    Text("Abrir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && qrToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar QR") },
            text = { Text("¿Estás seguro de eliminar el QR de ${qrToDelete?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteQR(qrToDelete!!.id)
                        showDeleteDialog = false
                        Toast.makeText(context, "QR eliminado", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QRButton(
    qr: SavedQR,
    isEditMode: Boolean,
    isDragging: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .then(
                if (isDragging) {
                    Modifier.graphicsLayer {
                        scaleX = 1.05f
                        scaleY = 1.05f
                        alpha = 0.9f
                    }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = qr.color.color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 12.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Nombre del QR centrado
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = qr.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
            }
            
            // Indicador de drag en modo arrastre (cuando isDragging es posible)
            if (!isEditMode && isDragging) {
                // Mostrar indicador de que se está arrastrando
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = "⋮⋮",
                        fontSize = 24.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Botones en modo edición
            if (isEditMode) {
                // Botón de editar (esquina superior izquierda)
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar nombre",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // Botón de eliminar (esquina superior derecha)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color.Red,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
