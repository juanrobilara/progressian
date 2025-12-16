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
    val generatedHabit: Habit? = null,
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
    private val updateMissionStatusUseCase: UpdateMissionStatusUseCase
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
    }

    private fun loginAnonymously() {
        viewModelScope.launch {
            userRepository.loginAnonymously()
        }
    }

    val showWelcomeDialog = settingsRepository.isFirstTime
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun onDismissWelcome() {
        viewModelScope.launch {
            settingsRepository.completeOnboarding()
        }
    }

    fun onSendMessage(message: String) {
        if (message.isBlank()) return

        val isAnonymous = userRepository.isUserAnonymous()
        val habitCount = uiState.value.habits.size

        if (isAnonymous && habitCount >= 2) {
            _uiState.update { it.copy(showLoginWall = true) }
            return
        }


            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val result = generateHabitPlanUseCase(message)

                if (result.isSuccess) {
                    val habit = result.getOrNull()
                    if (habit != null) {
                        _uiState.update { it.copy(isLoading = false, generatedHabit = habit) }
                    }
                } else if (result is Result.Error) {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
            }
        }

    fun dismissLoginWall() {
            _uiState.update { it.copy(showLoginWall = false) }
    }

    fun onAcceptGeneratedHabit() {
        val habitToSave = _uiState.value.generatedHabit ?: return

        viewModelScope.launch {
            habitRepository.saveHabit(habitToSave)
            _uiState.update { it.copy(generatedHabit = null) }
        }
    }

    fun onRejectGeneratedHabit() {
        _uiState.update { it.copy(generatedHabit = null) }
    }

    fun onMissionChecked(missionId: String, isCompleted: Boolean, xpReward: Int) {
        viewModelScope.launch {
            updateMissionStatusUseCase(missionId, isCompleted, xpReward)
        }
    }
    fun removeMissionFromPreview(missionId: String) {
        val currentHabit = _uiState.value.generatedHabit ?: return
        val updatedMissions = currentHabit.missions.filter { it.id != missionId }


        val newTotalXp = updatedMissions.sumOf { it.xpReward }

        _uiState.update {
            it.copy(generatedHabit = currentHabit.copy(missions = updatedMissions, totalXpReward = newTotalXp))
        }
    }

    fun addMissionToPreview(title: String) {
        val currentHabit = _uiState.value.generatedHabit ?: return
        if (title.isBlank()) return

        val newMission = Mission(
            habitId = currentHabit.id,
            title = title,
            description = "Misión personalizada",
            difficulty = Difficulty.EASY,
            xpReward = 10,
            isCompleted = false
        )

        val updatedMissions = currentHabit.missions + newMission
        val newTotalXp = updatedMissions.sumOf { it.xpReward }

        _uiState.update {
            it.copy(generatedHabit = currentHabit.copy(missions = updatedMissions, totalXpReward = newTotalXp))
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
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
        }
    }

    fun onUpdateHabitTitleDescription(habitId: String, newTitle: String, newDescription: String) {
        viewModelScope.launch {
            val currentHabit = habitRepository.getHabitById(habitId)
            if (currentHabit != null) {
                val updatedHabit = currentHabit.copy(
                    title = newTitle,
                    description = newDescription
                )
                habitRepository.saveHabit(updatedHabit)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}