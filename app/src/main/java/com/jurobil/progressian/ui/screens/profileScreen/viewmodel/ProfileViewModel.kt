package com.jurobil.progressian.ui.screens.profileScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurobil.progressian.core.result.Result
import com.jurobil.progressian.domain.model.UserStats
import com.jurobil.progressian.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isAnonymous: Boolean = false,
    val error: String? = null,
    val logoutTriggered: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()


    val userStats: StateFlow<UserStats> = userRepository.getUserStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats()
        )

    init {
        checkUserType()
    }

    private fun checkUserType() {
        _uiState.update { it.copy(isAnonymous = userRepository.isUserAnonymous()) }
    }

    fun onLogout() {
        viewModelScope.launch {
            userRepository.logout()
            _uiState.update { it.copy(logoutTriggered = true) }
        }
    }

    fun onUpdateProfile(newName: String) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = userRepository.updateUserProfile(newName, null)

            when (result) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}