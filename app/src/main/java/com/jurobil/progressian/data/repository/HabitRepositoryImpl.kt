package com.jurobil.progressian.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jurobil.progressian.data.local.dao.HabitDao
import com.jurobil.progressian.data.local.dao.MissionDao
import com.jurobil.progressian.data.mapper.toDomain
import com.jurobil.progressian.data.mapper.toDomainHabit
import com.jurobil.progressian.data.mapper.toEntity
import com.jurobil.progressian.data.mapper.toFirestoreMap
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Mission
import com.jurobil.progressian.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao,
    private val missionDao: MissionDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : HabitRepository {


    override fun getAllHabits(): Flow<List<Habit>> {
        val currentUserId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return dao.getAllHabits(currentUserId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getHabitById(id: String): Habit? {
        val habitWithMissions = dao.getHabitById(id)
        return habitWithMissions?.toDomain()
    }



    override suspend fun getMissionById(missionId: String): Mission? {
        val missionEntity = missionDao.getMissionById(missionId)
        return missionEntity?.toDomain()
    }



    override suspend fun saveHabit(habit: Habit) {
        val currentUserId = auth.currentUser?.uid ?: return
        val habitToSave = habit.copy(userId = currentUserId)


        val habitEntity = habitToSave.toEntity()
        val missionEntities = habitToSave.missions.map { it.toEntity(habitId = habit.id) }
        dao.saveHabitWithMissions(habitEntity, missionEntities)


        try {
            firestore.collection("habits")
                .document(habitToSave.id)
                .set(habitToSave.toFirestoreMap())
        } catch (e: Exception) {

            android.util.Log.e("HabitRepo", "Error respaldando en nube", e)
        }
    }

    override suspend fun toggleMissionComplete(missionId: String, completed: Boolean) {
        dao.updateMissionStatus(missionId, completed)
        updateHabitInFirestore(missionId)
    }

    override suspend fun toggleMissionIncomplete(missionId: String, completed: Boolean) {
        dao.updateMissionStatus(missionId, !completed)
        updateHabitInFirestore(missionId)
    }

    private suspend fun updateHabitInFirestore(missionId: String) {
        val mission = missionDao.getMissionById(missionId) ?: return
        val habitWithMissions = dao.getHabitById(mission.habitId) ?: return
        val habitDomain = habitWithMissions.toDomain()

        try {
            firestore.collection("habits")
                .document(habitDomain.id)
                .set(habitDomain.toFirestoreMap())
        } catch (e: Exception) {
            android.util.Log.e("HabitRepo", "Error actualizando nube", e)
        }
    }

    override suspend fun deleteHabit(habitId: String) {
        dao.deleteHabitById(habitId)
        try {
            firestore.collection("habits").document(habitId).delete()
        } catch (e: Exception) { }
    }

    override suspend fun syncHabits() {
        val userId = auth.currentUser?.uid ?: return

        try {

            val snapshot = firestore.collection("habits")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            if (!snapshot.isEmpty) {

                val habits = snapshot.documents.mapNotNull { it.toDomainHabit() }

                habits.forEach { habit ->
                    val entity = habit.toEntity()
                    val missions = habit.missions.map { it.toEntity(habit.id) }
                    dao.saveHabitWithMissions(entity, missions)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitRepo", "Error sincronizando", e)
        }
    }
}



