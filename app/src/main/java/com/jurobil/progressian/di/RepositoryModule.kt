package com.jurobil.progressian.di

import com.jurobil.progressian.data.repository.AIRepositoryImpl
import com.jurobil.progressian.data.repository.FeedRepositoryImpl
import com.jurobil.progressian.data.repository.HabitRepositoryImpl
import com.jurobil.progressian.data.repository.UserRepositoryImpl
import com.jurobil.progressian.domain.repository.AIRepository
import com.jurobil.progressian.domain.repository.FeedRepository
import com.jurobil.progressian.domain.repository.HabitRepository
import com.jurobil.progressian.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    abstract fun bindAIRepository(impl: AIRepositoryImpl): AIRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindFeedRepository(impl: FeedRepositoryImpl): FeedRepository
}