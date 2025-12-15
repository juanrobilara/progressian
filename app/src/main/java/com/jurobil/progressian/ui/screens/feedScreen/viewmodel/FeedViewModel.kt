package com.jurobil.progressian.ui.screens.feedScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurobil.progressian.domain.model.Post
import com.jurobil.progressian.domain.usecases.CreatePostUseCase
import com.jurobil.progressian.domain.usecases.GetFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    getFeedUseCase: GetFeedUseCase,
    private val createPostUseCase: CreatePostUseCase
) : ViewModel() {

    val uiState: StateFlow<FeedUiState> =
        getFeedUseCase()
            .map { FeedUiState(posts = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FeedUiState(isLoading = true)
            )

    fun createPost(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                createPostUseCase(content)
            } catch (e: Exception) {
                android.util.Log.e("FeedViewModel", "Error al publicar: ${e.message}")
            }
        }
    }
}