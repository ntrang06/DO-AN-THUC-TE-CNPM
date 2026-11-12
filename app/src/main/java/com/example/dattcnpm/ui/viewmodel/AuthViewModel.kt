package com.example.dattcnpm.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSuccess by mutableStateOf(false)
        private set

    // Mock session: chỉ tồn tại trong bộ nhớ ứng dụng.
    var mockLoggedIn by mutableStateOf(false)
        private set

    var mockEmail by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    fun signIn(
        email: String,
        pass: String,
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        if (isLoading) return

        val address = email.trim()

        if (address.isBlank() || pass.isBlank()) {
            errorMessage = "Vui lòng nhập đầy đủ email và mật khẩu."
            onComplete(false)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(address).matches()
        ) {
            errorMessage = "Vui lòng nhập địa chỉ email hợp lệ."
            onComplete(false)
            return
        }

        executeMock {
            mockLoggedIn = true
            mockEmail = address
            isSuccess = true
            onComplete(true)
        }
    }

    fun signUp(
        name: String,
        email: String,
        pass: String,
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        if (isLoading) return

        val displayName = name.trim()
        val address = email.trim()

        when {
            displayName.isBlank() ->
                errorMessage = "Vui lòng nhập họ và tên."

            !android.util.Patterns.EMAIL_ADDRESS
                .matcher(address).matches() ->
                errorMessage = "Vui lòng nhập email hợp lệ."

            pass.length < 8 ||
                    pass.none { it.isLetter() } ||
                    pass.none { it.isDigit() } ->
                errorMessage =
                    "Mật khẩu cần ít nhất 8 ký tự, gồm chữ và số."
        }

        if (errorMessage != null) {
            onComplete(false)
            return
        }

        executeMock {
            mockLoggedIn = true
            mockEmail = address
            isSuccess = true
            onComplete(true)
        }
    }

    fun sendPasswordReset(
        email: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (isLoading) return

        val address = email.trim()

        if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(address).matches()
        ) {
            errorMessage = "Vui lòng nhập email hợp lệ."
            onComplete(false)
            return
        }

        executeMock {
            isSuccess = true
            onComplete(true)
        }
    }

    fun signOut(
        context: Context,
        onComplete: () -> Unit
    ) {
        if (isLoading) return

        executeMock {
            mockLoggedIn = false
            mockEmail = null
            isSuccess = true
            onComplete()
        }
    }

    fun signInWithGoogle(
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        if (isLoading) return

        executeMock {
            mockLoggedIn = true
            mockEmail = "demo@gmail.com"
            isSuccess = true
            onComplete(true)
        }
    }

    private fun executeMock(
        action: () -> Unit
    ) {
        if (isLoading) return

        isLoading = true
        isSuccess = false
        errorMessage = null

        viewModelScope.launch {
            try {
                // Giả lập thời gian xử lý.
                delay(500)
                action()
            } catch (error: Exception) {
                errorMessage =
                    "Đã xảy ra lỗi trong quá trình thử nghiệm UI."
            } finally {
                isLoading = false
            }
        }
    }
}
