package com.jurobil.progressian.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.jurobil.progressian.domain.model.Post
import com.jurobil.progressian.domain.repository.FeedRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FeedRepository {

    override fun getFeed(): Flow<List<Post>> = callbackFlow {
        var registration: ListenerRegistration? = null

        registration = firestore.collection("posts")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FeedRepo", "Error feed", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val posts = snapshot?.toObjects(Post::class.java) ?: emptyList()
                trySend(posts)
            }

        awaitClose { registration?.remove() }
    }

    override suspend fun createPost(content: String) {
        val user = auth.currentUser ?: return

        val post = Post(
            id = firestore.collection("posts").document().id,
            authorId = user.uid,
            authorName = user.displayName ?: "Usuario",
            authorPhotoUrl = user.photoUrl?.toString(),
            content = content,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("posts")
            .document(post.id)
            .set(post)
            .await()
    }
}