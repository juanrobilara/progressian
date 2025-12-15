package com.jurobil.progressian.ui.screens.feedScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jurobil.progressian.domain.model.Post
import com.jurobil.progressian.ui.screens.feedScreen.components.CreatePostDialog
import com.jurobil.progressian.ui.screens.feedScreen.components.PostCard
import com.jurobil.progressian.ui.screens.feedScreen.viewmodel.FeedViewModel

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Publicar")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.posts) { post ->
                PostCard(post)
            }
        }
    }

    if (showDialog) {
        CreatePostDialog(
            onDismiss = { showDialog = false },
            onPublish = {
                viewModel.createPost(it)
                showDialog = false
            }
        )
    }
}