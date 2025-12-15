package com.jurobil.progressian.domain.model

data class UserStats(
    val uid: String = "",
    val userName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val currentLevel: Int = 1,
    val currentXp: Int = 0,
    val coins: Int = 0,
    val itemsOwned: List<String> = emptyList()
)