package com.jurobil.progressian.domain.repository

import android.net.Uri
import com.jurobil.progressian.core.result.Result
import com.jurobil.progressian.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserStats(): Flow<UserStats>
    suspend fun addXp(amount: Int)
    suspend fun loginAnonymously(): Result<Boolean>

    fun isUserAnonymous(): Boolean
    suspend fun loginWithGoogle(idToken: String): Result<Boolean>

    suspend fun loginWithEmail(email: String, pass: String): Result<Boolean>
    suspend fun removeXp(amount: Int)
    suspend fun logout()
    suspend fun updateUserProfile(name: String, photoUrl: String?): Result<Boolean>

    suspend fun uploadAvatar(uri: Uri): String
}