package com.jurobil.progressian.ui.screens.profileScreen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jurobil.progressian.ui.screens.profileScreen.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val userStats by viewModel.userStats.collectAsState()


    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onUpdateAvatar(uri)
        }
    }


    LaunchedEffect(state.logoutTriggered) {
        if (state.logoutTriggered) {
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                actions = {
                    IconButton(onClick = { viewModel.onLogout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!userStats.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = userStats.photoUrl,
                        contentDescription = "Avatar del Héroe",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Sin Avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Toca para cambiar retrato",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(24.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nivel ${userStats.currentLevel}", style = MaterialTheme.typography.headlineSmall)
                    Text("${userStats.currentXp} XP totales", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isAnonymous) {

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Modo Invitado",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tus datos no están sincronizados. Inicia sesión para guardar tu progreso.",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.onLogout() }) {
                            Text("Crear cuenta / Login")
                        }
                    }
                }
            } else {

                if (isEditing) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Nombre de usuario") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isEditing = false }) { Text("Cancelar") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.onUpdateProfile(editedName)
                            isEditing = false
                        }) {
                            Text("Guardar")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Nombre", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = userStats.userName.ifBlank { "Usuario sin nombre" },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        IconButton(onClick = {
                            editedName = userStats.userName
                            isEditing = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("Email", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = userStats.email.ifBlank { "No disponible" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}