package com.jurobil.progressian.domain.repository

import com.jurobil.progressian.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getFeed(): Flow<List<Post>>
    suspend fun createPost(content: String)
}