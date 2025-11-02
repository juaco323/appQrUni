package com.unab.registroqr.ui.screenspackage com.unab.registroqr.ui.screens



import android.content.Intentimport android.content.Intent

import android.net.Uriimport android.net.Uri

import android.widget.Toastimport android.widget.Toast

import androidx.compose.foundation.ExperimentalFoundationApiimport androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.foundation.combinedClickableimport androidx.compose.foundation.combinedClickable

import androidx.compose.foundation.layout.*import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumnimport androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.itemsimport androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Iconsimport androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowBackimport androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material.icons.filled.Deleteimport androidx.compose.material.icons.filled.Delete

import androidx.compose.material.icons.filled.Editimport androidx.compose.material.icons.filled.Edit

import androidx.compose.material3.*import androidx.compose.material3.*

import androidx.compose.runtime.*import androidx.compose.runtime.*

import androidx.compose.ui.Alignmentimport androidx.compose.ui.Alignment

import androidx.compose.ui.Modifierimport androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Colorimport androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContextimport androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeightimport androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlignimport androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dpimport androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.spimport androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModelimport androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavControllerimport androidx.navigation.NavController

import com.unab.registroqr.data.SavedQRimport com.unab.registroqr.data.SavedQR

import com.unab.registroqr.navigation.Screenimport com.unab.registroqr.navigation.Screen

import com.unab.registroqr.utils.getDayInSpanishimport com.unab.registroqr.utils.getDayInSpanish

import com.unab.registroqr.viewmodel.QRViewModelimport com.unab.registroqr.viewmodel.QRViewModel

import java.time.DayOfWeekimport java.time.DayOfWeek

import java.time.LocalTimeimport java.time.LocalTime



/**/**

 * Pantalla de QRs guardados * Pantalla de QRs guardados

 */ */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

@Composable@Composable

