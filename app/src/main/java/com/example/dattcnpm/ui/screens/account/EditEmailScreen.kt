
package com.example.dattcnpm.ui.screens.account

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dattcnpm.R
import com.example.dattcnpm.data.model.UserProfileManager
import com.example.dattcnpm.data.remote.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val EmailPrimary = Color(0xFF0F5B51)
private val EmailLight = Color(0xFFE0F0EC)
private val EmailDark = Color(0xFF0A3C35)
private val EmailSubText = Color(0xFF5A6E6A)
private val EmailBorder = Color(0xFFE2E8E6)

@Composable
fun EditEmailScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onContinueVerify: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var newEmailInput by remember {
        mutableStateOf("")
    }

    var pendingEmail by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var infoMessage by remember {
        mutableStateOf<String?>(null)
    }

    val currentEmail =
        FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    // Gửi liên kết xác minh email mới.
    val sendVerification: () -> Unit = send@{
        if (isLoading) return@send

        val email = newEmailInput.trim()
        val currentUser =
            FirebaseAuth.getInstance().currentUser

        when {
            currentUser == null -> {
                errorMessage =
                    "Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại."
                return@send
            }

            !Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches() -> {
                errorMessage =
                    "Vui lòng nhập địa chỉ email mới hợp lệ."
                return@send
            }

            email.equals(
                currentUser.email,
                ignoreCase = true
            ) -> {
                errorMessage =
                    "Email mới phải khác email hiện tại."
                return@send
            }
        }

        isLoading = true
        errorMessage = null
        infoMessage = null

        scope.launch {
            try {
                currentUser.verifyBeforeUpdateEmail(
                    email
                ).await()

                pendingEmail = email

                infoMessage =
                    "Đã gửi liên kết xác minh đến email mới. " +
                            "Vui lòng kiểm tra hộp thư và Spam, " +
                            "nhấn liên kết rồi quay lại ứng dụng."

            } catch (cancelled: CancellationException) {
                throw cancelled

            } catch (
                error: FirebaseAuthRecentLoginRequiredException
            ) {
                errorMessage =
                    "Firebase yêu cầu xác thực lại tài khoản. " +
                            "Vui lòng đăng xuất, đăng nhập lại rồi thử tiếp."

            } catch (error: Exception) {
                errorMessage =
                    "Không gửi được liên kết xác minh. " +
                            "Vui lòng kiểm tra email và kết nối Internet."

            } finally {
                isLoading = false
            }
        }
    }

    // Kiểm tra email đã được Firebase thay đổi chưa.
    val confirmVerification: () -> Unit = confirm@{
        if (isLoading) return@confirm

        val expectedEmail = pendingEmail

        if (expectedEmail.isNullOrBlank()) {
            errorMessage =
                "Vui lòng gửi liên kết xác minh email trước."
            return@confirm
        }

        val currentUser =
            FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            errorMessage =
                "Phiên đăng nhập đã kết thúc. " +
                        "Vui lòng đăng nhập lại bằng email mới nếu đã xác minh."
            return@confirm
        }

        val originalUid = currentUser.uid

        isLoading = true
        errorMessage = null
        infoMessage = null

        scope.launch {
            try {
                // Làm mới thông tin Firebase Authentication.
                currentUser.reload().await()

                val refreshedUser =
                    FirebaseAuth.getInstance().currentUser
                        ?: error(
                            "Phiên đăng nhập đã kết thúc."
                        )

                check(refreshedUser.uid == originalUid) {
                    "Tài khoản đã thay đổi."
                }

                val verifiedEmail =
                    refreshedUser.email.orEmpty()

                if (
                    !verifiedEmail.equals(
                        expectedEmail,
                        ignoreCase = true
                    ) ||
                    !refreshedUser.isEmailVerified
                ) {
                    errorMessage =
                        "Email mới chưa được Firebase xác minh. " +
                                "Vui lòng mở liên kết trong email " +
                                "rồi thử lại."

                } else {
                    // Làm mới token trước khi ghi Firestore.
                    refreshedUser.getIdToken(true).await()

                    check(
                        FirebaseAuth.getInstance()
                            .currentUser?.uid == originalUid
                    ) {
                        "Tài khoản đã thay đổi."
                    }

                    // Chỉ cập nhật Firestore khi Firebase
                    // Authentication đã xác nhận email mới.
                    UserProfileRepository.updateProfile(
                        uid = originalUid,
                        updates = mapOf(
                            "email" to verifiedEmail
                        )
                    )

                    check(
                        FirebaseAuth.getInstance()
                            .currentUser?.uid == originalUid
                    ) {
                        "Tài khoản đã thay đổi."
                    }

                    // Đồng bộ dữ liệu trên điện thoại.
                    UserProfileManager.init(context)
                    UserProfileManager.email = verifiedEmail
                    UserProfileManager.saveProfileInfo(context)

                    pendingEmail = null
                    infoMessage = "Đổi email thành công."

                    onContinueVerify()
                }

            } catch (cancelled: CancellationException) {
                throw cancelled

            } catch (error: Exception) {
                errorMessage =
                    "Chưa xác nhận hoặc đồng bộ được email mới. " +
                            "Nếu đã nhấn liên kết xác minh, " +
                            "vui lòng kiểm tra phiên đăng nhập " +
                            "và Firestore Rules rồi thử lại."

            } finally {
                isLoading = false
            }
        }
    }

    val handleBack: () -> Unit = {
        if (!isLoading) {
            onBackClick()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFFAFDFB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp
                )
        ) {

            // HEADER
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = EmailLight,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(
                            enabled = !isLoading,
                            onClick = handleBack
                        )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_back
                            ),
                            contentDescription = "Quay lại",
                            tint = EmailPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Đổi email",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmailDark
                    )

                    Text(
                        text = "Thông tin cá nhân • Xác minh Firebase",
                        fontSize = 13.sp,
                        color = EmailSubText
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ICON
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EmailLight,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_email
                        ),
                        contentDescription = null,
                        tint = EmailPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Đổi email liên kết",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = EmailDark
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Email mới chỉ được sử dụng sau khi " +
                        "Firebase xác minh thành công.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = EmailSubText
            )

            Spacer(Modifier.height(20.dp))

            // EMAIL CARD
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(
                    1.dp,
                    EmailBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        text = "Email hiện tại",
                        fontSize = 13.sp,
                        color = EmailSubText
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = currentEmail.ifBlank {
                            "Chưa đăng nhập"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmailDark
                    )

                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = "Email mới",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmailDark
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newEmailInput,
                        onValueChange = { newValue ->
                            newEmailInput = newValue

                            if (
                                pendingEmail != null &&
                                !newValue.trim().equals(
                                    pendingEmail,
                                    ignoreCase = true
                                )
                            ) {
                                pendingEmail = null
                            }

                            errorMessage = null
                            infoMessage = null
                        },
                        enabled = !isLoading,
                        singleLine = true,
                        label = {
                            Text("Nhập email mới")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMessage?.let { message ->
                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = message,
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp
                        )
                    }

                    infoMessage?.let { message ->
                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = message,
                            color = EmailPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // SEND VERIFICATION BUTTON
            Button(
                onClick = sendVerification,
                enabled = !isLoading &&
                        pendingEmail == null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmailPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(10.dp))

                    Text("Đang xử lý...")
                } else {
                    Text("Gửi liên kết xác minh")
                }
            }

            Spacer(Modifier.height(12.dp))

            // CONFIRM VERIFICATION BUTTON
            OutlinedButton(
                onClick = confirmVerification,
                enabled = !isLoading &&
                        pendingEmail != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Tôi đã xác minh email",
                    color = EmailPrimary
                )
            }

            Spacer(Modifier.height(20.dp))

            // INFORMATION BOX
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6F4),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_shield_check
                        ),
                        contentDescription = null,
                        tint = EmailPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "Hãy kiểm tra hộp thư và Spam của " +
                                "email mới. Sau khi nhấn liên kết " +
                                "xác minh, quay lại ứng dụng và " +
                                "chọn \"Tôi đã xác minh email\".",
                        fontSize = 13.sp,
                        color = EmailSubText,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // CANCEL BUTTON
            Text(
                text = "Hủy",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = EmailSubText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(enabled = !isLoading) {
                        onCancelClick()
                    }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
