
package com.example.dattcnpm.ui.navigation

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dattcnpm.data.model.InitialSetupPreferences
import com.example.dattcnpm.data.model.UserProfileManager
import com.example.dattcnpm.ui.screens.account.*
import com.example.dattcnpm.ui.screens.auth.EmailLoginScreen
import com.example.dattcnpm.ui.screens.auth.FirebasePasswordResetScreen
import com.example.dattcnpm.ui.screens.auth.LoginScreen
import com.example.dattcnpm.ui.screens.auth.PhoneLoginScreen
import com.example.dattcnpm.ui.screens.auth.RegisterScreen
import com.example.dattcnpm.ui.screens.auth.WelcomeScreen
import com.example.dattcnpm.ui.screens.family.ConnectFamilyScreen
import com.example.dattcnpm.ui.screens.family.FamilyScreen
import com.example.dattcnpm.ui.screens.family.RelativeTrackingScreen
import com.example.dattcnpm.ui.screens.home.HomeScreen
import com.example.dattcnpm.ui.screens.scan.ScanScreen
import com.example.dattcnpm.ui.screens.setup.SetupDisplayPreferenceScreen
import com.example.dattcnpm.ui.screens.setup.SetupTargetUserScreen
import com.example.dattcnpm.ui.viewmodel.AuthViewModel

