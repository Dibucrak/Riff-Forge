package com.riffforge.feature_admin.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.components.GlassBackground
import com.riffforge.core.presentation.components.GlassCard
import com.riffforge.core.presentation.components.GlassSectionHeader
import com.riffforge.core.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Liquid Glass Admin Screen — dos pestañas: Aportes pendientes y Gestión de usuarios.
 */
@Composable
fun AdminScreen(
    onNavigateUp: () -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var userToDelete by rememberSaveable { mutableStateOf<String?>(null) }

    GlassBackground(isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top bar — glassmorphic header
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkTheme = isDarkTheme,
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateUp,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isDarkTheme) SecondarySystemBackgroundDark.copy(alpha = 0.3f) else SystemGroupedBackgroundLight.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Panel de Administración",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs — glassmorphic pill
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkTheme = isDarkTheme,
                cornerRadius = 50.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AdminTabButton(
                        modifier = Modifier.weight(1f),
                        text = "Aportes",
                        icon = Icons.Default.Check,
                        isSelected = state.selectedTab == 0,
                        isDarkTheme = isDarkTheme,
                        onClick = { viewModel.onEvent(AdminEvent.SelectTab(0)) }
                    )
                    AdminTabButton(
                        modifier = Modifier.weight(1f),
                        text = "Usuarios",
                        icon = Icons.Default.Person,
                        isSelected = state.selectedTab == 1,
                        isDarkTheme = isDarkTheme,
                        onClick = { viewModel.onEvent(AdminEvent.SelectTab(1)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AccentTint
                    )
                } else {
                    when (state.selectedTab) {
                        0 -> ContributionsTab(
                            contributions = state.pendingContributions,
                            isDarkTheme = isDarkTheme,
                            textColor = textColor,
                            dimTextColor = dimTextColor,
                            onApprove = { viewModel.onEvent(AdminEvent.ApproveContribution(it)) },
                            onReject = { viewModel.onEvent(AdminEvent.RejectContribution(it)) },
                            viewModel = viewModel
                        )
                        1 -> UsersTab(
                            users = state.users,
                            isDarkTheme = isDarkTheme,
                            textColor = textColor,
                            dimTextColor = dimTextColor,
                            onToggleAdmin = { uid, isAdmin ->
                                viewModel.onEvent(AdminEvent.ToggleAdminRole(uid, isAdmin))
                            },
                            onDeleteUser = { uid ->
                                userToDelete = uid
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && userToDelete != null) {
        val user = state.users.find { it.uid == userToDelete }
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text("Eliminar usuario", color = textColor, fontWeight = FontWeight.Bold)
            },
            text = {
                val userName = user?.displayName ?: user?.email ?: "este usuario"
                Text(
                    text = "¿Estás seguro de que deseas eliminar a $userName? Esta acción no se puede deshacer.",
                    color = dimTextColor
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToDelete?.let { viewModel.onEvent(AdminEvent.SetDeleteTarget(it)) }
                        viewModel.onEvent(AdminEvent.ConfirmDeleteUser)
                        showDeleteDialog = false
                        userToDelete = null
                    }
                ) {
                    Text("Eliminar", color = SystemRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = dimTextColor)
                }
            }
        )
    }
}

@Composable
private fun AdminTabButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val bgColor = if (isSelected) {
        AccentTint.copy(alpha = 0.25f)
    } else {
        if (isDarkTheme) SecondarySystemBackgroundDark.copy(alpha = 0.2f) else SystemGroupedBackgroundLight.copy(alpha = 0.3f)
    }
    val tint = if (isSelected) AccentTint else textColor

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = tint, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ContributionsTab(
    contributions: List<com.riffforge.feature_contributions.domain.model.Contribution>,
    isDarkTheme: Boolean,
    textColor: Color,
    dimTextColor: Color,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    viewModel: AdminViewModel
) {
    if (contributions.isEmpty()) {
        GlassCard(
            modifier = Modifier.fillMaxSize(),
            isDarkTheme = isDarkTheme
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = AccentTint.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Bandeja limpia",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No hay aportes pendientes de revisión.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dimTextColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(contributions) { contribution ->
                AdminContributionCard(
                    contribution = contribution,
                    isDarkTheme = isDarkTheme,
                    textColor = textColor,
                    dimTextColor = dimTextColor,
                    onApprove = { onApprove(contribution.id) },
                    onReject = { onReject(contribution.id) }
                )
            }
        }
    }
}

@Composable
private fun AdminContributionCard(
    contribution: com.riffforge.feature_contributions.domain.model.Contribution,
    isDarkTheme: Boolean,
    textColor: Color,
    dimTextColor: Color,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDarkTheme = isDarkTheme,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = contribution.title,
                style = MaterialTheme.typography.titleMedium,
                color = AccentTint,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = contribution.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = dimTextColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Autor",
                    modifier = Modifier.size(16.dp),
                    tint = dimTextColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = contribution.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = dimTextColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onReject,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = SystemRed.copy(alpha = 0.15f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Rechazar",
                        tint = SystemRed
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onApprove,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = AccentTint.copy(alpha = 0.15f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Aprobar",
                        tint = AccentTint
                    )
                }
            }
        }
    }
}

@Composable
private fun UsersTab(
    users: List<com.riffforge.feature_admin.domain.model.AdminUser>,
    isDarkTheme: Boolean,
    textColor: Color,
    dimTextColor: Color,
    onToggleAdmin: (String, Boolean) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    if (users.isEmpty()) {
        GlassCard(
            modifier = Modifier.fillMaxSize(),
            isDarkTheme = isDarkTheme
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = dimTextColor.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sin usuarios",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No hay usuarios registrados aún.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dimTextColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(users) { user ->
                AdminUserCard(
                    user = user,
                    isDarkTheme = isDarkTheme,
                    textColor = textColor,
                    dimTextColor = dimTextColor,
                    onToggleAdmin = { onToggleAdmin(user.uid, it) },
                    onDeleteUser = { onDeleteUser(user.uid) }
                )
            }
        }
    }
}

@Composable
private fun AdminUserCard(
    user: com.riffforge.feature_admin.domain.model.AdminUser,
    isDarkTheme: Boolean,
    textColor: Color,
    dimTextColor: Color,
    onToggleAdmin: (Boolean) -> Unit,
    onDeleteUser: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDarkTheme = isDarkTheme,
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AccentTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (user.displayName ?: user.email).firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = AccentTint,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.displayName ?: "Sin nombre",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = dimTextColor
                )
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale("es"))
                Text(
                    text = "Desde ${dateFormat.format(user.joinDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = dimTextColor.copy(alpha = 0.7f)
                )
                if (user.isAdmin) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(AccentTint.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentTint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Admin toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = dimTextColor
                    )
                    Switch(
                        checked = user.isAdmin,
                        onCheckedChange = onToggleAdmin,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentTint,
                            checkedTrackColor = AccentTint.copy(alpha = 0.5f),
                            uncheckedThumbColor = dimTextColor.copy(alpha = 0.5f),
                            uncheckedTrackColor = dimTextColor.copy(alpha = 0.2f)
                        )
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDeleteUser,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = SystemRed.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar usuario",
                        tint = SystemRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}