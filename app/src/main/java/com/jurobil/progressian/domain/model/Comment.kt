package com.jurobil.progressian.domain.model

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)