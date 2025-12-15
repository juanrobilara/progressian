package com.jurobil.progressian.domain.model

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)