package com.riffforge.feature_admin.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.riffforge.LocalAuthManager
import com.riffforge.LocalCloudSyncManager
import com.riffforge.core.domain.store.AdminUserModel
import com.riffforge.core.domain.store.CommunityPostModel
import com.riffforge.core.domain.store.RepertoireStore
import com.riffforge.core.domain.util.AppSettings
import com.riffforge.core.domain.util.NotationConverter
import com.riffforge.core.presentation.components.ProfileAvatar
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.core.presentation.theme.SystemRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminScreen(
    onNavigateUp: () -> Unit = {}
) {
    val authManager = LocalAuthManager.current
    val syncManager = LocalCloudSyncManager.current
    val currentUser by authManager.currentUser.collectAsState(initial = null)
    val currentUserUid = currentUser?.uid.orEmpty()
    val useSpanish = AppSettings.useSpanishNotation
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Aportes", "Usuarios")
    var isLoading by remember { mutableStateOf(true) }
    var expandedIds by remember { mutableStateOf(setOf<String>()) }

    val contributions = RepertoireStore.contributions
    val pendingContributions = contributions.filter { it.status != "APPROVED" }
    val users = RepertoireStore.users
    val usersById = remember(users.toList()) { users.associateBy { it.uid } }

    var userPendingDelete by remember { mutableStateOf<AdminUserModel?>(null) }
    var pendingAdminToggle by remember { mutableStateOf<Pair<AdminUserModel, Boolean>?>(null) }
    var inspectedUser by remember { mutableStateOf<AdminUserModel?>(null) }
    var contributionPendingReject by remember { mutableStateOf<CommunityPostModel?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        val usersRes = syncManager.fetchUsers()
        if (usersRes.isSuccess) {
            RepertoireStore.setUsers(usersRes.getOrDefault(emptyList()))
        } else {
            snackbarHostState.showSnackbar(usersRes.exceptionOrNull()?.message ?: "Error al cargar usuarios")
        }
        val contribRes = syncManager.fetchContributions()
        if (contribRes.isSuccess) {
            RepertoireStore.setContributions(contribRes.getOrDefault(emptyList()))
        } else {
            snackbarHostState.showSnackbar(contribRes.exceptionOrNull()?.message ?: "Error al cargar aportes")
        }
        isLoading = false
    }

    // 3-Second Confirmation Dialog for Granting / Revoking Admin
    pendingAdminToggle?.let { (targetUser, grantAdmin) ->
        var countdown by remember(targetUser.uid, grantAdmin) { mutableStateOf(3) }
        LaunchedEffect(targetUser.uid, grantAdmin) {
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
        }
        AlertDialog(
            onDismissRequest = { pendingAdminToggle = null },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (grantAdmin) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (grantAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            },
            title = {
                Text(
                    text = if (grantAdmin) "¿Dar permisos de Admin?" else "¿Quitar permisos de Admin?",
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(
                    text = if (grantAdmin) {
                        "Vas a otorgar permisos de administrador a @${targetUser.username} (${targetUser.email}). Podrá aprobar/rechazar aportes y gestionar usuarios."
                    } else {
                        "Vas a quitar los permisos de administrador a @${targetUser.username} (${targetUser.email}). Perderá el acceso al Panel de Administración."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingAdminToggle = null
                        scope.launch {
                            val res = syncManager.updateUserRole(targetUser.uid, grantAdmin)
                            if (res.isSuccess) {
                                RepertoireStore.updateUserRole(targetUser.uid, if (grantAdmin) "ADMIN" else "USER")
                                snackbarHostState.showSnackbar(
                                    if (grantAdmin) "Permiso de administrador otorgado."
                                    else "Permiso de administrador revocado."
                                )
                            } else {
                                snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Error al actualizar rol.")
                            }
                        }
                    },
                    enabled = countdown == 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (grantAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        contentColor = if (grantAdmin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
                        disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = if (countdown > 0) "Confirmar (${countdown}s)" else if (grantAdmin) "Dar Admin" else "Quitar Admin",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAdminToggle = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    contributionPendingReject?.let { target ->
        AlertDialog(
            onDismissRequest = { contributionPendingReject = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Rechazar aporte?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Se rechazará y eliminará \"${target.title}\" (${target.artist}) de Firestore de forma permanente.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idToReject = target.id
                        contributionPendingReject = null
                        scope.launch {
                            val res = syncManager.deleteContribution(idToReject)
                            if (res.isSuccess) {
                                RepertoireStore.deleteContribution(idToReject)
                                snackbarHostState.showSnackbar("Aporte rechazado y eliminado.")
                            } else {
                                snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Error al rechazar aporte.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Rechazar y Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contributionPendingReject = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    inspectedUser?.let { targetUser ->
        val userPosts = contributions.filter { item ->
            item.authorId == targetUser.uid ||
                (item.authorId.isBlank() && item.authorUsername.equals(targetUser.username, ignoreCase = true))
        }
        AlertDialog(
            onDismissRequest = { inspectedUser = null },
            shape = RoundedCornerShape(24.dp),
            title = {
                Column {
                    Text("Aportes de @${targetUser.username}", fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = targetUser.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                if (userPosts.isEmpty()) {
                    Text("Este usuario aún no ha enviado aportes a la comunidad.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(userPosts, key = { it.id }) { post ->
                            val isApproved = post.status == "APPROVED"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(post.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                            Text(
                                                "${post.artist} · ${NotationConverter.formatChord(post.key, useSpanish)} · ${post.bpm} BPM",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isApproved) SystemGreen.copy(alpha = 0.16f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isApproved) Icons.Default.Check else Icons.Default.HourglassTop,
                                                    contentDescription = null,
                                                    tint = if (isApproved) SystemGreen else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
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
                                    }
                                    if (!isApproved) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { contributionPendingReject = post }) {
                                                Text("Rechazar", color = SystemRed, fontWeight = FontWeight.SemiBold)
                                            }
                                            TextButton(
                                                onClick = {
                                                    scope.launch {
                                                        val res = syncManager.approveContribution(post.id)
                                                        if (res.isSuccess) {
                                                            RepertoireStore.updateContributionStatus(post.id, "APPROVED")
                                                            snackbarHostState.showSnackbar("Aporte aprobado y publicado.")
                                                        } else {
                                                            snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Error al aprobar.")
                                                        }
                                                    }
                                                }
                                            ) {
                                                Text("Aprobar", color = SystemGreen, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedUser = null }) { Text("Cerrar") }
            }
        )
    }

    if (userPendingDelete != null) {
        val target = userPendingDelete!!
        AlertDialog(
            onDismissRequest = { userPendingDelete = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Eliminar Usuario", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Estás seguro de que deseas eliminar permanentemente los datos de @${target.username}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        userPendingDelete = null
                        scope.launch {
                            val res = syncManager.deleteUser(target.uid)
                            if (res.isSuccess) {
                                RepertoireStore.deleteUser(target.uid)
                                snackbarHostState.showSnackbar("Usuario eliminado.")
                            } else {
                                snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Error al eliminar usuario.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { userPendingDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Panel de Administración",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Segmented Tabs
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        if (pendingContributions.isEmpty()) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp, horizontal = 24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Bandeja limpia",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "No hay aportes pendientes de revisión.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 84.dp)
                            ) {
                                items(pendingContributions, key = { it.id }) { item ->
                                    val isExpanded = item.id in expandedIds
                                    val displayKey = NotationConverter.formatChord(item.key.ifBlank { "C" }, useSpanish)
                                    val previewContent = remember(item.lyricsAndChords, useSpanish) {
                                        val stripped = item.lyricsAndChords.replace(Regex("\\[(.*?)\\]")) { it.groupValues[1] }
                                        NotationConverter.convertLyricsText(stripped, useSpanish)
                                    }

                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedIds = if (isExpanded) expandedIds - item.id else expandedIds + item.id
                                            },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    val author = usersById[item.authorId]
                                                    Text(
                                                        text = item.title,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        ProfileAvatar(
                                                            photoData = author?.photoData?.ifBlank { item.authorPhotoData } ?: item.authorPhotoData,
                                                            photoUrl = author?.photoUrl?.ifBlank { item.authorPhotoUrl } ?: item.authorPhotoUrl,
                                                            size = 20.dp
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "${item.artist} · Por @${item.authorUsername}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                    contentDescription = "Revisar contenido",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        text = "Tono: $displayKey",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surface
                                                ) {
                                                    Text(
                                                        text = "${item.bpm} BPM",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                if (item.tuning.isNotBlank()) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.surface
                                                    ) {
                                                        Text(
                                                            text = item.tuning,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            AnimatedVisibility(visible = isExpanded && previewContent.isNotBlank()) {
                                                Column {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Surface(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = MaterialTheme.colorScheme.surface
                                                    ) {
                                                        Text(
                                                            text = previewContent,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.padding(12.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                IconButton(
                                                    onClick = { contributionPendingReject = item },
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(SystemRed.copy(alpha = 0.15f))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Rechazar",
                                                        tint = SystemRed
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            val res = syncManager.approveContribution(item.id)
                                                            if (res.isSuccess) {
                                                                RepertoireStore.updateContributionStatus(item.id, "APPROVED")
                                                                snackbarHostState.showSnackbar("Aporte aprobado y publicado.")
                                                            } else {
                                                                snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Error al aprobar.")
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(SystemGreen.copy(alpha = 0.15f))
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Aprobar",
                                                        tint = SystemGreen
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        if (users.isEmpty()) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp, horizontal = 24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Sin usuarios",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 84.dp)
                            ) {
                                items(users, key = { it.uid }) { user ->
                                    val isSelf = currentUserUid.isNotBlank() && user.uid == currentUserUid
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ProfileAvatar(
                                                photoData = user.photoData,
                                                photoUrl = user.photoUrl,
                                                size = 44.dp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isSelf) "@${user.username} (Tú)" else "@${user.username}",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = user.email,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                val userContribCount = contributions.count { item ->
                                                    item.authorId == user.uid ||
                                                        (item.authorId.isBlank() && item.authorUsername.equals(user.username, ignoreCase = true))
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = if (user.role == "ADMIN") MaterialTheme.colorScheme.primaryContainer
                                                        else MaterialTheme.colorScheme.surface
                                                    ) {
                                                        Text(
                                                            text = if (user.role == "ADMIN") "Admin" else "Usuario",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (user.role == "ADMIN") MaterialTheme.colorScheme.onPrimaryContainer
                                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = MaterialTheme.colorScheme.surface,
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable { inspectedUser = user }
                                                    ) {
                                                        Text(
                                                            text = "Aportes ($userContribCount)",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "Admin",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Switch(
                                                    checked = user.role == "ADMIN",
                                                    onCheckedChange = { newChecked ->
                                                        pendingAdminToggle = user to newChecked
                                                    },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                                IconButton(
                                                    onClick = { if (!isSelf) userPendingDelete = user },
                                                    enabled = !isSelf,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (isSelf) MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                                            else SystemRed.copy(alpha = 0.12f)
                                                        )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar Usuario",
                                                        tint = if (isSelf) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else SystemRed,
                                                        modifier = Modifier.size(18.dp)
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
            }
        }
    }
}
