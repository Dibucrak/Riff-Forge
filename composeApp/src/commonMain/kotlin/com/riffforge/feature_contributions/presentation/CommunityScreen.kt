package com.riffforge.feature_contributions.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.riffforge.LocalAuthManager
import com.riffforge.LocalCloudSyncManager
import com.riffforge.core.domain.store.CommunityPostModel
import com.riffforge.core.domain.store.RepertoireStore
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import com.riffforge.core.presentation.components.ProfileAvatar
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.core.presentation.theme.SystemRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(
    onNavigateUp: () -> Unit = {}
) {
    val authManager = LocalAuthManager.current
    val syncManager = LocalCloudSyncManager.current
    val currentUser by authManager.currentUser.collectAsState(initial = null)
    val myUid = currentUser?.uid.orEmpty()
    val myAuthorHandle = currentUser?.username?.takeIf { it.isNotBlank() }
        ?: currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "riffforge_user"

    val useSpanish = AppSettings.useSpanishNotation
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showShareDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var contributionPendingDelete by remember { mutableStateOf<CommunityPostModel?>(null) }

    suspend fun refreshContributions() {
        isLoading = true
        loadError = null
        val result = syncManager.fetchContributions()
        isLoading = false
        result.fold(
            onSuccess = { items ->
                RepertoireStore.setContributions(items)
            },
            onFailure = { err ->
                loadError = err.message ?: "No se pudieron cargar los aportes de Firestore."
            }
        )
    }

    LaunchedEffect(Unit) {
        refreshContributions()
    }

    val allContributions = RepertoireStore.contributions
    val approvedContributions = allContributions.filter { it.status == "APPROVED" }
    val myContributions = allContributions.filter { item ->
        (myUid.isNotBlank() && item.authorId == myUid) ||
            (item.authorId.isBlank() && item.authorUsername.equals(myAuthorHandle, ignoreCase = true))
    }

    val activeList = if (selectedTab == 0) approvedContributions else myContributions
    val filteredContributions = remember(activeList, searchQuery) {
        if (searchQuery.isBlank()) {
            activeList
        } else {
            activeList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.authorUsername.contains(searchQuery, ignoreCase = true) ||
                    it.key.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val userSongs = RepertoireStore.songs

    contributionPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { contributionPendingDelete = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Eliminar aporte?", fontWeight = FontWeight.Bold) },
            text = { Text("Se eliminará permanentemente \"${target.title}\" de la comunidad.") },
            confirmButton = {
                Button(
                    onClick = {
                        val deleteId = target.id
                        contributionPendingDelete = null
                        scope.launch {
                            val res = syncManager.deleteContribution(deleteId)
                            if (res.isSuccess) {
                                RepertoireStore.deleteContribution(deleteId)
                                snackbarHostState.showSnackbar("Aporte eliminado.")
                            } else {
                                snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Error al eliminar aporte.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contributionPendingDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showShareDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Compartir en la Comunidad",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Selecciona una canción de tu biblioteca. Pasará por revisión de un administrador antes de publicarse en la Comunidad:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (userSongs.isEmpty()) {
                        Text(
                            text = "No tienes canciones en tu repertorio local todavía.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(userSongs, key = { it.id }) { song ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable(enabled = !isSubmitting) {
                                            if (myUid.isBlank()) {
                                                showShareDialog = false
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Debes iniciar sesión para compartir.")
                                                }
                                                return@clickable
                                            }
                                            isSubmitting = true
                                            scope.launch {
                                                val res = syncManager.publishContribution(song, myUid, myAuthorHandle)
                                                isSubmitting = false
                                                showShareDialog = false
                                                if (res.isSuccess) {
                                                    selectedTab = 1
                                                    refreshContributions()
                                                    snackbarHostState.showSnackbar("'${song.title}' enviada a revisión. Un administrador debe aprobarla para publicarse.")
                                                } else {
                                                    snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Error al compartir canción.")
                                                }
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${song.artist} · ${NotationConverter.formatChord(song.key, useSpanish)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSubmitting) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Publicar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !isSubmitting, onClick = { showShareDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Comunidad Riff Forge", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Explora e importa canciones de otros músicos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showShareDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Compartir Canción", fontWeight = FontWeight.Bold) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // ── Segmented Tabs: Comunidad | Mis Aportes ──────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf(
                            "Comunidad",
                            if (myContributions.isNotEmpty()) "Mis Aportes (${myContributions.size})" else "Mis Aportes"
                        )
                        tabs.forEachIndexed { idx, label ->
                            val selected = selectedTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                    )
                                    .clickable { selectedTab = idx }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── Search Bar ──────────────────────────────────
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por canción, artista o autor...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        loadError != null && filteredContributions.isEmpty() -> {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = SystemRed,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No se pudo conectar con Comunidad",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = loadError.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { scope.launch { refreshContributions() } },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Reintentar", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        filteredContributions.isEmpty() -> {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = when {
                                            searchQuery.isNotBlank() -> "No hay coincidencias para \"$searchQuery\""
                                            selectedTab == 1 -> "Aún no has enviado aportes"
                                            else -> "Aún no hay aportes aprobados en la comunidad"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (selectedTab == 1)
                                            "Cuando compartas una canción aparecerá aquí como Pendiente hasta que un administrador la apruebe."
                                        else
                                            "Usa \"Compartir Canción\" para enviar un riff o canción a revisión.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 96.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredContributions, key = { it.id }) { contribution ->
                                    val imported = RepertoireStore.isContributionImported(contribution)
                                    val isMine = myUid.isNotBlank() && contribution.authorId == myUid
                                    val displayPhotoData = if (isMine && !currentUser?.photoData.isNullOrBlank()) {
                                        currentUser?.photoData.orEmpty()
                                    } else {
                                        contribution.authorPhotoData
                                    }
                                    val displayPhotoUrl = if (isMine && !currentUser?.photoUrl.isNullOrBlank()) {
                                        currentUser?.photoUrl.orEmpty()
                                    } else {
                                        contribution.authorPhotoUrl
                                    }
                                    CommunitySongCard(
                                        contribution = contribution,
                                        authorPhotoUrl = displayPhotoUrl,
                                        authorPhotoData = displayPhotoData,
                                        useSpanish = useSpanish,
                                        isMyContributionTab = selectedTab == 1,
                                        isImported = imported,
                                        onDelete = { contributionPendingDelete = contribution },
                                        onImport = {
                                            val added = RepertoireStore.importContributionToRepertoire(contribution)
                                            scope.launch {
                                                if (added) {
                                                    snackbarHostState.showSnackbar("'${contribution.title}' importada a Mi Repertorio")
                                                } else {
                                                    snackbarHostState.showSnackbar("'${contribution.title}' ya está en Mi Repertorio")
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommunitySongCard(
    contribution: CommunityPostModel,
    authorPhotoUrl: String,
    authorPhotoData: String,
    useSpanish: Boolean,
    isMyContributionTab: Boolean,
    isImported: Boolean,
    onDelete: () -> Unit,
    onImport: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val displayKey = remember(contribution.key, useSpanish) {
        NotationConverter.formatChord(contribution.key, useSpanish)
    }

    val previewText = remember(contribution.lyricsAndChords, useSpanish) {
        val stripped = contribution.lyricsAndChords.replace(Regex("\\[(.*?)\\]")) { it.groupValues[1] }
        NotationConverter.convertLyricsText(stripped, useSpanish)
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contribution.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = contribution.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isMyContributionTab) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isApproved = contribution.status == "APPROVED"
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isApproved) SystemGreen.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isApproved) Icons.Default.Check else Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = if (isApproved) SystemGreen else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isApproved) "Aprobada" else "Pendiente",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isApproved) SystemGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SystemRed.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar aporte",
                                tint = SystemRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = onImport,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isImported) SystemGreen.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                    ) {
                        Icon(
                            imageVector = if (isImported) Icons.Default.Check else Icons.Default.Download,
                            contentDescription = if (isImported) "Importada" else "Importar a mi repertorio",
                            tint = if (isImported) SystemGreen else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips Row (wrapping cleanly)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetadataChip(
                    icon = Icons.Default.MusicNote,
                    label = "Tono: $displayKey"
                )
                MetadataChip(
                    icon = Icons.Default.Speed,
                    label = "${contribution.bpm} BPM"
                )
                if (contribution.tuning.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = contribution.tuning,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded && previewText.isNotBlank()) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = previewText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileAvatar(
                        photoData = authorPhotoData,
                        photoUrl = authorPhotoUrl,
                        size = 22.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Por ${contribution.authorUsername}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Ver acordes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MetadataChip(
    icon: ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
