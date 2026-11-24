
package com.example.dattcnpm.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dattcnpm.data.model.UserProfileManager
import com.example.dattcnpm.data.remote.AuthRepository
import com.example.dattcnpm.data.remote.UserProfileRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.dattcnpm.data.device.DeviceManager
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

    private fun saveLocalProfile(
        context: Context,
        name: String,
        email: String
    ) {
        UserProfileManager.init(context)
        UserProfileManager.name = name
        UserProfileManager.email = email
        UserProfileManager.role = "Người uống thuốc"
        UserProfileManager.setLoggedIn(context, true)
        UserProfileManager.saveProfileInfo(context)
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

        isLoading = true
        isSuccess = false
        errorMessage = null

        viewModelScope.launch {
            var success = false
            var authenticatedUid: String? = null

            try {
                val user = withContext(NonCancellable) {
                    AuthRepository.signIn(address, pass).user
                } ?: error("Không tìm thấy tài khoản đăng nhập.")

                authenticatedUid = user.uid

                val profile = UserProfileRepository
                    .getProfile(user.uid)
                    ?: UserProfileRepository.createProfile(
                        user.uid,
                        user.displayName
                            ?.takeIf { it.isNotBlank() }
                            ?: address.substringBefore("@"),
                        address
                    )

                check(AuthRepository.currentUid == user.uid) {
                    "Phiên đăng nhập đã thay đổi."
                }

                saveLocalProfile(
                    context,
                    profile.displayName.ifBlank {
                        address.substringBefore("@")
                    },
                    profile.email.ifBlank { address }
                )

                success = true
                isSuccess = true

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                errorMessage = translateError(error)
            } finally {
                if (
                    !success &&
                    authenticatedUid != null &&
                    AuthRepository.currentUid == authenticatedUid
                ) {
                    AuthRepository.signOut()
                    UserProfileManager.setLoggedIn(context, false)
                }

                isLoading = false
            }

            onComplete(success)
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

        isLoading = true
        isSuccess = false
        errorMessage = null

        viewModelScope.launch {
            var success = false
            var createdUid: String? = null

            try {
                val user = withContext(NonCancellable) {
                    AuthRepository.signUp(address, pass).user
                } ?: error("Không tạo được tài khoản.")

                createdUid = user.uid

                try {
                    UserProfileRepository.createProfile(
                        user.uid,
                        displayName,
                        address
                    )
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    throw IllegalStateException(
                        "Tài khoản Firebase đã được tạo nhưng " +
                                "chưa lưu được hồ sơ. Hãy đăng nhập " +
                                "lại sau khi Firestore hoạt động.",
                        error
                    )
                }

                check(AuthRepository.currentUid == user.uid) {
                    "Phiên đăng nhập đã thay đổi."
                }

                saveLocalProfile(
                    context,
                    displayName,
                    address
                )

                success = true
                isSuccess = true

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                errorMessage = translateError(error)
            } finally {
                if (
                    !success &&
                    createdUid != null &&
                    AuthRepository.currentUid == createdUid
                ) {
                    AuthRepository.signOut()
                    UserProfileManager.setLoggedIn(context, false)
                }

                isLoading = false
            }

            onComplete(success)
        }
    }

    fun signInWithGoogle(
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        if (isLoading) return

        if (AuthRepository.currentUser != null) {
            errorMessage =
                "Vui lòng đăng xuất tài khoản hiện tại trước."
            onComplete(false)
            return
        }

        val activity = generateSequence(context) {
            (it as? ContextWrapper)?.baseContext
        }.filterIsInstance<Activity>().firstOrNull()

        if (activity == null) {
            errorMessage =
                "Không tìm thấy Activity để đăng nhập Google."
            onComplete(false)
            return
        }

        isLoading = true
        isSuccess = false
        errorMessage = null

        viewModelScope.launch {
            var success = false
            var authenticatedUid: String? = null

            try {
                val clientIdResource =
                    context.resources.getIdentifier(
                        "default_web_client_id",
                        "string",
                        context.packageName
                    )

                check(clientIdResource != 0) {
                    "Chưa có Google Web Client ID. " +
                            "Kiểm tra Firebase và google-services.json."
                }

                val webClientId =
                    context.getString(clientIdResource)

                check(webClientId.isNotBlank()) {
                    "Google Web Client ID không hợp lệ."
                }

                val googleOption =
                    GetSignInWithGoogleOption.Builder(
                        webClientId
                    ).build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()

                val credential = CredentialManager
                    .create(activity)
                    .getCredential(
                        context = activity,
                        request = request
                    )
                    .credential

                check(
                    credential is CustomCredential &&
                            credential.type ==
                            GoogleIdTokenCredential
                                .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    "Không nhận được tài khoản Google hợp lệ."
                }

                val googleIdToken =
                    GoogleIdTokenCredential
                        .createFrom(credential.data)
                        .idToken

                val user = withContext(NonCancellable) {
                    AuthRepository
                        .signInWithGoogle(googleIdToken)
                        .user
                } ?: error(
                    "Không xác định được tài khoản Google."
                )

                authenticatedUid = user.uid

                val userEmail = user.email.orEmpty()

                val userName = user.displayName
                    ?.takeIf { it.isNotBlank() }
                    ?: userEmail.substringBefore("@")
                        .ifBlank { "Người dùng" }

                val profile = UserProfileRepository
                    .getProfile(user.uid)
                    ?: UserProfileRepository.createProfile(
                        user.uid,
                        userName,
                        userEmail
                    )

                check(AuthRepository.currentUid == user.uid) {
                    "Phiên đăng nhập đã thay đổi."
                }

                saveLocalProfile(
                    context,
                    profile.displayName.ifBlank { userName },
                    profile.email.ifBlank { userEmail }
                )

                success = true
                isSuccess = true

            } catch (_: GetCredentialCancellationException) {
                // Người dùng đóng giao diện Google.
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: GetCredentialException) {
                errorMessage =
                    "Không thể đăng nhập Google. " +
                            "Vui lòng kiểm tra tài khoản và thử lại."
            } catch (error: Exception) {
                errorMessage = translateError(error)
            } finally {
                if (
                    !success &&
                    authenticatedUid != null &&
                    AuthRepository.currentUid == authenticatedUid
                ) {
                    AuthRepository.signOut()
                    UserProfileManager.setLoggedIn(context, false)
                }

                isLoading = false
            }

            onComplete(success)
        }
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

        isLoading = true
        isSuccess = false
        errorMessage = null

        viewModelScope.launch {
            var success = false

            try {
                AuthRepository.sendPasswordResetEmail(address)
                success = true
                isSuccess = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                errorMessage = translateError(error)
            } finally {
                isLoading = false
            }

            onComplete(success)
        }
    }


    fun signOut(
        context: Context,
        onComplete: () -> Unit
    ) {
        if (isLoading) return

        isLoading = true
        isSuccess = false
        errorMessage = null

        viewModelScope.launch {
            try {
                // Xóa document thiết bị trước khi đăng xuất Firebase.
                DeviceManager.removeDeviceAndSignOut(
                    context.applicationContext
                )

                isSuccess = true
                onComplete()

            } catch (cancelled: CancellationException) {
                throw cancelled

            } catch (error: Exception) {
                errorMessage =
                    "Chưa gỡ được thiết bị khỏi tài khoản. " +
                            "Vui lòng kiểm tra Internet và thử lại."

                isSuccess = false

            } finally {
                isLoading = false
            }
        }
    }


    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
            return "Không có kết nối Internet. Vui lòng thử lại."
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
                    "Bạn đã thử quá nhiều lần."

                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "Phương thức đăng nhập chưa được bật."

                else ->
                    "Không thể xác thực tài khoản. Vui lòng thử lại."
            }
        }

        return error.message
            ?: "Không thể hoàn tất yêu cầu. Vui lòng thử lại."
    }
}
