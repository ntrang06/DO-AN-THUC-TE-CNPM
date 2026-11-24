
package com.example.dattcnpm.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ResetPrimaryColor = Color(0xFF0F5B51)
private val ResetBackgroundColor = Color(0xFFFAFDFB)

@Composable
fun FirebasePasswordResetScreen(
    onBackClick: () -> Unit,
    onSendEmail: (String, (Boolean) -> Unit) -> Unit,
    isLoading: Boolean,
    externalError: String?
) {
    var email by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var emailSent by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ResetBackgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {

            TextButton(
                onClick = onBackClick,
                enabled = !isLoading
            ) {
                Text(
                    "Quay lại",
                    color = ResetPrimaryColor
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Quên mật khẩu",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ResetPrimaryColor
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (emailSent) {
                    "Yêu cầu đã được xử lý. " +
                            "Nếu email có tài khoản hợp lệ, " +
                            "hãy kiểm tra hộp thư và Spam " +
                            "để tìm liên kết đặt lại mật khẩu."
                } else {
                    "Nhập email đăng ký tài khoản MMRA. " +
                            "Chúng tôi sẽ gửi liên kết đặt lại mật khẩu."
                },
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(28.dp))

            if (!emailSent) {

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        localError = null
                    },
                    label = {
                        Text("Email đã đăng ký")
                    },
                    placeholder = {
                        Text("example@gmail.com")
                    },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                val displayedError = localError ?: externalError

                if (displayedError != null) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = displayedError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val address = email.trim()

                        if (
                            !Patterns.EMAIL_ADDRESS
                                .matcher(address)
                                .matches()
                        ) {
                            localError =
                                "Vui lòng nhập địa chỉ email hợp lệ."
                        } else {
                            localError = null

                            onSendEmail(address) { success ->
                                if (success) {
                                    emailSent = true
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ResetPrimaryColor
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Gửi email đặt lại mật khẩu")
                    }
                }

            } else {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ResetPrimaryColor
                    )
                ) {
                    Text("Quay về đăng nhập")
                }
            }
        }
    }
}
