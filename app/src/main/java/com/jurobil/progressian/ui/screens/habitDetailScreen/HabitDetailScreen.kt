package com.jurobil.progressian.ui.screens.habitDetailScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role.Companion.Checkbox
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jurobil.progressian.domain.model.Mission
import com.jurobil.progressian.ui.screens.habitDetailScreen.viewmodel.HabitDetailViewModel
import com.jurobil.progressian.ui.screens.homeScreen.MissionItemRow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    viewModel: HabitDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onMissionClick: (String) -> Unit
) {
    val habit by viewModel.habit.collectAsState()
    var showShareDialog by remember { mutableStateOf(false) }
    var showAddMissionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = habit?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddMissionDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Misión")
            }
        }
    ) { padding ->
        val currentHabit = habit
        if (currentHabit != null) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(currentHabit.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Misiones", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(currentHabit.missions) { mission ->
                    RpgMissionCard(
                        mission = mission,
                        onClick = { onMissionClick(mission.id) },
                        onCheck = { isChecked ->
                            viewModel.onMissionChecked(mission.id, isChecked, mission.xpReward)
                        },
                        onDelete = { viewModel.deleteMission(mission.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showShareDialog && habit != null) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Compartir Hábito") },
            text = { Text("¿Cómo deseas compartir tu aventura?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.shareHabit(habit!!, shareAsPlan = false)
                    showShareDialog = false
                }) { Text("Solo mi Progreso") }
            },
            dismissButton = {
                Button(onClick = {
                    viewModel.shareHabit(habit!!, shareAsPlan = true)
                    showShareDialog = false
                }) { Text("Compartir Plan (Clonable)") }
            }
        )
    }

    if (showAddMissionDialog) {
        AddMissionDialog(
            onDismiss = { showAddMissionDialog = false },
            onConfirm = { title, description, xpReward ->
                viewModel.addMission(title, description, xpReward)
                showAddMissionDialog = false
            }
        )
    }
}

@Composable
fun AddMissionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var xpReward by remember { mutableIntStateOf(50) }
    val (difficultyName, difficultyColor) = when (xpReward) {
        in 0..50 -> "FÁCIL" to Color(0xFF4CAF50)      // Verde
        in 51..100 -> "NORMAL" to Color(0xFFFFC107)   // Dorado
        in 101..300 -> "DIFÍCIL" to Color(0xFFFF5722) // Naranja
        else -> "ÉPICO" to Color(0xFF9C27B0) //Púrpura
    } //Nota para juan del futuro, mover colores a donde corresponda

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Nueva Misión")
                Spacer(modifier = Modifier.width(8.dp))
                Badge(containerColor = difficultyColor.copy(alpha = 0.2f)) {
                    Text(
                        difficultyName,
                        color = difficultyColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recompensa:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$xpReward XP",
                        style = MaterialTheme.typography.titleMedium,
                        color = difficultyColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = xpReward.toFloat(),
                    onValueChange = { newValue ->
                        xpReward = (newValue / 10).roundToInt() * 10
                    },
                    valueRange = 0f..500f,
                    colors = SliderDefaults.colors(
                        thumbColor = difficultyColor,
                        activeTrackColor = difficultyColor,
                        inactiveTrackColor = difficultyColor.copy(alpha = 0.2f)
                    )
                )

                Text(
                    text = "Ajusta la XP para definir la dificultad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description, xpReward) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = difficultyColor)
            ) {
                Text("Agregar Misión", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
@Composable
fun RpgMissionCard(
    mission: Mission,
    onCheck: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {

    val isDone = mission.isCompleted
    val borderColor = if (isDone) Color.Gray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
    val containerColor = if (isDone) Color.Black.copy(alpha = 0.3f) else Color(0xFF1E1E1E)
    val textColor = if (isDone) Color.Gray else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),

        border = BorderStroke(2.dp, borderColor),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(if (isDone) 0.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = mission.isCompleted,
                onCheckedChange = onCheck,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.Gray,
                    uncheckedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mission.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isDone) FontWeight.Normal else FontWeight.Bold,
                    color = textColor,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null
                )

                if (!isDone) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Text(
                            text = "[ ${mission.xpReward} XP ]",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${mission.difficulty.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = if (isDone) Color.Gray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}