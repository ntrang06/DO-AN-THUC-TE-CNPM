
package com.example.dattcnpm.ui.viewmodel

import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dattcnpm.data.model.UserProfileManager
import com.example.dattcnpm.data.remote.AuthRepository
import com.example.dattcnpm.data.remote.UserProfileRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSuccess by mutableStateOf(false)
        private set

    val currentUid: String?
        get() = AuthRepository.currentUid

    val isLoggedIn: Boolean
        get() = AuthRepository.isLoggedIn()

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

        if (!isValidEmail(address) || pass.isBlank()) {
            errorMessage = "Email hoặc mật khẩu không hợp lệ."
            onComplete(false)
            return
        }

        startAction {
            val user = AuthRepository.signIn(address, pass).user
                ?: error("Không tìm thấy tài khoản đăng nhập.")

            val profile = UserProfileRepository.getProfile(user.uid)
                ?: UserProfileRepository.createProfile(
                    user.uid,
                    user.displayName
                        ?.takeIf { it.isNotBlank() }
                        ?: address.substringBefore("@"),
                    address
                )

            UserProfileManager.init(context)
            UserProfileManager.name = profile.displayName
            UserProfileManager.email = profile.email
            UserProfileManager.setLoggedIn(context, true)
            UserProfileManager.saveProfileInfo(context)
        }.invoke(onComplete)
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

            !isValidEmail(address) ->
                errorMessage = "Vui lòng nhập email hợp lệ."

            pass.length < 8 ||
                    pass.none { it.isLetter() } ||
                    pass.none { it.isDigit() } ->
                errorMessage =
                    "Mật khẩu cần ít nhất 8 ký tự, gồm chữ và số."

            else -> errorMessage = null
        }

        if (errorMessage != null) {
            onComplete(false)
            return
        }

        startAction {
            val user = AuthRepository.signUp(address, pass).user
                ?: error("Không tạo được tài khoản.")

            try {
                UserProfileRepository.createProfile(
                    user.uid,
                    displayName,
                    address
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                throw IllegalStateException(
                    "Tài khoản Firebase đã được tạo nhưng chưa " +
                            "lưu được hồ sơ. Không đăng ký lại; " +
                            "hãy đăng nhập sau khi Firestore hoạt động.",
                    error
                )
            }

            UserProfileManager.init(context)
            UserProfileManager.name = displayName
            UserProfileManager.email = address
            UserProfileManager.setLoggedIn(context, true)
            UserProfileManager.saveProfileInfo(context)
        }.invoke(onComplete)
    }

    fun sendPasswordReset(
        email: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (isLoading) return

        val address = email.trim()

        if (!isValidEmail(address)) {
            errorMessage = "Vui lòng nhập email hợp lệ."
            onComplete(false)
            return
        }

        startAction {
            AuthRepository.sendPasswordResetEmail(address)
        }.invoke(onComplete)
    }

    fun signOut(
        context: Context,
        onComplete: () -> Unit
    ) {
        if (isLoading) return

        AuthRepository.signOut()
        UserProfileManager.setLoggedIn(context, false)

        isSuccess = true
        errorMessage = null
        onComplete()
    }

    fun signInWithGoogle(
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        // Chưa nối Credential Manager để lấy Google ID Token.
        // Không giả lập đăng nhập thành công.
        errorMessage =
            "Đăng nhập Google chưa được cấu hình hoàn tất."
        isSuccess = false
        onComplete(false)
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun startAction(
        action: suspend () -> Unit
    ): ((Boolean) -> Unit) -> Unit {
        return { onComplete ->
            if (isLoading) {
                onComplete(false)
            } else {
                isLoading = true
                isSuccess = false
                errorMessage = null

                viewModelScope.launch {
                    var success = false

                    try {
                        action()
                        success = true
                        isSuccess = true
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        errorMessage = translateError(error)
                    } finally {
                        isLoading = false
                    }

                    onComplete(success)
                }
            }
        }
    }

    private fun translateError(error: Exception): String {
        if (
            error is IllegalStateException &&
            error.message?.startsWith(
                "Tài khoản Firebase đã được tạo"
            ) == true
        ) {
            return error.message
                ?: "Chưa lưu được hồ sơ người dùng."
        }

        if (error is FirebaseNetworkException) {
            return "Không có kết nối mạng. Vui lòng thử lại."
        }

        if (error is FirebaseAuthException) {
            return when (error.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" ->
                    "Email này đã được đăng ký."

                "ERROR_INVALID_EMAIL" ->
                    "Địa chỉ email không hợp lệ."

                "ERROR_WRONG_PASSWORD",
                "ERROR_USER_NOT_FOUND",
                "ERROR_INVALID_CREDENTIAL",
                "ERROR_INVALID_LOGIN_CREDENTIALS" ->
                    "Email hoặc mật khẩu không chính xác."

                "ERROR_WEAK_PASSWORD" ->
                    "Mật khẩu chưa đủ mạnh."

                "ERROR_USER_DISABLED" ->
                    "Tài khoản đã bị vô hiệu hóa."

                "ERROR_TOO_MANY_REQUESTS" ->
                    "Bạn thử quá nhiều lần. Vui lòng thử lại sau."

                else ->
                    "Không thể xác thực tài khoản. Vui lòng thử lại."
            }
        }

        return "Không thể hoàn tất yêu cầu. " +
                (error.message ?: "Vui lòng thử lại.")
    }
}
