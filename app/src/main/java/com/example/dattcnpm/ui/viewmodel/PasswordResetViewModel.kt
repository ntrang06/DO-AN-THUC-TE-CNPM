package com.example.dattcnpm.ui.viewmodel

import android.os.SystemClock
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PasswordResetStep {
    EMAIL,
    OTP,
    PASSWORD,
    SUCCESS
}

data class PasswordResetUiState(
    val step: PasswordResetStep = PasswordResetStep.EMAIL,
    val email: String = "",
    val otpCode: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val resendInSeconds: Int = 0,
    val otpExpiresInSeconds: Int = 0
)

class PasswordResetViewModel : ViewModel() {

    var uiState by mutableStateOf(PasswordResetUiState())
        private set

    private var otpDeadline = 0L
    private var resendDeadline = 0L
    private var timerJob: Job? = null

    // OTP cố định để kiểm thử UI.
    private val mockOtp = "123456"

    fun updateEmail(value: String) {
        if (uiState.isLoading ||
            uiState.step != PasswordResetStep.EMAIL
        ) return

        uiState = uiState.copy(
            email = value,
            errorMessage = null
        )
    }

    fun updateOtp(value: String) {
        if (uiState.isLoading ||
            uiState.step != PasswordResetStep.OTP
        ) return

        uiState = uiState.copy(
            otpCode = value.filter { it in '0'..'9' }.take(6),
            errorMessage = null
        )
    }

    fun updateNewPassword(value: String) {
        if (uiState.isLoading ||
            uiState.step != PasswordResetStep.PASSWORD
        ) return

        uiState = uiState.copy(
            newPassword = value,
            errorMessage = null
        )
    }

    fun updateConfirmPassword(value: String) {
        if (uiState.isLoading ||
            uiState.step != PasswordResetStep.PASSWORD
        ) return

        uiState = uiState.copy(
            confirmPassword = value,
            errorMessage = null
        )
    }

    fun sendOtp() {
        if (uiState.isLoading ||
            uiState.step !in setOf(
                PasswordResetStep.EMAIL,
                PasswordResetStep.OTP
            )
        ) return

        refreshTime()

        if (uiState.resendInSeconds > 0) {
            showError(
                "Vui lòng đợi ${uiState.resendInSeconds} giây trước khi gửi lại mã."
            )
            return
        }

        val email = uiState.email.trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Vui lòng nhập địa chỉ email hợp lệ.")
            return
        }

        perform {
            val now = SystemClock.elapsedRealtime()

            otpDeadline = now + 300_000L
            resendDeadline = now + 30_000L

            uiState = uiState.copy(
                step = PasswordResetStep.OTP,
                email = email,
                otpCode = "",
                newPassword = "",
                confirmPassword = "",
                errorMessage = null,
                infoMessage =
                    "Chế độ UI thử nghiệm: mã OTP của bạn là 123456."
            )

            startTimer()
        }
    }

    fun verifyOtp() {
        if (uiState.isLoading ||
            uiState.step != PasswordResetStep.OTP
        ) return

        refreshTime()

        if (uiState.otpExpiresInSeconds == 0) {
            showError("Mã OTP đã hết hạn. Vui lòng gửi lại mã.")
            return
        }

        if (!Regex("[0-9]{6}").matches(uiState.otpCode)) {
            showError("Vui lòng nhập mã OTP gồm đúng 6 chữ số.")
            return
        }

        perform {
            if (uiState.otpCode != mockOtp) {
                showError("Mã OTP không chính xác. Vui lòng thử lại.")
                return@perform
            }

            uiState = uiState.copy(
                step = PasswordResetStep.PASSWORD,
                otpCode = "",
                infoMessage = null
            )
        }
    }

    fun completeReset() {
        if (uiState.isLoading ||
            uiState.step != PasswordResetStep.PASSWORD
        ) return

        val password = uiState.newPassword

        if (
            password.length !in 8..128 ||
            password.none { it.isLetter() } ||
            password.none { it.isDigit() }
        ) {
            showError("Mật khẩu cần từ 8 đến 128 ký tự, gồm chữ và số.")
            return
        }

        if (password != uiState.confirmPassword) {
            showError("Mật khẩu nhập lại chưa khớp.")
            return
        }

        perform {
            timerJob?.cancel()

            otpDeadline = 0L
            resendDeadline = 0L

            uiState = uiState.copy(
                step = PasswordResetStep.SUCCESS,
                otpCode = "",
                newPassword = "",
                confirmPassword = "",
                resendInSeconds = 0,
                otpExpiresInSeconds = 0,
                errorMessage = null,
                infoMessage =
                    "Mật khẩu đã được cập nhật thành công (chế độ UI thử nghiệm)."
            )
        }
    }

    fun changeEmail() {
        if (uiState.isLoading) return

        timerJob?.cancel()
        otpDeadline = 0L
        resendDeadline = 0L

        uiState = uiState.copy(
            step = PasswordResetStep.EMAIL,
            otpCode = "",
            newPassword = "",
            confirmPassword = "",
            errorMessage = null,
            infoMessage = null,
            resendInSeconds = 0,
            otpExpiresInSeconds = 0
        )
    }

    private fun perform(action: suspend () -> Unit) {
        if (uiState.isLoading) return

        uiState = uiState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                // Giả lập thời gian xử lý.
                delay(500)
                action()
            } catch (error: Exception) {
                showError("Chưa thực hiện được yêu cầu. Vui lòng thử lại.")
            } finally {
                uiState = uiState.copy(isLoading = false)
                refreshTime()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        refreshTime()

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                refreshTime()

                if (
                    uiState.resendInSeconds == 0 &&
                    uiState.otpExpiresInSeconds == 0
                ) break
            }
        }
    }

    private fun refreshTime() {
        val now = SystemClock.elapsedRealtime()

        uiState = uiState.copy(
            resendInSeconds = remainingSeconds(resendDeadline, now),
            otpExpiresInSeconds = remainingSeconds(otpDeadline, now)
        )
    }

    private fun remainingSeconds(
        deadline: Long,
        now: Long
    ): Int {
        return ((deadline - now).coerceAtLeast(0L) + 999L)
            .div(1000L)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
    }

    private fun showError(message: String) {
        uiState = uiState.copy(errorMessage = message)
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
