package com.example.dattcnpm.ui.navigation

import android.net.Uri
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dattcnpm.data.model.InitialSetupPreferences
import com.example.dattcnpm.data.model.UserProfileManager
import com.example.dattcnpm.ui.screens.account.*
import com.example.dattcnpm.ui.screens.auth.EmailLoginScreen
import com.example.dattcnpm.ui.screens.auth.LoginScreen
import com.example.dattcnpm.ui.screens.auth.PhoneLoginScreen
import com.example.dattcnpm.ui.screens.auth.RegisterScreen
import com.example.dattcnpm.ui.screens.auth.WelcomeScreen
import com.example.dattcnpm.ui.screens.home.HomeScreen
import com.example.dattcnpm.ui.screens.medicine.ManualMedicineScreen
import com.example.dattcnpm.ui.screens.medicine.MedicineScreen
import com.example.dattcnpm.ui.screens.reminder.DoseReminderScreen
import com.example.dattcnpm.ui.screens.schedule.ScheduleScreen
import com.example.dattcnpm.ui.screens.setup.SetupDisplayPreferenceScreen
import com.example.dattcnpm.ui.screens.setup.SetupTargetUserScreen

@Composable
fun MMRANavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    UserProfileManager.init(context)

    var mockEmail by remember { mutableStateOf("") }
    var mockPassword by remember { mutableStateOf("") }
    var mockName by remember { mutableStateOf("") }
    var registerError by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var setupPreferences by remember { mutableStateOf(InitialSetupPreferences()) }

    val goBack: () -> Unit = { navController.popBackStack() }

    val openRegister: () -> Unit = {
        registerError = null
        navController.navigate(MMRADestinations.REGISTER)
    }

    val navigateAfterLogin: () -> Unit = {
        UserProfileManager.setLoggedIn(context, true)
        UserProfileManager.saveProfileInfo(context)

        val destination = if (UserProfileManager.isSetupCompleted(context)) {
            MMRADestinations.HOME
        } else {
            MMRADestinations.INITIAL_SETUP_TARGET
        }

        navController.navigate(destination) {
            popUpTo(MMRADestinations.LOGIN) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Trang chủ và Lịch uống dùng cùng một đường dẫn đến chi tiết liều.
    val openDose: (String) -> Unit = { doseId ->
        navController.navigate("reminder/${Uri.encode(doseId)}?readOnly=false") {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = MMRADestinations.LOGIN,
        modifier = modifier
    ) {
        composable(MMRADestinations.WELCOME) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(MMRADestinations.LOGIN) },
                onRegisterClick = openRegister
            )
        }

        composable(MMRADestinations.LOGIN) {
            LoginScreen(
                onBackClick = goBack,
                onGoogleSignInClick = {},
                onPhoneSignInClick = {
                    navController.navigate(MMRADestinations.PHONE_LOGIN)
                },
                onEmailSignInClick = {
                    loginError = null
                    navController.navigate(MMRADestinations.EMAIL_LOGIN)
                },
                onRegisterClick = openRegister
            )
        }

        composable(MMRADestinations.REGISTER) {
            RegisterScreen(
                onBackClick = goBack,
                onRegisterClick = { fullName, email, password ->
                    val cleanEmail = email.trim()
                    if (mockEmail.isNotBlank() &&
                        mockEmail.equals(cleanEmail, ignoreCase = true)
                    ) {
                        registerError = "Email này đã được đăng ký."
                    } else {
                        mockName = fullName
                        mockEmail = cleanEmail
                        mockPassword = password
                        registerError = null
                        loginError = null
                        navController.navigate(MMRADestinations.EMAIL_LOGIN) {
                            popUpTo(MMRADestinations.REGISTER) { inclusive = true }
                        }
                    }
                },
                onGoogleClick = {},
                onLoginClick = {
                    navController.navigate(MMRADestinations.LOGIN) {
                        popUpTo(MMRADestinations.REGISTER) { inclusive = true }
                    }
                },
                externalError = registerError
            )
        }

        composable(MMRADestinations.EMAIL_LOGIN) {
            EmailLoginScreen(
                onBackClick = goBack,
                onLoginClick = { email, password ->
                    val isValidAccount = mockEmail.isNotBlank() &&
                            email.equals(mockEmail, ignoreCase = true) &&
                            password == mockPassword

                    if (isValidAccount) {
                        UserProfileManager.name = mockName
                        UserProfileManager.email = mockEmail
                        UserProfileManager.role = "Người uống thuốc"
                        loginError = null
                        navigateAfterLogin()
                    } else {
                        loginError = "Email hoặc mật khẩu không chính xác."
                    }
                },
                onOtpLoginClick = {
                    navController.navigate(MMRADestinations.VERIFY_EMAIL)
                },
                onGoogleLoginClick = {},
                onForgotPasswordClick = {
                    navController.navigate(MMRADestinations.FORGOT_PASSWORD)
                },
                onRegisterClick = openRegister,
                externalError = loginError
            )
        }

        composable(MMRADestinations.PHONE_LOGIN) {
            PhoneLoginScreen(
                onBackClick = goBack,
                onLoginClick = {
                    UserProfileManager.name = "Người dùng"
                    UserProfileManager.phone = "0912345678"
                    UserProfileManager.email = ""
                    UserProfileManager.role = "Người uống thuốc"
                    navigateAfterLogin()
                },
                onOtpLoginClick = {
                    navController.navigate(MMRADestinations.VERIFY_PHONE)
                },
                onForgotPasswordClick = {
                    navController.navigate(MMRADestinations.FORGOT_PASSWORD)
                },
                onRegisterClick = openRegister
            )
        }

        composable(MMRADestinations.VERIFY_EMAIL) {
            PlaceholderScreen("Xác thực Email", "Quay lại", goBack)
        }

        composable(MMRADestinations.VERIFY_PHONE) {
            PlaceholderScreen("Xác thực số điện thoại", "Quay lại", goBack)
        }

        composable(MMRADestinations.FORGOT_PASSWORD) {
            PlaceholderScreen("Quên mật khẩu", "Quay lại", goBack)
        }

        composable(MMRADestinations.INITIAL_SETUP_TARGET) {
            SetupTargetUserScreen(
                initialSelectedRoles = setupPreferences.selectedRoles,
                onBackClick = {},
                onContinueClick = { roles ->
                    setupPreferences = setupPreferences.copy(selectedRoles = roles)
                    navController.navigate(MMRADestinations.INITIAL_SETUP_DISPLAY) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(MMRADestinations.INITIAL_SETUP_DISPLAY) {
            SetupDisplayPreferenceScreen(
                initialLargeText = setupPreferences.isLargeTextEnabled,
                initialVoiceGuidance = setupPreferences.isVoiceGuidanceEnabled,
                onBackClick = goBack,
                onCompleteClick = { isLargeText, isVoiceGuidance ->
                    setupPreferences = setupPreferences.copy(
                        isLargeTextEnabled = isLargeText,
                        isVoiceGuidanceEnabled = isVoiceGuidance
                    )
                    UserProfileManager.setSetupCompleted(context, true)
                    UserProfileManager.setLoggedIn(context, true)
                    UserProfileManager.saveProfileInfo(context)
                    navController.navigate(MMRADestinations.HOME) {
                        popUpTo(MMRADestinations.INITIAL_SETUP_TARGET) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(MMRADestinations.HOME) {
            HomeScreen(
                onViewAllClick = {
                    navController.navigate(MMRADestinations.SCHEDULE) {
                        launchSingleTop = true
                    }
                },
                onViewDoseClick = openDose,
                onAccountClick = {
                    navController.navigate(MMRADestinations.MY_ACCOUNT) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(MMRADestinations.MEDICINE) {
            MedicineScreen(
                onMedicineClick = { medicineId ->
                    navController.navigate("medicine/edit/${Uri.encode(medicineId)}") {
                        launchSingleTop = true
                    }
                },
                onAddMedicineClick = {
                    navController.navigate(MMRADestinations.MANUAL_MEDICINE) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(MMRADestinations.MANUAL_MEDICINE) {
            ManualMedicineScreen(
                medicineId = null,
                onClose = goBack
            )
        }

        composable(
            route = MMRADestinations.EDIT_MEDICINE,
            arguments = listOf(
                navArgument("medicineId") { type = NavType.StringType }
            )
        ) { entry ->
            ManualMedicineScreen(
                medicineId = entry.arguments?.getString("medicineId").orEmpty(),
                onClose = goBack
            )
        }

        composable(MMRADestinations.SCHEDULE) {
            ScheduleScreen(onDoseClick = openDose)
        }

        composable(
            route = MMRADestinations.REMINDER,
            arguments = listOf(
                navArgument("doseId") {
                    type = NavType.StringType
                },
                navArgument("readOnly") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entry ->
            DoseReminderScreen(
                doseId = entry.arguments?.getString("doseId"),
                readOnly = entry.arguments?.getBoolean("readOnly") ?: false,
                onBack = goBack
            )
        }

        composable(MMRADestinations.SCAN) {
            PlaceholderScreen("Quét thuốc", "Quay lại", goBack)
        }

        composable(MMRADestinations.HISTORY) {
            PlaceholderScreen("Lịch sử")
        }

        composable(MMRADestinations.FAMILY) {
            PlaceholderScreen("Gia đình")
        }

        composable(MMRADestinations.ACCOUNT) {
            AccountDestination(navController)
        }

        composable(MMRADestinations.MY_ACCOUNT) {
            AccountDestination(navController)
        }

        composable(MMRADestinations.MY_ACCOUNT_AVATAR_OPTIONS) {
            AvatarOptionsScreen(
                avatarBitmap = UserProfileManager.currentAvatarBitmap,
                onTakePhotoClick = {
                    navController.navigate(MMRADestinations.CAPTURE_AVATAR)
                },
                onPickGalleryClick = {
                    navController.navigate(MMRADestinations.GALLERY_PICKER)
                },
                onDeletePhotoClick = {
                    UserProfileManager.deleteAvatar(context)
                    navController.navigate(MMRADestinations.AVATAR_DELETED_SUCCESS)
                },
                onCancelClick = goBack
            )
        }

        composable(MMRADestinations.CAPTURE_AVATAR) {
            CaptureAvatarScreen(
                onBackClick = goBack,
                onPhotoCaptured = { bitmap ->
                    UserProfileManager.tempCapturedBitmap = bitmap
                    navController.navigate(MMRADestinations.REVIEW_CAPTURED_PHOTO)
                },
                onCancelClick = goBack
            )
        }

        composable(MMRADestinations.REVIEW_CAPTURED_PHOTO) {
            ReviewCapturedPhotoScreen(
                capturedBitmap = UserProfileManager.tempCapturedBitmap,
                onBackClick = goBack,
                onContinueCropClick = {
                    navController.navigate(MMRADestinations.CROP_AVATAR)
                },
                onRetakeClick = goBack
            )
        }

        composable(MMRADestinations.GALLERY_PICKER) {
            GalleryPickerScreen(
                onBackClick = goBack,
                onPhotoSelected = { bitmap ->
                    UserProfileManager.tempCapturedBitmap = bitmap
                    navController.navigate(MMRADestinations.CROP_AVATAR)
                },
                onCancelClick = goBack
            )
        }

        composable(MMRADestinations.CROP_AVATAR) {
            CropAvatarScreen(
                inputBitmap = UserProfileManager.tempCapturedBitmap,
                onBackClick = goBack,
                onConfirmCrop = { croppedBitmap ->
                    UserProfileManager.saveAvatarBitmap(context, croppedBitmap)
                    navController.navigate(MMRADestinations.AVATAR_UPDATED_SUCCESS)
                }
            )
        }

        composable(MMRADestinations.AVATAR_UPDATED_SUCCESS) {
            AvatarUpdatedScreen(
                updatedBitmap = UserProfileManager.currentAvatarBitmap,
                onBackToAccountClick = {
                    navController.popBackStack(MMRADestinations.MY_ACCOUNT, false)
                }
            )
        }

        composable(MMRADestinations.AVATAR_DELETED_SUCCESS) {
            AvatarDeletedScreen(
                onBackToAccountClick = {
                    navController.popBackStack(MMRADestinations.MY_ACCOUNT, false)
                }
            )
        }

        composable(MMRADestinations.EDIT_NAME) {
            EditNameScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() },
                onCancelClick = goBack
            )
        }

        composable(MMRADestinations.EDIT_EMAIL) {
            EditEmailScreen(
                onBackClick = goBack,
                onContinueVerify = { goBack() },
                onCancelClick = goBack
            )
        }

        composable(MMRADestinations.EDIT_PHONE) {
            EditPhoneScreen(
                onBackClick = goBack,
                onContinueVerify = { goBack() },
                onCancelClick = goBack
            )
        }

        composable(MMRADestinations.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() },
                onForgotPasswordClick = {
                    navController.navigate(MMRADestinations.FORGOT_PASSWORD)
                },
                onCancelClick = goBack
            )
        }

        composable(MMRADestinations.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() }
            )
        }

        composable(MMRADestinations.ACCESSIBILITY_SETTINGS) {
            AccessibilitySettingsScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() },
                onResetClick = {
                    navController.navigate(MMRADestinations.INITIAL_SETUP_TARGET)
                }
            )
        }

        composable(MMRADestinations.VOICE_GUIDANCE_SETTINGS) {
            VoiceGuidanceSettingsScreen(
                onBackClick = goBack,
                onSaveSuccess = { goBack() }
            )
        }

        composable(MMRADestinations.MANAGE_USAGE_MODE) {
            ManageUsageModeScreen(
                initialSelectedRoles = setupPreferences.selectedRoles,
                onBackClick = goBack,
                onCancelClick = goBack,
                onConfirmSave = { updatedRoles ->
                    setupPreferences = setupPreferences.copy(selectedRoles = updatedRoles)
                    goBack()
                }
            )
        }

        composable(MMRADestinations.SWITCH_ACCOUNT) {
            SwitchAccountDestination(
                onBack = goBack,
                onSignedOut = {
                    UserProfileManager.setLoggedIn(context, false)
                    navController.navigate(MMRADestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(MMRADestinations.LOGOUT_CONFIRMATION) {
            LogoutDestination(
                onBack = goBack,
                onSignedOut = {
                    UserProfileManager.setLoggedIn(context, false)
                    navController.navigate(MMRADestinations.WELCOME) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun AccountDestination(navController: NavHostController) {
    MyAccountScreen(
        avatarBitmap = UserProfileManager.currentAvatarBitmap,
        onBackClick = { navController.popBackStack() },
        onAvatarOptionsClick = {
            navController.navigate(MMRADestinations.MY_ACCOUNT_AVATAR_OPTIONS)
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
            navController.navigate(MMRADestinations.NOTIFICATION_SETTINGS)
        },
        onAccessibilityClick = {
            navController.navigate(MMRADestinations.ACCESSIBILITY_SETTINGS)
        },
        onVoiceGuidanceClick = {
            navController.navigate(MMRADestinations.VOICE_GUIDANCE_SETTINGS)
        },
        onUsageModeClick = {
            navController.navigate(MMRADestinations.MANAGE_USAGE_MODE)
        },
        onSwitchAccountClick = {
            navController.navigate(MMRADestinations.SWITCH_ACCOUNT)
        },
        onLogoutClick = {
            navController.navigate(MMRADestinations.LOGOUT_CONFIRMATION)
        }
    )
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
        Text(title, style = MaterialTheme.typography.headlineSmall)

        if (actionLabel != null) {
            TextButton(onClick = onActionClick) {
                Text(actionLabel)
            }
        }
    }
}