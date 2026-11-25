
package com.example.dattcnpm.data.remote

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

object AuthRepository {

    private val auth: FirebaseAuth
        get() = FirebaseProvider.auth

    val currentUser
        get() = auth.currentUser

    val currentUid: String?
        get() = auth.currentUser?.uid

    /**
     * Lấy Firebase ID Token của tài khoản đang đăng nhập.
     *
     * Token này được gửi đến FastAPI Backend để xác minh danh tính.
     */
    suspend fun getFirebaseIdToken(
        forceRefresh: Boolean = false
    ): String {
        val user = auth.currentUser
            ?: throw IllegalStateException(
                "Người dùng chưa đăng nhập Firebase."
            )

        return user.getIdToken(forceRefresh)
            .await()
            .token
            ?: throw IllegalStateException(
                "Không thể lấy Firebase ID Token."
            )
    }

    suspend fun signUp(
        email: String,
        password: String
    ): AuthResult {
        require(email.isNotBlank())
        require(password.isNotBlank())

        return auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).await()
    }

    suspend fun signIn(
        email: String,
        password: String
    ): AuthResult {
        return auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()
    }

    suspend fun signInWithGoogle(
        idToken: String
    ): AuthResult {
        require(idToken.isNotBlank()) {
            "Không nhận được Google ID Token."
        }

        val credential = GoogleAuthProvider
            .getCredential(idToken, null)

        return auth.signInWithCredential(
            credential
        ).await()
    }

    suspend fun sendPasswordResetEmail(
        email: String
    ) {
        require(email.isNotBlank()) {
            "Vui lòng nhập email."
        }

        auth.setLanguageCode("vi")

        auth.sendPasswordResetEmail(
            email.trim()
        ).await()
    }

    suspend fun reloadUser() {
        auth.currentUser?.reload()?.await()
    }

    fun signOut() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
