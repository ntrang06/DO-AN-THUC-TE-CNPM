package com.example.dattcnpm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AccountPasswordViewModel : ViewModel() {

    // MOCK DATA: Giả lập trạng thái tài khoản đã đăng nhập.
    var isSignedIn by mutableStateOf(true)
        private set

    // MOCK DATA:
    // true: Tài khoản đã có mật khẩu.
    // false: Tài khoản chưa có mật khẩu (ví dụ đăng nhập Google).
    var hasPasswordProvider by mutableStateOf(true)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    // Dùng để thay đổi trạng thái tài khoản khi kiểm thử UI.
    fun setMockAccountState(
        signedIn: Boolean,
        hasPassword: Boolean
    ) {
        isSignedIn = signedIn
        hasPasswordProvider = hasPassword
        clearError()
    }

    fun savePassword(
        currentPassword: String,
        newPassword: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (isLoading) return

        errorMessage = null

        // Kiểm tra dữ liệu đầu vào.
        when {
            !isSignedIn -> {
                errorMessage = "Vui lòng đăng nhập trước khi đặt mật khẩu."
                onComplete(false)
                return
            }

            hasPasswordProvider && currentPassword.isBlank() -> {
                errorMessage = "Vui lòng nhập mật khẩu hiện tại."
                onComplete(false)
                return
            }

            newPassword.length < 8 ||
                    !newPassword.any { it.isLetter() } ||
                    !newPassword.any { it.isDigit() } -> {
                errorMessage =
                    "Mật khẩu mới cần ít nhất 8 ký tự, gồm chữ và số."
                onComplete(false)
                return
            }
        }

        isLoading = true

        viewModelScope.launch {
            var success = false

            try {
                hasPasswordProvider = true
                success = true

            } catch (error: Exception) {
                errorMessage =
                    "Chưa lưu được mật khẩu. Vui lòng thử lại."
            } finally {
                isLoading = false
            }

            onComplete(success)
        }
    }
}
