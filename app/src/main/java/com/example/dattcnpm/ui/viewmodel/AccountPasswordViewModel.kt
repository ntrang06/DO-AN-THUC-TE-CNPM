
package com.example.dattcnpm.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AccountPasswordViewModel : ViewModel() {

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val isSignedIn: Boolean
        get() = auth.currentUser != null

    val hasPasswordProvider: Boolean
        get() = auth.currentUser
            ?.providerData
            ?.any { provider ->
                provider.providerId ==
                        EmailAuthProvider.PROVIDER_ID
            } == true

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    fun savePassword(
        currentPassword: String,
        newPassword: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (isLoading) return

        errorMessage = null

        val user = auth.currentUser

        if (user == null) {
            errorMessage =
                "Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại."
            onComplete(false)
            return
        }

        val email = user.email

        if (!hasPasswordProvider || email.isNullOrBlank()) {
            errorMessage =
                "Tài khoản này chưa có phương thức đăng nhập " +
                        "bằng email và mật khẩu. " +
                        "Chức năng liên kết mật khẩu sẽ được " +
                        "tích hợp riêng."
            onComplete(false)
            return
        }

        when {
            currentPassword.isBlank() -> {
                errorMessage =
                    "Vui lòng nhập mật khẩu hiện tại."
            }

            newPassword.length < 8 ||
                    newPassword.none { it.isLetter() } ||
                    newPassword.none { it.isDigit() } -> {
                errorMessage =
                    "Mật khẩu mới cần ít nhất 8 ký tự, " +
                            "gồm chữ và số."
            }

            newPassword == currentPassword -> {
                errorMessage =
                    "Mật khẩu mới phải khác mật khẩu hiện tại."
            }
        }

        if (errorMessage != null) {
            onComplete(false)
            return
        }

        isLoading = true

        viewModelScope.launch {
            var success = false

            try {
                val originalUid = user.uid

                // Xác thực lại mật khẩu hiện tại.
                val credential = EmailAuthProvider
                    .getCredential(
                        email,
                        currentPassword
                    )

                user.reauthenticate(credential).await()

                check(auth.currentUser?.uid == originalUid) {
                    "Tài khoản đã thay đổi."
                }

                // Đổi mật khẩu trên Firebase.
                user.updatePassword(newPassword).await()

                success = true

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: FirebaseNetworkException) {
                errorMessage =
                    "Không có kết nối Internet. Vui lòng thử lại."
            } catch (error: FirebaseAuthException) {
                errorMessage = when (error.errorCode) {
                    "ERROR_WRONG_PASSWORD",
                    "ERROR_INVALID_CREDENTIAL",
                    "ERROR_INVALID_LOGIN_CREDENTIALS" ->
                        "Mật khẩu hiện tại không chính xác."

                    "ERROR_REQUIRES_RECENT_LOGIN" ->
                        "Firebase yêu cầu đăng nhập lại " +
                                "trước khi đổi mật khẩu."

                    "ERROR_WEAK_PASSWORD" ->
                        "Mật khẩu mới chưa đủ mạnh."

                    "ERROR_USER_DISABLED",
                    "ERROR_USER_TOKEN_EXPIRED",
                    "ERROR_INVALID_USER_TOKEN" ->
                        "Phiên đăng nhập không còn hợp lệ. " +
                                "Vui lòng đăng nhập lại."

                    "ERROR_TOO_MANY_REQUESTS" ->
                        "Bạn đã thử quá nhiều lần. " +
                                "Vui lòng thử lại sau."

                    else ->
                        "Không đổi được mật khẩu. " +
                                "Vui lòng thử lại."
                }
            } catch (error: Exception) {
                errorMessage =
                    "Chưa đổi được mật khẩu. " +
                            "Vui lòng kiểm tra kết nối và thử lại."
            } finally {
                isLoading = false
            }

            onComplete(success)
        }
    }
}