@Composable
fun MMRANavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    UserProfileManager.init(context)

    val authViewModel: AuthViewModel = viewModel()

    var setupPreferences by remember {
        mutableStateOf(InitialSetupPreferences())
    }

    val showNotAvailable: (String) -> Unit = { feature ->
        Toast.makeText(
            context,
            "$feature chưa được tích hợp Firebase.",
            Toast.LENGTH_SHORT
        ).show()
    }

    val goBack: () -> Unit = {
        navController.popBackStack()
    }

    val openRegister: () -> Unit = {
        if (!authViewModel.isLoading) {
            authViewModel.clearError()
            navController.navigate(MMRADestinations.REGISTER)
        }
    }

    val navigateAfterLogin: () -> Unit = navigate@{
        if (!authViewModel.isLoggedIn) {
            Toast.makeText(
                context,
                "Bạn cần đăng nhập trước.",
                Toast.LENGTH_SHORT
            ).show()
            return@navigate
        }

        UserProfileManager.setLoggedIn(context, true)
        UserProfileManager.saveProfileInfo(context)

        val destination = if (
            UserProfileManager.isSetupCompleted(context)
        ) {
            MMRADestinations.HOME
        } else {
            MMRADestinations.INITIAL_SETUP_TARGET
        }

        navController.navigate(destination) {
            popUpTo(MMRADestinations.LOGIN) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val googleSignIn: () -> Unit = {
        if (!authViewModel.isLoading) {
            authViewModel.signInWithGoogle(context) { success ->
                if (success) {
                    navigateAfterLogin()
                }
            }
        }
    }

    // Trang chủ và lịch uống mở cùng một màn chi tiết liều.
    val openDose: (String) -> Unit = { doseId ->
        navController.navigate(
            "reminder/${Uri.encode(doseId)}?readOnly=false"
        ) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = MMRADestinations.LOGIN,
        modifier = modifier
    ) {

        // WELCOME
        composable(MMRADestinations.WELCOME) {
            WelcomeScreen(
                onLoginClick = {
                    authViewModel.clearError()
                    navController.navigate(MMRADestinations.LOGIN)
                },
                onRegisterClick = openRegister
            )
        }

        // LOGIN
        composable(MMRADestinations.LOGIN) {
            LoginScreen(
                onBackClick = goBack,
                onGoogleSignInClick = googleSignIn,
                onPhoneSignInClick = {
                    showNotAvailable("Đăng nhập số điện thoại")
                },
                onEmailSignInClick = {
                    if (!authViewModel.isLoading) {
                        authViewModel.clearError()
                        navController.navigate(
                            MMRADestinations.EMAIL_LOGIN
                        )
                    }
                },
                onRegisterClick = openRegister
            )

            authViewModel.errorMessage?.let { message ->
                AuthErrorDialog(
                    message = message,
                    onDismiss = authViewModel::clearError
                )
            }
        }

        // REGISTER
        composable(MMRADestinations.REGISTER) {
            RegisterScreen(
                onBackClick = {
                    if (!authViewModel.isLoading) {
                        goBack()
                    }
                },
                onRegisterClick = { fullName, email, password ->
                    authViewModel.signUp(
                        name = fullName,
                        email = email,
                        pass = password,
                        context = context
                    ) { success ->
                        if (success) {
                            navigateAfterLogin()
                        }
                    }
                },
                onGoogleClick = googleSignIn,
                onLoginClick = {
                    if (!authViewModel.isLoading) {
                        authViewModel.clearError()
                        navController.navigate(
                            MMRADestinations.LOGIN
                        ) {
                            popUpTo(MMRADestinations.REGISTER) {
                                inclusive = true
                            }
                        }
                    }
                },
                isLoading = authViewModel.isLoading,
                externalError = authViewModel.errorMessage
            )
        }

        // EMAIL LOGIN
        composable(MMRADestinations.EMAIL_LOGIN) {
            EmailLoginScreen(
                onBackClick = {
                    if (!authViewModel.isLoading) {
                        goBack()
                    }
                },
                onLoginClick = { email, password ->
                    authViewModel.signIn(
                        email = email,
                        pass = password,
                        context = context
                    ) { success ->
                        if (success) {
                            navigateAfterLogin()
                        }
                    }
                },
                onOtpLoginClick = {
                    showNotAvailable("Đăng nhập bằng OTP")
                },
                onGoogleLoginClick = googleSignIn,
                onForgotPasswordClick = {
                    if (!authViewModel.isLoading) {
                        authViewModel.clearError()
                        navController.navigate(
                            MMRADestinations.FORGOT_PASSWORD
                        )
                    }
                },
                onRegisterClick = openRegister,
                isLoading = authViewModel.isLoading,
                externalError = authViewModel.errorMessage
            )
        }

        // PHONE LOGIN - CHƯA TÍCH HỢP
        composable(MMRADestinations.PHONE_LOGIN) {
            PhoneLoginScreen(
                onBackClick = goBack,
                onLoginClick = {
                    showNotAvailable("Đăng nhập số điện thoại")
                },
                onOtpLoginClick = {
                    showNotAvailable("Xác thực OTP điện thoại")
                },
                onForgotPasswordClick = {
                    navController.navigate(
                        MMRADestinations.FORGOT_PASSWORD
                    )
                },
                onRegisterClick = openRegister
            )
        }

        composable(MMRADestinations.VERIFY_EMAIL) {
            PlaceholderScreen(
                title = "Xác thực Email",
                actionLabel = "Quay lại",
                onActionClick = goBack
            )
        }

        composable(MMRADestinations.VERIFY_PHONE) {
            PlaceholderScreen(
                title = "Xác thực số điện thoại",
                actionLabel = "Quay lại",
                onActionClick = goBack
            )
        }

        // FIREBASE PASSWORD RESET
        composable(MMRADestinations.FORGOT_PASSWORD) {
            FirebasePasswordResetScreen(
                onBackClick = {
                    if (!authViewModel.isLoading) {
                        authViewModel.clearError()
                        goBack()
                    }
                },
                onSendEmail = { email, onComplete ->
                    authViewModel.sendPasswordReset(
                        email = email,
                        onComplete = onComplete
                    )
                },
                isLoading = authViewModel.isLoading,
                externalError = authViewModel.errorMessage
            )
        }

        // INITIAL SETUP - TARGET USER
        composable(MMRADestinations.INITIAL_SETUP_TARGET) {
            if (authViewModel.isLoggedIn) {
                SetupTargetUserScreen(
                    initialSelectedRoles =
                        setupPreferences.selectedRoles,
                    onBackClick = {},
                    onContinueClick = { roles ->
                        setupPreferences = setupPreferences.copy(
                            selectedRoles = roles
                        )

                        navController.navigate(
                            MMRADestinations.INITIAL_SETUP_DISPLAY
                        ) {
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                AuthenticationRequiredScreen(
                    onLoginClick = {
                        navController.navigate(
                            MMRADestinations.LOGIN
                        ) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        // INITIAL SETUP - DISPLAY
        composable(MMRADestinations.INITIAL_SETUP_DISPLAY) {
            if (authViewModel.isLoggedIn) {
                SetupDisplayPreferenceScreen(
                    initialLargeText =
                        setupPreferences.isLargeTextEnabled,
                    initialVoiceGuidance =
                        setupPreferences.isVoiceGuidanceEnabled,
                    onBackClick = goBack,
                    onCompleteClick = { isLargeText, isVoiceGuidance ->
                        if (!authViewModel.isLoggedIn) {
                            Toast.makeText(
                                context,
                                "Phiên đăng nhập đã kết thúc.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            setupPreferences = setupPreferences.copy(
                                isLargeTextEnabled = isLargeText,
                                isVoiceGuidanceEnabled = isVoiceGuidance
                            )

                            UserProfileManager.setSetupCompleted(
                                context,
                                true
                            )
                            UserProfileManager.setLoggedIn(
                                context,
                                true
                            )
                            UserProfileManager.saveProfileInfo(context)

                            navController.navigate(
                                MMRADestinations.HOME
                            ) {
                                popUpTo(
                                    MMRADestinations.INITIAL_SETUP_TARGET
                                ) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            } else {
                AuthenticationRequiredScreen(
                    onLoginClick = {
                        navController.navigate(
                            MMRADestinations.LOGIN
                        ) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        // HOME
        composable(MMRADestinations.HOME) {
            HomeScreen(
                onViewAllClick = {
                    navController.navigate(
                        MMRADestinations.SCHEDULE
                    ) {
                        launchSingleTop = true
                    }
                },
                onViewDoseClick = openDose,
                onAccountClick = {
                    navController.navigate(
                        MMRADestinations.MY_ACCOUNT
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // MEDICINE / SCHEDULE / HISTORY
        personalMedicationRoutes(navController, context)

        // SCAN
        composable(MMRADestinations.SCAN) {
            ScanScreen(
                onScanSuccess = { result ->
                    navController.navigate(
                        MMRADestinations.MANUAL_MEDICINE
                    ) {
                        launchSingleTop = true
                    }

                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            "scanPrefill",
                            Bundle().apply {
                                putString("name", result.medicineName)
                                putString("dosage", result.dosage)
                                putString("form", result.dosageForm)
                            }
                        )
                },
                onManualInputClick = {
                    navController.navigate(
                        MMRADestinations.MANUAL_MEDICINE
                    ) {
                        launchSingleTop = true
                    }
                },
                onClose = {
                    if (!navController.popBackStack()) {
                        navController.navigate(
                            MMRADestinations.HOME
                        ) {
                            popUpTo(MMRADestinations.SCAN) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // FAMILY
        composable(MMRADestinations.FAMILY) {
            FamilyScreen(
                onConnectClick = {
                    navController.navigate(
                        MMRADestinations.FAMILY_INVITE
                    ) {
                        launchSingleTop = true
                    }
                },
                onMemberClick = { memberId ->
                    navController.navigate(
                        MMRADestinations.familyMemberDetail(
                            Uri.encode(memberId)
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // FAMILY MEMBER DETAIL
        composable(MMRADestinations.FAMILY_MEMBER_DETAIL) { entry ->
            val memberId = entry.arguments?.getString("memberId")

            if (memberId != null) {
                RelativeTrackingScreen(
                    navController = navController,
                    memberId = memberId,
                    onBackClick = goBack,
                    onRemindClick = {
                        Toast.makeText(
                            context,
                            "Bản thử giao diện: chưa gửi nhắc nhở đến người thân.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        // FAMILY INVITE
        composable(MMRADestinations.FAMILY_INVITE) {
            ConnectFamilyScreen(
                onBackClick = goBack,
                onScanQRClick = {
                    Toast.makeText(
                        context,
                        "Chức năng quét mã kết nối chưa được tích hợp.",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onSubmitCode = { code ->
                    val message = if (
                        code.equals("MMRA-8899", ignoreCase = true)
                    ) {
                        "Đã nhận mã mẫu. Chưa tạo liên kết thật."
                    } else {
                        "Mã không khớp với mã mẫu."
                    }

                    Toast.makeText(
                        context,
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        // ACCOUNT
        composable(MMRADestinations.ACCOUNT) {
            AccountDestination(navController)
        }

        composable(MMRADestinations.MY_ACCOUNT) {
            AccountDestination(navController)
        }

        // AVATAR OPTIONS
        composable(MMRADestinations.MY_ACCOUNT_AVATAR_OPTIONS) {
            AvatarOptionsScreen(
                avatarBitmap =
                    UserProfileManager.currentAvatarBitmap,
                onTakePhotoClick = {
                    navController.navigate(
                        MMRADestinations.CAPTURE_AVATAR
                    )
                },
                onPickGalleryClick = {
                    navController.navigate(
                        MMRADestinations.GALLERY_PICKER
                    )
                },
                onDeletePhotoClick = {
                    UserProfileManager.deleteAvatar(context)
                    navController.navigate(
                        MMRADestinations.AVATAR_DELETED_SUCCESS
                    )
                },
                onCancelClick = goBack
            )
        }

        // CAPTURE AVATAR
        composable(MMRADestinations.CAPTURE_AVATAR) {
            CaptureAvatarScreen(
                onBackClick = goBack,
                onPhotoCaptured = { bitmap ->
                    UserProfileManager.tempCapturedBitmap = bitmap
                    navController.navigate(
                        MMRADestinations.REVIEW_CAPTURED_PHOTO
                    )
                },
                onCancelClick = goBack
            )
        }

        // REVIEW CAPTURED PHOTO
        composable(MMRADestinations.REVIEW_CAPTURED_PHOTO) {
            ReviewCapturedPhotoScreen(
                capturedBitmap =
                    UserProfileManager.tempCapturedBitmap,
                onBackClick = goBack,
                onContinueCropClick = {
                    navController.navigate(
                        MMRADestinations.CROP_AVATAR
                    )
                },
                onRetakeClick = goBack
            )
        }

        // GALLERY PICKER
        composable(MMRADestinations.GALLERY_PICKER) {
            GalleryPickerScreen(
                onBackClick = goBack,
                onPhotoSelected = { bitmap ->
                    UserProfileManager.tempCapturedBitmap = bitmap
                    navController.navigate(
                        MMRADestinations.CROP_AVATAR
                    )
                },
                onCancelClick = goBack
            )
        }

        // CROP AVATAR
        composable(MMRADestinations.CROP_AVATAR) {
            CropAvatarScreen(
                inputBitmap =
                    UserProfileManager.tempCapturedBitmap,
                onBackClick = goBack,
                onConfirmCrop = { croppedBitmap ->
                    UserProfileManager.saveAvatarBitmap(
                        context,
                        croppedBitmap
                    )
                    navController.navigate(
                        MMRADestinations.AVATAR_UPDATED_SUCCESS
                    )
                }
            )
        }

        // AVATAR UPDATED
        composable(MMRADestinations.AVATAR_UPDATED_SUCCESS) {
            AvatarUpdatedScreen(
                updatedBitmap =
                    UserProfileManager.currentAvatarBitmap,
                onBackToAccountClick = {
                    navController.popBackStack(
                        MMRADestinations.MY_ACCOUNT,
                        false
                    )
                }
            )
        }

        // AVATAR DELETED
        composable(MMRADestinations.AVATAR_DELETED_SUCCESS) {
            AvatarDeletedScreen(
                onBackToAccountClick = {
                    navController.popBackStack(
                        MMRADestinations.MY_ACCOUNT,
                        false
                    )
                }
            )
        }

        // EDIT NAME
        composable(MMRADestinations.EDIT_NAME) {
            EditNameScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() },
                onCancelClick = goBack
            )
        }

        // EDIT EMAIL
        composable(MMRADestinations.EDIT_EMAIL) {
            EditEmailScreen(
                onBackClick = goBack,
                onContinueVerify = { goBack() },
                onCancelClick = goBack
            )
        }

        // EDIT PHONE
        composable(MMRADestinations.EDIT_PHONE) {
            EditPhoneScreen(
                onBackClick = goBack,
                onContinueVerify = { goBack() },
                onCancelClick = goBack
            )
        }

        // CHANGE PASSWORD
        composable(MMRADestinations.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() },
                onForgotPasswordClick = {
                    navController.navigate(
                        MMRADestinations.FORGOT_PASSWORD
                    )
                },
                onCancelClick = goBack
            )
        }

        // NOTIFICATION SETTINGS
        composable(MMRADestinations.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() }
            )
        }

        // ACCESSIBILITY SETTINGS
        composable(MMRADestinations.ACCESSIBILITY_SETTINGS) {
            AccessibilitySettingsScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() },
                onResetClick = {
                    navController.navigate(
                        MMRADestinations.INITIAL_SETUP_TARGET
                    )
                }
            )
        }

        // VOICE GUIDANCE SETTINGS
        composable(MMRADestinations.VOICE_GUIDANCE_SETTINGS) {
            VoiceGuidanceSettingsScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() }
            )
        }

        // MANAGE USAGE MODE
        composable(MMRADestinations.MANAGE_USAGE_MODE) {
            ManageUsageModeScreen(
                initialSelectedRoles =
                    setupPreferences.selectedRoles,
                onBackClick = goBack,
                onCancelClick = goBack,
                onConfirmSave = { updatedRoles ->
                    setupPreferences = setupPreferences.copy(
                        selectedRoles = updatedRoles
                    )
                    goBack()
                }
            )
        }

        // SWITCH ACCOUNT
        composable(MMRADestinations.SWITCH_ACCOUNT) {
            SwitchAccountDestination(
                onBack = goBack,
                onSignedOut = {
                    UserProfileManager.setLoggedIn(context, false)
                    navController.navigate(
                        MMRADestinations.LOGIN
                    ) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // LOGOUT
        composable(MMRADestinations.LOGOUT_CONFIRMATION) {
            LogoutDestination(
                onBack = goBack,
                onSignedOut = {
                    UserProfileManager.setLoggedIn(context, false)
                    navController.navigate(
                        MMRADestinations.WELCOME
                    ) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun AccountDestination(
    navController: NavHostController
) {
    MyAccountScreen(
        avatarBitmap =
            UserProfileManager.currentAvatarBitmap,
        onBackClick = {
            navController.popBackStack()
        },
        onAvatarOptionsClick = {
            navController.navigate(
                MMRADestinations.MY_ACCOUNT_AVATAR_OPTIONS
            )
        },
        onEditNameClick = {
            navController.navigate(MMRADestinations.EDIT_NAME)
        },
        onEditEmailClick = {
            navController.navigate(MMRADestinations.EDIT_EMAIL)
        },
        onEditPhoneClick = {
            navController.navigate(MMRADestinations.EDIT_PHONE)
        },
        onChangePasswordClick = {
            navController.navigate(MMRADestinations.CHANGE_PASSWORD)
        },
        onNotificationsClick = {
            navController.navigate(
                MMRADestinations.NOTIFICATION_SETTINGS
            )
        },
        onAccessibilityClick = {
            navController.navigate(
                MMRADestinations.ACCESSIBILITY_SETTINGS
            )
        },
        onVoiceGuidanceClick = {
            navController.navigate(
                MMRADestinations.VOICE_GUIDANCE_SETTINGS
            )
        },
        onUsageModeClick = {
            navController.navigate(MMRADestinations.MANAGE_USAGE_MODE)
        },
        onSwitchAccountClick = {
            navController.navigate(MMRADestinations.SWITCH_ACCOUNT)
        },
        onLogoutClick = {
            navController.navigate(
                MMRADestinations.LOGOUT_CONFIRMATION
            )
        }
    )
}

@Composable
private fun AuthenticationRequiredScreen(
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Vui lòng đăng nhập",
            style = MaterialTheme.typography.headlineSmall
        )
        TextButton(onClick = onLoginClick) {
            Text("Đến màn đăng nhập")
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        if (actionLabel != null) {
            TextButton(onClick = onActionClick) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun AuthErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Đăng nhập chưa thành công")
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}
