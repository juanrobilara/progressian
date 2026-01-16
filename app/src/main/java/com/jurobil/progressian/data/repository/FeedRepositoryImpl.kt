package com.jurobil.progressian.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jurobil.progressian.data.mapper.toPost
import com.jurobil.progressian.domain.model.Comment
import com.jurobil.progressian.domain.model.Habit
import com.jurobil.progressian.domain.model.Post
import com.jurobil.progressian.domain.model.PostType
import com.jurobil.progressian.domain.repository.FeedRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FeedRepository {

    override fun getFeed(): Flow<List<Post>> = callbackFlow {
        val userId = auth.currentUser?.uid
        var registration: ListenerRegistration? = null

        registration = firestore.collection("posts")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FeedRepo", "Error feed", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val docs = snapshot?.documents ?: emptyList()

                launch {
                    val posts = docs.map { doc ->
                        async {
                            val post = doc.toPost()

                            if (post != null && userId != null) {
                                val isLiked = firestore.collection("posts")
                                    .document(post.id)
                                    .collection("likes")
                                    .document(userId)
                                    .get()
                                    .await()
                                    .exists()

                                post.copy(isLikedByCurrentUser = isLiked)
                            } else {
                                post
                            }
                        }
                    }.awaitAll().filterNotNull()

                    trySend(posts)
                }
            }
        awaitClose { registration?.remove() }
    }


    override suspend fun updateAuthorProfileInPosts(userId: String, newName: String, newPhotoUrl: String?) {
        try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("authorId", userId)
                .get()
                .await()

            val batch = firestore.batch()

            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, mapOf(
                    "authorName" to newName,
                    "authorPhotoUrl" to newPhotoUrl
                ))
            }

            batch.commit().await()
        } catch (e: Exception) {
            android.util.Log.e("FeedRepo", "Error updating posts profile", e)
        }
    }


    override suspend fun createPost(content: String) {
        savePostToFirestore(content, PostType.TEXT, null)
    }
    override suspend fun shareHabitProgress(habit: Habit) {
        val completed = habit.missions.count { it.isCompleted }
        val total = habit.missions.size
        val content = "¡He avanzado en mi hábito '${habit.title}'! \nLlevo $completed/$total misiones completadas. Nivel de XP: ${habit.totalXpReward}."

        savePostToFirestore(content, PostType.PROGRESS, habit)
    }

    override suspend fun shareHabitPlan(habit: Habit) {
        val content = "Les comparto este hábito personalizado para que lo intenten: '${habit.title}'"
        savePostToFirestore(content, PostType.HABIT_PLAN, habit)
    }

    private suspend fun savePostToFirestore(content: String, type: PostType, habit: Habit?) {
        val user = auth.currentUser ?: return
        val ref = firestore.collection("posts").document()

        val post = Post(
            id = ref.id,
            authorId = user.uid,
            authorName = user.displayName ?: "Aventurero",
            authorPhotoUrl = user.photoUrl?.toString(),
            content = content,
            createdAt = System.currentTimeMillis(),
            type = type,
            sharedHabit = habit
        )

        ref.set(post).await()
    }

    override suspend fun toggleLike(post: Post) {
        val userId = auth.currentUser?.uid ?: return
        val postRef = firestore.collection("posts").document(post.id)
        val likeRef = postRef.collection("likes").document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(likeRef)
            if (snapshot.exists()) {
                transaction.delete(likeRef)
                transaction.update(postRef, "likeCount", com.google.firebase.firestore.FieldValue.increment(-1))
            } else {
                transaction.set(likeRef, mapOf("ts" to System.currentTimeMillis()))
                transaction.update(postRef, "likeCount", com.google.firebase.firestore.FieldValue.increment(1))
            }
        }.await()
    }

    override suspend fun addComment(postId: String, content: String) {
        val user = auth.currentUser ?: return
        val postRef = firestore.collection("posts").document(postId)
        val newCommentRef = postRef.collection("comments").document()

        val comment = Comment(
            id = newCommentRef.id,
            userId = user.uid,
            userName = user.displayName ?: "Aventurero",
            userPhotoUrl = user.photoUrl?.toString(),
            content = content
        )

        firestore.runTransaction { transaction ->
            transaction.set(newCommentRef, comment)
            transaction.update(postRef, "commentCount", com.google.firebase.firestore.FieldValue.increment(1))
        }.await()
    }

    override fun getComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val registration = firestore.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { s, _ ->
                trySend(s?.toObjects(Comment::class.java) ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    override suspend fun deletePost(postId: String) {
        try {
            firestore.collection("posts").document(postId).delete().await()
        } catch (e: Exception) {
            Log.e("FeedRepo", "Error eliminando post", e)
        }
    }

    override suspend fun updatePost(postId: String, newContent: String) {
        try {
            firestore.collection("posts").document(postId)
                .update("content", newContent)
                .await()
        } catch (e: Exception) {
            Log.e("FeedRepo", "Error actualizando post", e)
        }
    }

}