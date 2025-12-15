package com.jurobil.progressian.ui.screens.missionDetailScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Misión") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        if (mission != null) {
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text(mission.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Dificultad: ${mission.difficulty}", style = MaterialTheme.typography.labelLarge)
                Text("Recompensa: ${mission.xpReward} XP", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Text(mission.description, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}