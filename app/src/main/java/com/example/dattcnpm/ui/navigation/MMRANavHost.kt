package com.example.dattcnpm.ui.navigation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.dattcnpm.data.model.InitialSetupPreferences
import com.example.dattcnpm.data.model.UserProfileManager
import com.example.dattcnpm.ui.screens.auth.WelcomeScreen
import com.example.dattcnpm.ui.screens.auth.LoginScreen
import com.example.dattcnpm.ui.screens.auth.RegisterScreen
import com.example.dattcnpm.ui.screens.auth.EmailLoginScreen
import com.example.dattcnpm.ui.screens.auth.PhoneLoginScreen
import com.example.dattcnpm.ui.screens.setup.SetupTargetUserScreen
import com.example.dattcnpm.ui.screens.setup.SetupDisplayPreferenceScreen
import com.example.dattcnpm.ui.screens.home.HomeScreen
import com.example.dattcnpm.ui.screens.account.MyAccountScreen
import com.example.dattcnpm.ui.screens.account.AvatarOptionsScreen
import com.example.dattcnpm.ui.screens.account.AvatarUpdatedScreen
import com.example.dattcnpm.ui.screens.account.AvatarDeletedScreen
import com.example.dattcnpm.ui.screens.account.CaptureAvatarScreen
import com.example.dattcnpm.ui.screens.account.ReviewCapturedPhotoScreen
import com.example.dattcnpm.ui.screens.account.GalleryPickerScreen
import com.example.dattcnpm.ui.screens.account.CropAvatarScreen
import com.example.dattcnpm.ui.screens.account.EditNameScreen
import com.example.dattcnpm.ui.screens.account.EditEmailScreen
import com.example.dattcnpm.ui.screens.account.EditPhoneScreen
import com.example.dattcnpm.ui.screens.account.ChangePasswordScreen
import com.example.dattcnpm.ui.screens.account.NotificationSettingsScreen
import com.example.dattcnpm.ui.screens.account.AccessibilitySettingsScreen
import com.example.dattcnpm.ui.screens.account.VoiceGuidanceSettingsScreen
import com.example.dattcnpm.ui.screens.account.ManageUsageModeScreen

