package com.jurobil.progressian.ui.screens.homeScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurobil.progressian.core.result.Result
import com.jurobil.progressian.data.repository.SettingsRepository
import com.jurobil.progressian.domain.model.Difficulty
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Mission
import com.jurobil.progressian.domain.model.UserStats
import com.jurobil.progressian.domain.repository.HabitRepository
import com.jurobil.progressian.domain.repository.UserRepository
import com.jurobil.progressian.domain.usecases.CompleteMissionUseCase
import com.jurobil.progressian.domain.usecases.GenerateHabitPlanUseCase
import com.jurobil.progressian.domain.usecases.RecalculateUserStatsUseCase
import com.jurobil.progressian.domain.usecases.UpdateMissionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val habits: List<Habit> = emptyList(),
    val userStats: UserStats = UserStats(),
    val generatedHabitPlan: List<Habit>? = null,
    val error: String? = null,
    val showLoginWall: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val generateHabitPlanUseCase: GenerateHabitPlanUseCase,
    private val completeMissionUseCase: CompleteMissionUseCase,
    private val updateMissionStatusUseCase: UpdateMissionStatusUseCase,
    private val recalculateUserStatsUseCase: RecalculateUserStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = combine(
        _uiState,
        habitRepository.getAllHabits(),
        userRepository.getUserStats()
    ) { state, habits, stats ->
        state.copy(
            habits = habits,
            userStats = stats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    init {
        loginAnonymously()
        syncUserData()
    }

    private fun loginAnonymously() {
        viewModelScope.launch { userRepository.loginAnonymously() }
    }

    val showWelcomeDialog = settingsRepository.isFirstTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onDismissWelcome() {
        viewModelScope.launch { settingsRepository.completeOnboarding() }
    }

    fun onSendMessage(message: String) {
        if (message.isBlank()) return

        val isAnonymous = userRepository.isUserAnonymous()

        val habitCount = uiState.value.habits.size

        if (isAnonymous && habitCount >= 5) {
            _uiState.update { it.copy(showLoginWall = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }


            val result = generateHabitPlanUseCase(message)

            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, generatedHabitPlan = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
            }
        }
    }

    fun dismissLoginWall() {
        _uiState.update { it.copy(showLoginWall = false) }
    }

    fun onAcceptGeneratedPlan() {
        val plan = _uiState.value.generatedHabitPlan ?: return

        viewModelScope.launch {

            plan.forEach { habit ->
                habitRepository.saveHabit(habit)
            }
            _uiState.update { it.copy(generatedHabitPlan = null) }
        }
    }

    fun onRejectGeneratedPlan() {
        _uiState.update { it.copy(generatedHabitPlan = null) }
    }


    fun removeHabitFromPlan(habitId: String) {
        val currentPlan = _uiState.value.generatedHabitPlan ?: return
        val updatedPlan = currentPlan.filter { it.id != habitId }

        if (updatedPlan.isEmpty()) {
            onRejectGeneratedPlan()
        } else {
            _uiState.update { it.copy(generatedHabitPlan = updatedPlan) }
        }
    }

    fun onMissionChecked(missionId: String, isCompleted: Boolean, xpReward: Int) {
        viewModelScope.launch {
            updateMissionStatusUseCase(missionId, isCompleted, xpReward)
            recalculateUserStatsUseCase()
        }
    }

    fun updateHabitImage(habitId: String, imageUri: String) {
        viewModelScope.launch {
            val habit = habitRepository.getHabitById(habitId)
            if (habit != null) {
                val updatedHabit = habit.copy(imageUrl = imageUri)
                habitRepository.saveHabit(updatedHabit)
            }
        }
    }

    fun onDeleteHabit(habitId: String) {
        viewModelScope.launch { habitRepository.deleteHabit(habitId) }
    }

    fun onUpdateHabitTitleDescription(habitId: String, newTitle: String, newDescription: String) {
        viewModelScope.launch {
            val currentHabit = habitRepository.getHabitById(habitId)
            if (currentHabit != null) {
                val updatedHabit = currentHabit.copy(title = newTitle, description = newDescription)
                habitRepository.saveHabit(updatedHabit)
            }
        }
    }

    private fun syncUserData() {
        viewModelScope.launch { habitRepository.syncHabits() }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}