fun SavedQRsScreen(fun SavedQRsScreen(

    navController: NavController,    navController: NavController,

    viewModel: QRViewModel = viewModel()    viewModel: QRViewModel = viewModel()

) {) {

    val context = LocalContext.current    val context = LocalContext.current

    val qrList by viewModel.qrList.collectAsState()    val qrList by viewModel.qrList.collectAsState()

        

    var isEditMode by remember { mutableStateOf(false) }    var isEditMode by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }    var showDeleteDialog by remember { mutableStateOf(false) }

    var qrToDelete by remember { mutableStateOf<SavedQR?>(null) }    var qrToDelete by remember { mutableStateOf<SavedQR?>(null) }

    var showOpenDialog by remember { mutableStateOf(false) }    var showOpenDialog by remember { mutableStateOf(false) }

    var qrToOpen by remember { mutableStateOf<SavedQR?>(null) }    var qrToOpen by remember { mutableStateOf<SavedQR?>(null) }

        

    // Lista ordenada por día de la semana    // Lista ordenada por día de la semana

    val sortedList = remember(qrList) {    val sortedList = remember(qrList) {

        qrList.sortedWith(compareBy(        qrList.sortedWith(compareBy(

            { it.notifications.firstOrNull()?.dayOfWeek ?: it.dayOfWeek ?: DayOfWeek.MONDAY },            { it.notifications.firstOrNull()?.dayOfWeek ?: it.dayOfWeek ?: DayOfWeek.MONDAY },

            { it.notifications.firstOrNull()?.classTime ?: it.classTime ?: LocalTime.MIN }            { it.notifications.firstOrNull()?.classTime ?: it.classTime ?: LocalTime.MIN }

        ))        ))

    }    }

        

    Scaffold(    Scaffold(

        topBar = {        topBar = {

            TopAppBar(            TopAppBar(

                title = { Text("QRs guardados") },                title = { Text("QRs guardados") },

                navigationIcon = {                navigationIcon = {

                    IconButton(onClick = {                     IconButton(onClick = { 

                        if (isEditMode) {                        if (isEditMode) {

                            isEditMode = false                            isEditMode = false

                        } else {                        } else {

                            navController.navigateUp()                            navController.navigateUp()

                        }                        }

                    }) {                    }) {

                        Icon(Icons.Default.ArrowBack, "Volver")                        Icon(Icons.Default.ArrowBack, "Volver")

                    }                    }

                },                },

                actions = {                actions = {

                    if (qrList.isNotEmpty()) {                    if (qrList.isNotEmpty()) {

                        TextButton(                        TextButton(

                            onClick = { isEditMode = !isEditMode }                            onClick = { isEditMode = !isEditMode }

                        ) {                        ) {

                            Text(if (isEditMode) "Listo" else "Editar")                            Text(if (isEditMode) "Listo" else "Editar")

                        }                        }

                    }                    }

                },                },

                colors = TopAppBarDefaults.topAppBarColors(                colors = TopAppBarDefaults.topAppBarColors(

                    containerColor = MaterialTheme.colorScheme.primaryContainer                    containerColor = MaterialTheme.colorScheme.primaryContainer

                )                )

            )            )

        }        }

    ) { paddingValues ->    ) { paddingValues ->

        if (qrList.isEmpty()) {        if (qrList.isEmpty()) {

            // Mensaje cuando no hay QRs guardados            // Mensaje cuando no hay QRs guardados

            Box(            Box(

                modifier = Modifier                modifier = Modifier

                    .fillMaxSize()                    .fillMaxSize()

                    .padding(paddingValues),                    .padding(paddingValues),

                contentAlignment = Alignment.Center                contentAlignment = Alignment.Center

            ) {            ) {

                Text(                Text(

                    text = "Aún no hay QRs guardados.",                    text = "Aún no hay QRs guardados.",

                    fontSize = 16.sp,                    fontSize = 16.sp,

                    color = Color.Gray,                    color = Color.Gray,

                    textAlign = TextAlign.Center                    textAlign = TextAlign.Center

                )                )

            }            }

        } else {        } else {

            // Agrupar QRs por día            // Agrupar QRs por día si se selecciona ordenamiento BY_DAY

            val groupedQRs = sortedList.groupBy { qr ->            val groupedQRs = if (selectedSortOption == SortOption.BY_DAY) {

                qr.notifications.firstOrNull()?.dayOfWeek ?: qr.dayOfWeek ?: DayOfWeek.MONDAY                reorderableList.groupBy { qr ->

            }.toList().sortedBy { it.first.value }                    qr.notifications.firstOrNull()?.dayOfWeek ?: qr.dayOfWeek ?: DayOfWeek.MONDAY

                            }.toList().sortedBy { it.first.value }

            // Lista de QRs            } else {

            LazyColumn(                emptyList()

                modifier = Modifier            }

                    .fillMaxSize()            

                    .padding(paddingValues)            // Lista de QRs

                    .padding(horizontal = 16.dp),            LazyColumn(

                verticalArrangement = Arrangement.spacedBy(12.dp),                state = reorderableState.listState,

                contentPadding = PaddingValues(vertical = 16.dp)                modifier = Modifier

            ) {                    .fillMaxSize()

                // Mostrar con headers por día                    .padding(paddingValues)

                groupedQRs.forEach { (dayOfWeek, qrsForDay) ->                    .padding(horizontal = 16.dp)

                    // Header del día                    .then(

                    item(key = "header_$dayOfWeek") {                        if (selectedSortOption == SortOption.CUSTOM) {

                        Text(                            Modifier

                            text = getDayInSpanish(dayOfWeek),                                .reorderable(reorderableState)

                            fontSize = 18.sp,                                .detectReorderAfterLongPress(reorderableState)

                            fontWeight = FontWeight.Bold,                        } else {

                            color = MaterialTheme.colorScheme.primary,                            Modifier

                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)                        }

                        )                    ),

                    }                verticalArrangement = Arrangement.spacedBy(12.dp),

                                    contentPadding = PaddingValues(vertical = 16.dp)

                    // QRs de ese día ordenados por hora            ) {

                    items(                // Mensaje informativo en modo CUSTOM

                        qrsForDay.sortedBy { it.notifications.firstOrNull()?.classTime ?: it.classTime },                if (selectedSortOption == SortOption.CUSTOM) {

                        key = { it.id }                    item(key = "info_message") {

                    ) { qr ->                        Card(

                        QRButton(                            modifier = Modifier

                            qr = qr,                                .fillMaxWidth()

                            isEditMode = isEditMode,                                .padding(bottom = 8.dp),

                            onTap = {                            colors = CardDefaults.cardColors(

                                if (!isEditMode) {                                containerColor = MaterialTheme.colorScheme.secondaryContainer

                                    qrToOpen = qr                            )

                                    showOpenDialog = true                        ) {

                                }                            Row(

                            },                                modifier = Modifier

                            onLongPress = {                                    .fillMaxWidth()

                                if (!isEditMode) {                                    .padding(12.dp),

                                    isEditMode = true                                verticalAlignment = Alignment.CenterVertically

                                }                            ) {

                            },                                Text(

                            onDelete = {                                    text = "💡",

                                qrToDelete = qr                                    fontSize = 20.sp,

                                showDeleteDialog = true                                    modifier = Modifier.padding(end = 8.dp)

                            },                                )

                            onEdit = {                                Text(

                                navController.navigate(Screen.EditQR.createRoute(qr.id))                                    text = "Mantén pulsado un botón y arrástralo para cambiar el orden",

                            }                                    fontSize = 13.sp,

                        )                                    color = MaterialTheme.colorScheme.onSecondaryContainer

                    }                                )

                }                            }

            }                        }

        }                    }

    }                }

                    

    // Diálogo de confirmación para abrir                if (selectedSortOption == SortOption.BY_DAY) {

    if (showOpenDialog && qrToOpen != null) {                    // Mostrar con headers por día

        AlertDialog(                    groupedQRs.forEach { (dayOfWeek, qrsForDay) ->

            onDismissRequest = { showOpenDialog = false },                        // Header del día

            title = { Text("Abrir QR") },                        item(key = "header_$dayOfWeek") {

            text = { Text("¿Abrir en el navegador el QR de ${qrToOpen?.name}?") },                            Text(

            confirmButton = {                                text = getDayInSpanish(dayOfWeek),

                TextButton(onClick = {                                fontSize = 18.sp,

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrToOpen?.link))                                fontWeight = FontWeight.Bold,

                    context.startActivity(intent)                                color = MaterialTheme.colorScheme.primary,

                    showOpenDialog = false                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)

                }) {                            )

                    Text("Abrir")                        }

                }                        

            },                        // QRs de ese día ordenados por hora

            dismissButton = {                        items(

                TextButton(onClick = { showOpenDialog = false }) {                            qrsForDay.sortedBy { it.notifications.firstOrNull()?.classTime ?: it.classTime },

                    Text("Cancelar")                            key = { it.id }

                }                        ) { qr ->

            }                            QRButton(

        )                                qr = qr,

    }                                isEditMode = isEditMode,

                                    isDragging = false,

    // Diálogo de confirmación para eliminar                                onTap = {

    if (showDeleteDialog && qrToDelete != null) {                                    if (!isEditMode) {

        AlertDialog(                                        qrToOpen = qr

            onDismissRequest = { showDeleteDialog = false },                                        showOpenDialog = true

            title = { Text("Eliminar QR") },                                    }

            text = { Text("¿Estás seguro de eliminar el QR de ${qrToDelete?.name}?") },                                },

            confirmButton = {                                onLongPress = {

                TextButton(                                    if (!isEditMode) {

                    onClick = {                                        isEditMode = true

                        viewModel.deleteQR(qrToDelete!!.id)                                    }

                        showDeleteDialog = false                                },

                        Toast.makeText(context, "QR eliminado", Toast.LENGTH_SHORT).show()                                onDelete = {

                    },                                    qrToDelete = qr

                    colors = ButtonDefaults.textButtonColors(                                    showDeleteDialog = true

                        contentColor = MaterialTheme.colorScheme.error                                },

                    )                                onEdit = {

                ) {                                    // Navegar a pantalla de edición completa

                    Text("Eliminar")                                    navController.navigate(Screen.EditQR.createRoute(qr.id))

                }                                }

            },                            )

            dismissButton = {                        }

                TextButton(onClick = { showDeleteDialog = false }) {                    }

                    Text("Cancelar")                } else {

                }                    // Mostrar lista normal con reordenamiento

            }                    items(reorderableList, key = { it.id }) { qr ->

        )                        ReorderableItem(reorderableState, key = qr.id) { isDragging ->

    }                            QRButton(

}                                qr = qr,

                                isEditMode = isEditMode,