@Composable
fun MMRANavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    UserProfileManager.init(context)
    var mockEmail by remember {
        mutableStateOf("")
    }
    var mockPassword by remember {
        mutableStateOf("")
    }
    var mockName by remember {
        mutableStateOf("")
    }
    var registerError by remember {
        mutableStateOf<String?>(null)
    }
    var loginError by remember {
        mutableStateOf<String?>(null)
    }
    var setupPreferences by remember {
        mutableStateOf(InitialSetupPreferences())
    }
    val startDestination = MMRADestinations.LOGIN
    val navigateAfterLogin: () -> Unit = {
        UserProfileManager.setLoggedIn(context, true)
        UserProfileManager.saveProfileInfo(context)
        val destination =
            if (UserProfileManager.isSetupCompleted(context)) {
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
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(MMRADestinations.WELCOME) {
            WelcomeScreen(
                onLoginClick = {
                    navController.navigate(MMRADestinations.LOGIN)
                },
                onRegisterClick = {
                    registerError = null
                    navController.navigate(MMRADestinations.REGISTER)
                }
            )
        }
        composable(MMRADestinations.LOGIN) {
            LoginScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onGoogleSignInClick = {},
                onPhoneSignInClick = {
                    navController.navigate(MMRADestinations.PHONE_LOGIN)
                },
                onEmailSignInClick = {
                    loginError = null
                    navController.navigate(MMRADestinations.EMAIL_LOGIN)
                },
                onRegisterClick = {
                    registerError = null
                    navController.navigate(MMRADestinations.REGISTER)
                }
            )
        }
        composable(MMRADestinations.REGISTER) {
            RegisterScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onRegisterClick = { fullName, email, password ->
                    val cleanEmail = email.trim()
                    if (
                        mockEmail.isNotBlank() &&
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
                            popUpTo(MMRADestinations.REGISTER) {
                                inclusive = true
                            }
                        }
                    }
                },
                onGoogleClick = {},
                onLoginClick = {
                    navController.navigate(MMRADestinations.LOGIN) {
                        popUpTo(MMRADestinations.REGISTER) {
                            inclusive = true
                        }
                    }
                },
                externalError = registerError
            )
        }
        composable(MMRADestinations.EMAIL_LOGIN) {
            EmailLoginScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = { email, password ->

                    val isValidAccount =
                        email.equals(mockEmail, ignoreCase = true) &&
                                password == mockPassword &&
                                mockEmail.isNotBlank()
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
                onRegisterClick = {
                    registerError = null
                    navController.navigate(MMRADestinations.REGISTER)
                },
                externalError = loginError
            )
        }
        composable(MMRADestinations.PHONE_LOGIN) {
            PhoneLoginScreen(
                onBackClick = {
                    navController.popBackStack()
                },
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
                onRegisterClick = {
                    registerError = null
                    navController.navigate(MMRADestinations.REGISTER)
                }
            )
        }
        composable(MMRADestinations.VERIFY_EMAIL) {
            PlaceholderScreen(
                title = "Xác thực Email",
                actionLabel = "Quay lại",
                onActionClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.VERIFY_PHONE) {
            PlaceholderScreen(
                title = "Xác thực số điện thoại",
                actionLabel = "Quay lại",
                onActionClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.FORGOT_PASSWORD) {
            PlaceholderScreen(
                title = "Quên mật khẩu",
                actionLabel = "Quay lại",
                onActionClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.INITIAL_SETUP_TARGET) {
            SetupTargetUserScreen(
                initialSelectedRoles = setupPreferences.selectedRoles,
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
        }
        composable(MMRADestinations.INITIAL_SETUP_DISPLAY) {
            SetupDisplayPreferenceScreen(
                initialLargeText = setupPreferences.isLargeTextEnabled,
                initialVoiceGuidance = setupPreferences.isVoiceGuidanceEnabled,
                onBackClick = {
                    navController.popBackStack()
                },
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
                onAccountClick = {
                    navController.navigate(MMRADestinations.MY_ACCOUNT) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(MMRADestinations.MEDICINE) {
            PlaceholderScreen(title = "Thuốc")
        }
        composable(MMRADestinations.SCHEDULE) {
            PlaceholderScreen(title = "Lịch uống")
        }
        composable(MMRADestinations.SCAN) {
            PlaceholderScreen(
                title = "Quét thuốc",
                actionLabel = "Quay lại",
                onActionClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.HISTORY) {
            PlaceholderScreen(title = "Lịch sử")
        }
        composable(MMRADestinations.FAMILY) {
            PlaceholderScreen(title = "Gia đình")
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
                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.CAPTURE_AVATAR) {
            CaptureAvatarScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPhotoCaptured = { bitmap ->
                    UserProfileManager.tempCapturedBitmap = bitmap
                    navController.navigate(
                        MMRADestinations.REVIEW_CAPTURED_PHOTO
                    )
                },
                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.REVIEW_CAPTURED_PHOTO) {
            ReviewCapturedPhotoScreen(
                capturedBitmap = UserProfileManager.tempCapturedBitmap,
                onBackClick = {
                    navController.popBackStack()
                },
                onContinueCropClick = {
                    navController.navigate(
                        MMRADestinations.CROP_AVATAR
                    )
                },
                onRetakeClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.GALLERY_PICKER) {
            GalleryPickerScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPhotoSelected = { bitmap ->
                    UserProfileManager.tempCapturedBitmap = bitmap
                    navController.navigate(
                        MMRADestinations.CROP_AVATAR
                    )
                },
                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.CROP_AVATAR) {
            CropAvatarScreen(
                inputBitmap = UserProfileManager.tempCapturedBitmap,
                onBackClick = {
                    navController.popBackStack()
                },
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
        composable(MMRADestinations.AVATAR_UPDATED_SUCCESS) {
            AvatarUpdatedScreen(
                updatedBitmap = UserProfileManager.currentAvatarBitmap,
                onBackToAccountClick = {
                    navController.popBackStack(
                        MMRADestinations.MY_ACCOUNT,
                        false
                    )
                }
            )
        }
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
        composable(MMRADestinations.EDIT_NAME) {
            EditNameScreen(
                onBackClick = {
                    navController.popBackStack()
                },

                onSaveSuccess = {
                    navController.popBackStack()
                },

                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.EDIT_EMAIL) {
            EditEmailScreen(
                onBackClick = {
                    navController.popBackStack()
                },

                onContinueVerify = {
                    navController.popBackStack()
                },
                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.EDIT_PHONE) {
            EditPhoneScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onContinueVerify = {
                    navController.popBackStack()
                },
                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onForgotPasswordClick = {
                    navController.navigate(
                        MMRADestinations.FORGOT_PASSWORD
                    )
                },
                onCancelClick = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.ACCESSIBILITY_SETTINGS) {
            AccessibilitySettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },

                onSaveSuccess = {
                    navController.popBackStack()
                },

                onResetClick = {
                    navController.navigate(
                        MMRADestinations.INITIAL_SETUP_TARGET
                    )
                }
            )
        }
        composable(MMRADestinations.VOICE_GUIDANCE_SETTINGS) {
            VoiceGuidanceSettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },

                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.MANAGE_USAGE_MODE) {
            ManageUsageModeScreen(
                initialSelectedRoles = setupPreferences.selectedRoles,
                onBackClick = {
                    navController.popBackStack()
                },
                onCancelClick = {
                    navController.popBackStack()
                },
                onConfirmSave = { updatedRoles ->
                    setupPreferences = setupPreferences.copy(
                        selectedRoles = updatedRoles
                    )
                    navController.popBackStack()
                }
            )
        }
        composable(MMRADestinations.SWITCH_ACCOUNT) {
            SwitchAccountDestination(
                onBack = {
                    navController.popBackStack()
                },
                onSignedOut = {
                    UserProfileManager.setLoggedIn(context, false)
                    navController.navigate(MMRADestinations.LOGIN) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(MMRADestinations.LOGOUT_CONFIRMATION) {
            LogoutDestination(
                onBack = {
                    navController.popBackStack()
                },
                onSignedOut = {
                    UserProfileManager.setLoggedIn(context, false)

                    navController.navigate(MMRADestinations.WELCOME) {
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
        avatarBitmap = UserProfileManager.currentAvatarBitmap,
        onBackClick = {
            navController.popBackStack()
        },
        onAvatarOptionsClick = {
            navController.navigate(
                MMRADestinations.MY_ACCOUNT_AVATAR_OPTIONS
            )
        },
        onEditNameClick = {
            navController.navigate(
                MMRADestinations.EDIT_NAME
            )
        },
        onEditEmailClick = {
            navController.navigate(
                MMRADestinations.EDIT_EMAIL
            )
        },
        onEditPhoneClick = {
            navController.navigate(
                MMRADestinations.EDIT_PHONE
            )
        },
        onChangePasswordClick = {
            navController.navigate(
                MMRADestinations.CHANGE_PASSWORD
            )
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
            navController.navigate(
                MMRADestinations.MANAGE_USAGE_MODE
            )
        },
        onSwitchAccountClick = {
            navController.navigate(
                MMRADestinations.SWITCH_ACCOUNT
            )
        },
        onLogoutClick = {
            navController.navigate(
                MMRADestinations.LOGOUT_CONFIRMATION
            )
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