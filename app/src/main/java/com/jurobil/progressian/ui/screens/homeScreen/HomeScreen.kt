package com.jurobil.progressian.ui.screens.homeScreen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Mission
import com.jurobil.progressian.ui.screens.homeScreen.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onHabitClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var promptText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    var habitIdToUpdateImage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var selectedHabit by remember { mutableStateOf<Habit?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && habitIdToUpdateImage != null) {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flag)

            viewModel.updateHabitImage(habitIdToUpdateImage!!, uri.toString())
        }
        habitIdToUpdateImage = null
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(state.error!!)
            viewModel.clearError()
        }
    }


    if (state.showLoginWall) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLoginWall() },
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            title = { Text("¡Desbloquea todo el potencial!") },
            text = {
                Text("Has alcanzado el límite de 2 hábitos del modo invitado. \n\nRegístrate gratis para guardar tu progreso en la nube, acceder desde otros dispositivos y crear hábitos ilimitados.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissLoginWall()
                        onNavigateToLogin()
                    }
                ) {
                    Text("Crear cuenta / Iniciar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLoginWall() }) {
                    Text("Quizás más tarde")
                }
            }
        )
    }

    if (state.generatedHabit != null) {
        HabitPreviewDialog(
            habit = state.generatedHabit!!,
            onConfirm = { viewModel.onAcceptGeneratedHabit() },
            onDismiss = { viewModel.onRejectGeneratedHabit() },
            onDeleteMission = { missionId ->
                viewModel.removeMissionFromPreview(missionId)
            },
            onAddMission = { title ->
                viewModel.addMissionToPreview(title)
            }
        )
    }

    if (showActionDialog && selectedHabit != null) {
        AlertDialog(
            onDismissRequest = { showActionDialog = false },
            title = { Text("Gestionar Hábito") },
            text = { Text("¿Qué deseas hacer con '${selectedHabit?.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showActionDialog = false
                        showEditDialog = true // Abrir el de edición
                    }
                ) { Text("Editar") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteHabit(selectedHabit!!.id)
                        showActionDialog = false
                        selectedHabit = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            }
        )
    }

    if (showEditDialog && selectedHabit != null) {
        EditHabitDialog(
            habit = selectedHabit!!,
            onDismiss = {
                showEditDialog = false
                selectedHabit = null
            },
            onConfirm = { newTitle, newDesc ->
                viewModel.onUpdateHabitTitleDescription(selectedHabit!!.id, newTitle, newDesc)
                showEditDialog = false
                selectedHabit = null
            }
        )
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Progressian", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Lvl ${state.userStats.currentLevel} • XP: ${state.userStats.currentXp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {

                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Quiero aprender a tocar guitarra...") },
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.onSendMessage(promptText)
                        promptText = ""
                    },
                    enabled = !state.isLoading && promptText.isNotBlank()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Mis Hábitos",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(state.habits) { habit ->
                HabitCard(
                    habit = habit,
                    onClick = { onHabitClick(habit.id) },
                    onMissionCheck = { missionId, isDone, xp ->
                        viewModel.onMissionChecked(missionId, isDone, xp)
                    },
                    onImageClick = {
                        habitIdToUpdateImage = habit.id
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onLongClick = {
                        selectedHabit = habit
                        showActionDialog = true
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habit: Habit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMissionCheck: (String, Boolean, Int) -> Unit,
    onImageClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color = Color.Gray.copy(alpha = 0.3f))
                        .clickable { onImageClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (!habit.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = habit.imageUrl,
                            contentDescription = "Imagen del Hábito",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar imagen",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Badge(Modifier.padding(start = 4.dp)) {
                        Text("${habit.totalXpReward} XP")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = habit.description,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth() // Ocupa todo el ancho
                // maxLines = 2
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            habit.missions.take(2).forEach { mission ->
                MissionItemRow(mission, onMissionCheck)
            }
            if (habit.missions.size > 2) {
                Text(
                    "+${habit.missions.size - 2} misiones más",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MissionItemRow(
    mission: Mission,
    onCheck: (String, Boolean, Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = mission.isCompleted,
            onCheckedChange = { isChecked ->
                onCheck(mission.id, isChecked, mission.xpReward)
            }
        )
        Text(
            text = mission.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun HabitPreviewDialog(
    habit: Habit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteMission: (String) -> Unit, // Callback borrar
    onAddMission: (String) -> Unit     // Callback agregar
) {
    var newMissionText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(habit.title) },
        text = {
            Column {
                Text(habit.description, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))

                // Lista de misiones con scroll
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(habit.missions) { mission ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${mission.title}", modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDeleteMission(mission.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Input simple para agregar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newMissionText,
                        onValueChange = { newMissionText = it },
                        placeholder = { Text("Nueva misión...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        onAddMission(newMissionText)
                        newMissionText = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Guardar Plan") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Preview
@Composable
fun HabitCardPreview() {
    HabitCard(
        habit = Habit(
            "1",
            "Las aventuras trans de antonio un trans hecho y derecho que le gusta follar",
            "Sexoanaldurooo",
            "",
            totalXpReward = 2,
            isCompleted = false,
            missions = emptyList(),
            createdAt = 1L,
        ),
        onClick = {},
        onMissionCheck = { _, _, _ -> },
        onImageClick = {},
        onLongClick = {}
    )
}

@Composable
fun EditHabitDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(habit.title) }
    var description by remember { mutableStateOf(habit.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Hábito") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, description) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}