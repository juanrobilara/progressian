package com.jurobil.progressian.ui.screens.feedScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurobil.progressian.domain.model.Comment
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Post
import com.jurobil.progressian.domain.model.UserStats
import com.jurobil.progressian.domain.repository.FeedRepository
import com.jurobil.progressian.domain.repository.HabitRepository
import com.jurobil.progressian.domain.repository.UserRepository
import com.jurobil.progressian.domain.usecases.CreatePostUseCase
import com.jurobil.progressian.domain.usecases.GetFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val selectedUserProfile: UserStats? = null,
    val isProfileLoading: Boolean = false
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val habitRepository: HabitRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _feedFlow = feedRepository.getFeed()

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = combine(_feedFlow, _uiState) { posts, state ->


        state.copy(posts = posts)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedUiState(isLoading = true)
    )

    val currentUserId: String
        get() = if (userRepository.isUserAnonymous()) "anon" else try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        } catch(e: Exception) { "" }

    fun createPost(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            feedRepository.createPost(content)
        }
    }


    fun cloneHabit(habit: Habit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val newHabitId = UUID.randomUUID().toString()

                val newHabit = habit.copy(
                    id = newHabitId,
                    userId = "",
                    isCompleted = false,
                    createdAt = System.currentTimeMillis(),
                    missions = habit.missions.map { mission ->
                        mission.copy(
                            id = UUID.randomUUID().toString(),
                            habitId = newHabitId,
                            isCompleted = false
                        )
                    }
                )
                habitRepository.saveHabit(newHabit)

                _uiState.update { it.copy(isLoading = false, message = "¡Hábito '${newHabit.title}' agregado a tu perfil!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, message = "Error al clonar: ${e.message}") }
            }
        }
    }


    fun toggleLike(post: Post) {
        viewModelScope.launch {
            feedRepository.toggleLike(post)
        }
    }

    fun addComment(postId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            feedRepository.addComment(postId, content)
        }
    }

    fun getComments(postId: String): Flow<List<Comment>> {
        return feedRepository.getComments(postId)
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun formatTimestamp(timestamp: Long): String {
        val formatter = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())

        return formatter.format(Instant.ofEpochMilli(timestamp))
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            feedRepository.deletePost(post.id)
        }
    }

    fun updatePost(post: Post, newContent: String) {
        if (newContent.isBlank()) return
        viewModelScope.launch {
            feedRepository.updatePost(post.id, newContent)
        }
    }


    //AMISTADES

    fun onSelectUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true, selectedUserProfile = null) }
            val userStats = userRepository.getUserProfile(userId)
            _uiState.update { it.copy(isProfileLoading = false, selectedUserProfile = userStats) }
        }
    }

    fun onDismissUserProfile() {
        _uiState.update { it.copy(selectedUserProfile = null) }
    }

    fun toggleFollow(targetUserId: String) {
        val currentUser = currentUserId
        val selectedUser = _uiState.value.selectedUserProfile ?: return

        viewModelScope.launch {
            val isFollowing = selectedUser.followers.contains(currentUser)

            val updatedFollowers = if (isFollowing) {
                selectedUser.followers - currentUser
            } else {
                selectedUser.followers + currentUser
            }

            _uiState.update {
                it.copy(selectedUserProfile = selectedUser.copy(followers = updatedFollowers))
            }

            if (isFollowing) {
                userRepository.unfollowUser(targetUserId)
            } else {
                userRepository.followUser(targetUserId)
            }
        }
    }

}