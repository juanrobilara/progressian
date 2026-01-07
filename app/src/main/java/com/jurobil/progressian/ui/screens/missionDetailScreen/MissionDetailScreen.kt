package com.jurobil.progressian.ui.screens.missionDetailScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jurobil.progressian.ui.screens.missionDetailScreen.viewmodel.MissionDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionDetailScreen(
    viewModel: MissionDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val mission = viewModel.mission.value
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf("") }
    var editedXp by remember { mutableStateOf("") }

    LaunchedEffect(mission) {
        if (mission != null) {
            editedTitle = mission.title
            editedXp = mission.xpReward.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editando Misión" else "Detalle de Misión") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                },
                actions = {
                    if (mission != null) {
                        if (isEditing) {
                            IconButton(onClick = {
                                val xpInt = editedXp.toIntOrNull() ?: mission.xpReward
                                viewModel.updateMission(editedTitle, xpInt)
                                isEditing = false
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Guardar")
                            }
                        } else {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (mission != null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {

                if (isEditing) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Título de la misión") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = editedXp,
                        onValueChange = { if (it.all { char -> char.isDigit() }) editedXp = it },
                        label = { Text("Recompensa (XP)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(mission.title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Dificultad: ${mission.difficulty}", style = MaterialTheme.typography.labelLarge)
                    Text("Recompensa: ${mission.xpReward} XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(mission.description, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}