@OptIn(ExperimentalFoundationApi::class)                                isDragging = isDragging,

@Composable                                onTap = {

fun QRButton(                                    if (!isEditMode) {

    qr: SavedQR,                                        qrToOpen = qr

    isEditMode: Boolean,                                        showOpenDialog = true

    onTap: () -> Unit,                                    }

    onLongPress: () -> Unit,                                },

    onDelete: () -> Unit,                                onLongPress = {

    onEdit: () -> Unit                                    // Solo activar modo edición si NO está en modo CUSTOM

) {                                    if (!isEditMode && selectedSortOption != SortOption.CUSTOM) {

    Card(                                        isEditMode = true

        modifier = Modifier                                    }

            .fillMaxWidth()                                },

            .height(70.dp)                                onDelete = {

            .combinedClickable(                                    qrToDelete = qr

                onClick = onTap,                                    showDeleteDialog = true

                onLongClick = onLongPress                                },

            ),                                onEdit = {

        colors = CardDefaults.cardColors(                                    // Navegar a pantalla de edición completa

            containerColor = qr.color.color                                    navController.navigate(Screen.EditQR.createRoute(qr.id))

        ),                                }

        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)                            )

    ) {                        }

        Box(                    }

            modifier = Modifier.fillMaxSize()                }

        ) {            }

            // Nombre del QR centrado        }

            Box(    }

                modifier = Modifier.fillMaxSize(),    

                contentAlignment = Alignment.Center    // Diálogo de confirmación para abrir

            ) {    if (showOpenDialog && qrToOpen != null) {

                Text(        AlertDialog(

                    text = qr.name,            onDismissRequest = { showOpenDialog = false },

                    fontSize = 18.sp,            title = { Text("Abrir QR") },

                    fontWeight = FontWeight.Bold,            text = { Text("¿Abrir en el navegador el QR de ${qrToOpen?.name}?") },

                    color = Color.White,            confirmButton = {

                    textAlign = TextAlign.Center,                TextButton(onClick = {

                    modifier = Modifier.padding(horizontal = 48.dp)                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrToOpen?.link))

                )                    context.startActivity(intent)

            }                    showOpenDialog = false

                            }) {

            // Botones en modo edición                    Text("Abrir")

            if (isEditMode) {                }

                // Botón de editar (esquina superior izquierda)            },

                IconButton(            dismissButton = {

                    onClick = onEdit,                TextButton(onClick = { showOpenDialog = false }) {

                    modifier = Modifier                    Text("Cancelar")

                        .align(Alignment.TopStart)                }

                        .padding(8.dp)            }

                        .size(32.dp)        )

                ) {    }

                    Surface(    

                        shape = MaterialTheme.shapes.small,    // Diálogo de confirmación para eliminar

                        color = MaterialTheme.colorScheme.primary,    if (showDeleteDialog && qrToDelete != null) {

                        modifier = Modifier.size(28.dp)        AlertDialog(

                    ) {            onDismissRequest = { showDeleteDialog = false },

                        Box(            title = { Text("Eliminar QR") },

                            contentAlignment = Alignment.Center,            text = { Text("¿Estás seguro de eliminar el QR de ${qrToDelete?.name}?") },

                            modifier = Modifier.fillMaxSize()            confirmButton = {

                        ) {                TextButton(

                            Icon(                    onClick = {

                                imageVector = Icons.Default.Edit,                        viewModel.deleteQR(qrToDelete!!.id)

                                contentDescription = "Editar",                        showDeleteDialog = false

                                tint = Color.White,                        Toast.makeText(context, "QR eliminado", Toast.LENGTH_SHORT).show()

                                modifier = Modifier.size(16.dp)                    },

                            )                    colors = ButtonDefaults.textButtonColors(

                        }                        contentColor = MaterialTheme.colorScheme.error

                    }                    )

                }                ) {

                                    Text("Eliminar")

                // Botón de eliminar (esquina superior derecha)                }

                IconButton(            },

                    onClick = onDelete,            dismissButton = {

                    modifier = Modifier                TextButton(onClick = { showDeleteDialog = false }) {

                        .align(Alignment.TopEnd)                    Text("Cancelar")

                        .padding(8.dp)                }

                        .size(32.dp)            }

                ) {        )

                    Surface(    }

                        shape = MaterialTheme.shapes.small,}

                        color = Color.Red,

                        modifier = Modifier.size(28.dp)@OptIn(ExperimentalFoundationApi::class)

                    ) {@Composable

                        Box(fun QRButton(

                            contentAlignment = Alignment.Center,    qr: SavedQR,

                            modifier = Modifier.fillMaxSize()    isEditMode: Boolean,

                        ) {    isDragging: Boolean,

                            Icon(    onTap: () -> Unit,

                                imageVector = Icons.Default.Delete,    onLongPress: () -> Unit,

                                contentDescription = "Eliminar",    onDelete: () -> Unit,

                                tint = Color.White,    onEdit: () -> Unit

                                modifier = Modifier.size(16.dp)) {

                            )    Card(

                        }        modifier = Modifier

                    }            .fillMaxWidth()

                }            .height(70.dp)

            }            .combinedClickable(

        }                onClick = onTap,

    }                onLongClick = onLongPress

}            )

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
