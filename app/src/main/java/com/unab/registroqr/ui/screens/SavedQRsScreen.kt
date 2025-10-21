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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unab.registroqr.data.SavedQR
import com.unab.registroqr.viewmodel.QRViewModel
import org.burnoutcrew.reorderable.*

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
    
    // Estado para reordenamiento
    var reorderableList by remember { mutableStateOf(qrList) }
    
    LaunchedEffect(qrList) {
        reorderableList = qrList
    }
    
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            reorderableList = reorderableList.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }
        },
        onDragEnd = { _, _ ->
            viewModel.updateQROrder(reorderableList)
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
                        TextButton(
                            onClick = { isEditMode = !isEditMode }
                        ) {
                            Text(if (isEditMode) "Listo" else "Editar")
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
            // Lista de QRs
            LazyColumn(
                state = reorderableState.listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .reorderable(reorderableState)
                    .detectReorderAfterLongPress(reorderableState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
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
                                if (!isEditMode) {
                                    isEditMode = true
                                }
                            },
                            onDelete = {
                                qrToDelete = qr
                                showDeleteDialog = true
                            }
                        )
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
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            ),
        colors = CardDefaults.cardColors(
            containerColor = qr.color.color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
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
            
            // Botón de eliminar (solo en modo edición)
            if (isEditMode) {
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
