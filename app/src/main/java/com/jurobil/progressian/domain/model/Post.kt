package com.jurobil.progressian.domain.model

enum class PostType {
    TEXT, PROGRESS, HABIT_PLAN
}

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val content: String = "",
    val authorPhotoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val type: PostType = PostType.TEXT,
    val sharedHabit: Habit? = null,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isLikedByCurrentUser: Boolean = false,
)