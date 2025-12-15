package com.jurobil.progressian.domain.usecases

import com.jurobil.progressian.domain.model.Post
import com.jurobil.progressian.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFeedUseCase @Inject constructor(
    private val repository: FeedRepository
) {
    operator fun invoke(): Flow<List<Post>> = repository.getFeed()
}