package com.jurobil.progressian.data.repository
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jurobil.progressian.core.result.Result
import com.jurobil.progressian.domain.model.UserStats
import com.jurobil.progressian.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun loginAnonymously(): Result<Boolean> {

        if (auth.currentUser != null) {
            return Result.Success(true)
        }

        return try {
            val authResult = auth.signInAnonymously().await()
            val user = authResult.user

            if (user != null) {
                val userDocRef = firestore.collection("users").document(user.uid)
                val snapshot = userDocRef.get().await()

                if (!snapshot.exists()) {
                    val initialStats = UserStats(
                        uid = user.uid,
                        currentLevel = 1,
                        currentXp = 0
                    )
                    userDocRef.set(initialStats).await()
                }
                Result.Success(true)
            } else {
                Result.Error(Exception("El usuario es nulo tras el login"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error login anonimo", e)
            Result.Error(e)
        }
    }

    override fun getUserStats(): Flow<UserStats> = callbackFlow {
        var firestoreRegistration: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            firestoreRegistration?.remove()
            firestoreRegistration = null

            if (user != null) {
                firestoreRegistration = firestore.collection("users").document(user.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("UserRepository", "Error escuchando stats", error)
                            trySend(UserStats(uid = user.uid, currentLevel = 1))
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val stats = snapshot.toObject(UserStats::class.java)
                            if (stats != null) {
                                trySend(stats)
                            } else {
                                trySend(UserStats(uid = user.uid, currentLevel = 1))
                            }
                        } else {
                            trySend(UserStats(uid = user.uid, currentLevel = 1))
                        }
                    }
            } else {
                trySend(UserStats(uid = "loading", currentLevel = 1))
            }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreRegistration?.remove()
        }
    }

    override suspend fun addXp(amount: Int) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(uid)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentXp = snapshot.getLong("currentXp") ?: 0
                val currentLevel = snapshot.getLong("currentLevel") ?: 1
                var newXp = currentXp + amount
                var newLevel = currentLevel
                var xpToNextLevel = newLevel * 100

                while (newXp >= xpToNextLevel) {
                    newXp -= xpToNextLevel
                    newLevel++
                    xpToNextLevel = newLevel * 100
                }

                transaction.update(userRef, "currentXp", newXp)
                transaction.update(userRef, "currentLevel", newLevel)
            }.await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error añadiendo XP", e)
        }
    }

    override suspend fun removeXp(amount: Int) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(uid)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentXp = snapshot.getLong("currentXp")?.toInt() ?: 0
                val currentLevel = snapshot.getLong("currentLevel")?.toInt() ?: 1
                var newXp = currentXp
                var newLevel = currentLevel
                var xpToRemove = amount

                while (xpToRemove > 0) {
                    if (newXp >= xpToRemove) {

                        newXp -= xpToRemove
                        xpToRemove = 0
                    } else {

                        xpToRemove -= newXp


                        if (newLevel > 1) {
                            newLevel--

                            newXp = newLevel * 100
                        } else {

                            newXp = 0
                            xpToRemove = 0
                        }
                    }
                }
                transaction.update(userRef, "currentXp", newXp)
                transaction.update(userRef, "currentLevel", newLevel)
            }.await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error removiendo XP", e)
        }
    }

    override fun isUserAnonymous(): Boolean {
        return auth.currentUser?.isAnonymous == true
    }

    override suspend fun loginWithGoogle(idToken: String): Result<Boolean> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.isAnonymous) {
                try {
                    currentUser.linkWithCredential(credential).await()

                    firestore.collection("users").document(currentUser.uid).update("email", currentUser.email)

                    Result.Success(true)
                } catch (e: FirebaseAuthUserCollisionException) {

                    val authResult = auth.signInWithCredential(credential).await()
                    checkUserDocExists(authResult.user?.uid)
                    Result.Success(true)
                }
            } else {
                val authResult = auth.signInWithCredential(credential).await()
                checkUserDocExists(authResult.user?.uid)
                Result.Success(true)
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error Google Login", e)
            Result.Error(e)
        }
    }

    override suspend fun loginWithEmail(email: String, pass: String): Result<Boolean> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            if (result.user != null) {
                checkUserDocExists(result.user!!.uid)
                Result.Success(true)
            } else {
                Result.Error(Exception("Login fallido"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun checkUserDocExists(uid: String?) {
        if (uid == null) return
        val userDocRef = firestore.collection("users").document(uid)
        val snapshot = userDocRef.get().await()

        if (!snapshot.exists()) {
            val initialStats = UserStats(
                uid = uid,
                currentLevel = 1,
                currentXp = 0
            )
            userDocRef.set(initialStats).await()
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun updateUserProfile(name: String, photoUrl: String?): Result<Boolean> {
        val user = auth.currentUser ?: return Result.Error(Exception("No user logged in"))

        return try {
            val profileUpdates = userProfileChangeRequest {
                displayName = name
                if (photoUrl != null) {
                    photoUri = android.net.Uri.parse(photoUrl)
                }
            }
            user.updateProfile(profileUpdates).await()
            val updates = mutableMapOf<String, Any>(
                "userName" to name
            )
            if (photoUrl != null) updates["photoUrl"] = photoUrl

            firestore.collection("users").document(user.uid)
                .update(updates)
                .await()

            Result.Success(true)
        } catch (e: Exception) {
            Log.e("UserRepo", "Error updating profile", e)
            Result.Error(e)
        }
    }
}

