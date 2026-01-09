package com.jurobil.progressian.domain.repository

import com.jurobil.progressian.domain.model.Comment
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getFeed(): Flow<List<Post>>
    suspend fun createPost(content: String)

    suspend fun updateAuthorProfileInPosts(userId: String, newName: String, newPhotoUrl: String?)

    suspend fun shareHabitProgress(habit: Habit)
    suspend fun shareHabitPlan(habit: Habit)
    fun getComments(postId: String): Flow<List<Comment>>
    suspend fun addComment(postId: String, content: String)
    suspend fun toggleLike(post: Post)

    suspend fun deletePost(postId: String)

    suspend fun updatePost(postId: String, newContent: String)

}