package com.jurobil.progressian.ui.screens.habitDetailScreen.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.repository.HabitRepository
import com.jurobil.progressian.domain.usecases.CompleteMissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val completeMissionUseCase: CompleteMissionUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val habitId: String = checkNotNull(savedStateHandle["habitId"])
    private val _habit = MutableStateFlow<Habit?>(null)
    val habit = _habit.asStateFlow()

    init {
        loadHabit()
    }

    private fun loadHabit() {
        viewModelScope.launch {
            _habit.value = habitRepository.getHabitById(habitId)
        }
    }

    fun onMissionChecked(missionId: String, isCompleted: Boolean, xpReward: Int) {
        viewModelScope.launch {
            completeMissionUseCase(missionId, isCompleted, xpReward)
            loadHabit() // Recargamos para refrescar la UI
        }
    }
}