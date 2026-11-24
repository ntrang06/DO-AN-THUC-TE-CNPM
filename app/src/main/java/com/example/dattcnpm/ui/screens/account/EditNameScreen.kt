
package com.example.dattcnpm.ui.screens.account

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dattcnpm.R
import com.example.dattcnpm.data.model.UserProfileManager
import com.example.dattcnpm.data.remote.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun EditNameScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaveSuccess: () -> Unit = {},
    onCancelClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val primaryColor = Color(0xFF0F5B51)
    val lightBgColor = Color(0xFFE0F0EC)
    val darkTextColor = Color(0xFF0A3C35)
    val subTextColor = Color(0xFF5A6E6A)
    val borderColor = Color(0xFFE2E8E6)
    val infoBgColor = Color(0xFFEFF6F4)

    var nameInput by remember {
        mutableStateOf(UserProfileManager.name)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var isSaving by remember {
        mutableStateOf(false)
    }

    val saveName: () -> Unit = save@{
        if (isSaving) return@save

        val trimmedName = nameInput.trim()

        if (trimmedName.isBlank()) {
            errorMessage = "Vui lòng nhập họ và tên."
            return@save
        }

        if (trimmedName.length > 100) {
            errorMessage = "Họ và tên không được quá 100 ký tự."
            return@save
        }

        val uid = FirebaseAuth.getInstance()
            .currentUser?.uid

        if (uid.isNullOrBlank()) {
            errorMessage =
                "Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại."
            return@save
        }

        isSaving = true
        errorMessage = null

        scope.launch {
            try {
                // Bước 1: Lưu tên mới lên Firestore.
                UserProfileRepository.updateProfile(
                    uid = uid,
                    updates = mapOf(
                        "displayName" to trimmedName
                    )
                )

                // Bước 2: Đảm bảo tài khoản chưa thay đổi.
                check(
                    FirebaseAuth.getInstance()
                        .currentUser?.uid == uid
                ) {
                    "Tài khoản đã thay đổi. Vui lòng mở lại hồ sơ."
                }

                // Bước 3: Cập nhật dữ liệu trên điện thoại.
                UserProfileManager.init(context)
                UserProfileManager.name = trimmedName
                UserProfileManager.saveProfileInfo(context)

                // Bước 4: Trở về màn Account.
                onSaveSuccess()

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                errorMessage = when {
                    error.message?.contains(
                        "PERMISSION_DENIED",
                        ignoreCase = true
                    ) == true ->
                        "Không có quyền cập nhật hồ sơ. " +
                                "Vui lòng kiểm tra Firestore Rules."

                    error.message?.contains(
                        "UNAVAILABLE",
                        ignoreCase = true
                    ) == true ->
                        "Chưa kết nối được Firestore. " +
                                "Vui lòng kiểm tra Internet."

                    else ->
                        "Chưa lưu được tên. " +
                                "Vui lòng kiểm tra kết nối và thử lại."
                }
            } finally {
                isSaving = false
            }
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

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = lightBgColor,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(enabled = !isSaving) {
                            onBackClick()
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_back
                            ),
                            contentDescription = "Quay lại",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = stringResource(
                            R.string.account_edit_name
                        ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkTextColor
                    )

                    Text(
                        text = stringResource(
                            R.string.account_section_personal
                        ),
                        fontSize = 13.sp,
                        color = subTextColor
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Icon
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = lightBgColor,
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_user
                        ),
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Tên của bạn",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = darkTextColor
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tên giúp người thân nhận ra bạn trong MMRA.",
                fontSize = 14.sp,
                color = subTextColor
            )

            Spacer(Modifier.height(20.dp))

            // Input Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(
                    1.dp,
                    borderColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.label_fullname
                        ),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkTextColor
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            errorMessage = null
                        },
                        enabled = !isSaving,
                        placeholder = {
                            Text(
                                text = stringResource(
                                    R.string.hint_fullname
                                ),
                                color = Color.Gray
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = borderColor,
                            focusedBorderColor = primaryColor
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMessage?.let { message ->
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = message,
                            fontSize = 13.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Nhập đầy đủ họ và tên của bạn.",
                        fontSize = 12.sp,
                        color = subTextColor
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = saveName,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(10.dp))

                    Text("Đang lưu...")
                } else {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_check_circle
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Lưu thay đổi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(
                    R.string.avatar_action_cancel
                ),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = subTextColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(enabled = !isSaving) {
                        onCancelClick()
                    }
            )

            Spacer(Modifier.height(24.dp))

            // Info Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = infoBgColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(
                            R.drawable.ic_info_circle
                        ),
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "Tên mới được đồng bộ với hồ sơ " +
                                "Firebase của bạn. Thay đổi tên " +
                                "không ảnh hưởng đến lịch uống thuốc.",
                        fontSize = 13.sp,
                        color = subTextColor,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditNameScreenPreview() {
    EditNameScreen()
}
