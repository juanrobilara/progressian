package com.jurobil.progressian.di

import android.content.Context
import androidx.room.Room
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jurobil.progressian.BuildConfig
import com.jurobil.progressian.data.local.ProgressianDatabase
import com.jurobil.progressian.data.local.dao.HabitDao
import com.jurobil.progressian.data.local.dao.MissionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ProgressianDatabase {
        return Room.databaseBuilder(
            context,
            ProgressianDatabase::class.java,
            "progressian_db"
        ).build()
    }

    @Provides
    fun provideHabitDao(db: ProgressianDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideMissionDao(db: ProgressianDatabase): MissionDao = db.missionDao()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    @Provides
    @Singleton
    fun provideGenerativeModel(apiKey: String): GenerativeModel {
        val config = generationConfig {
            responseMimeType = "application/json"
        }

        return GenerativeModel(
            modelName = "models/gemini-2.5-flash",
            apiKey = apiKey,
            generationConfig = config
        )
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}

