package com.jurobil.progressian.ui.screens.missionDetailScreen.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jurobil.progressian.domain.model.Mission
import com.jurobil.progressian.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissionDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    val missionId: String = checkNotNull(savedStateHandle["missionId"])

    var mission = mutableStateOf<Mission?>(null)
        private set

    init {
        viewModelScope.launch {
            mission.value = habitRepository.getMissionById(missionId)
        }
    }
}