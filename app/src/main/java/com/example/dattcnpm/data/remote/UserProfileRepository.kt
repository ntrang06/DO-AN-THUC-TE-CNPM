
package com.example.dattcnpm.data.remote

import com.example.dattcnpm.data.remote.dto.UserProfileDto
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

object UserProfileRepository {

    suspend fun createProfile(
        uid: String,
        displayName: String,
        email: String
    ): UserProfileDto {

        val document = FirebaseProvider.userDocument(uid)

        FirebaseProvider.firestore
            .runTransaction { transaction ->

                val snapshot = transaction.get(document)

                if (!snapshot.exists()) {
                    val fields = mapOf<String, Any?>(
                        "displayName" to displayName.trim(),
                        "email" to email.trim(),
                        "usageMode" to null,
                        "avatarUrl" to null,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )

                    transaction.set(document, fields)
                }

                Unit
            }.await()

        return getProfile(uid)
            ?: error("Không thể tải hồ sơ người dùng.")
    }

    suspend fun getProfile(
        uid: String
    ): UserProfileDto? {

        val snapshot = FirebaseProvider
            .userDocument(uid)
            .get()
            .await()

        return if (snapshot.exists()) {
            snapshot.toObject(UserProfileDto::class.java)
        } else {
            null
        }
    }

    suspend fun updateProfile(
        uid: String,
        updates: Map<String, Any?>
    ) {
        val fields = updates.toMutableMap()

        fields.remove("createdAt")
        fields["updatedAt"] = FieldValue.serverTimestamp()

        FirebaseProvider
            .userDocument(uid)
            .update(fields)
            .await()
    }
}
