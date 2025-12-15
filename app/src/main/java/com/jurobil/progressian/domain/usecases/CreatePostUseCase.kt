package com.jurobil.progressian.domain.usecases

import com.jurobil.progressian.domain.repository.FeedRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val repository: FeedRepository
) {
    suspend operator fun invoke(content: String) {
        repository.createPost(content)
    }
}
