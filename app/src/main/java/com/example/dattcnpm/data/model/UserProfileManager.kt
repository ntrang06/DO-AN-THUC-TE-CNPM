
package com.example.dattcnpm.data.model

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.io.FileOutputStream

object UserProfileManager {

    private const val PREFS_NAME = "dattcnpm_local_profile"

    private const val DEFAULT_NAME = "Người dùng"
    private const val DEFAULT_ROLE = "Người uống thuốc"
    private const val TAG = "UserProfileManager"

    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PHONE = "user_phone"
    private const val KEY_ROLE = "user_role"

    private const val KEY_LARGE_TEXT = "is_large_text"
    private const val KEY_VOICE_GUIDANCE = "is_voice_guidance"
    private const val KEY_VOICE_VOLUME = "voice_volume"
    private const val KEY_MED_REMINDERS = "is_med_reminders"
    private const val KEY_FAMILY_ALERTS = "is_family_alerts"
    private const val KEY_NOTIFICATIONS_ENABLED =
        "is_notifications_enabled"

    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_SETUP_COMPLETED = "is_setup_completed"
    private const val KEY_HAS_CUSTOM_AVATAR = "has_custom_avatar"

    private var initialized = false

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    private fun avatarFile(context: Context): File {
        return File(
            context.applicationContext.filesDir,
            "local_user_avatar.jpg"
        )
    }

    // -----------------------------------------
    // USER PROFILE STATE
    // -----------------------------------------

    var name by mutableStateOf(DEFAULT_NAME)
    var role by mutableStateOf(DEFAULT_ROLE)
    var email by mutableStateOf("")
    var phone by mutableStateOf("")

    // -----------------------------------------
    // APP SETTINGS
    // -----------------------------------------

    var isLargeTextEnabled by mutableStateOf(true)

    var isVoiceGuidanceEnabled by mutableStateOf(true)

    var voiceVolume by mutableFloatStateOf(0.7f)

    var isMedicationReminderEnabled by mutableStateOf(true)

    var isFamilyAlertEnabled by mutableStateOf(true)

    var isNotificationsEnabled by mutableStateOf(true)

    // -----------------------------------------
    // AVATAR STATE
    // -----------------------------------------

    var currentAvatarBitmap by mutableStateOf<Bitmap?>(null)

    var tempCapturedBitmap by mutableStateOf<Bitmap?>(null)

    var tempSelectedUri by mutableStateOf<Uri?>(null)

    // -----------------------------------------
    // INITIALIZATION
    // -----------------------------------------

    fun init(context: Context) {
        if (initialized) return

        val saved = prefs(context)

        name = saved.getString(
            KEY_NAME,
            DEFAULT_NAME
        ).orEmpty().ifBlank { DEFAULT_NAME }

        email = saved.getString(
            KEY_EMAIL,
            ""
        ).orEmpty()

        phone = saved.getString(
            KEY_PHONE,
            ""
        ).orEmpty()

        role = saved.getString(
            KEY_ROLE,
            DEFAULT_ROLE
        ).orEmpty().ifBlank { DEFAULT_ROLE }

        isLargeTextEnabled = saved.getBoolean(
            KEY_LARGE_TEXT,
            true
        )

        isVoiceGuidanceEnabled = saved.getBoolean(
            KEY_VOICE_GUIDANCE,
            true
        )

        voiceVolume = saved.getFloat(
            KEY_VOICE_VOLUME,
            0.7f
        ).coerceIn(0f, 1f)

        isMedicationReminderEnabled = saved.getBoolean(
            KEY_MED_REMINDERS,
            true
        )

        isFamilyAlertEnabled = saved.getBoolean(
            KEY_FAMILY_ALERTS,
            true
        )

        isNotificationsEnabled = saved.getBoolean(
            KEY_NOTIFICATIONS_ENABLED,
            true
        )

        val file = avatarFile(context)

        if (
            saved.getBoolean(KEY_HAS_CUSTOM_AVATAR, false) &&
            file.isFile
        ) {
            currentAvatarBitmap = BitmapFactory.decodeFile(
                file.absolutePath
            )
        } else {
            currentAvatarBitmap = null
        }

        initialized = true
    }

    // -----------------------------------------
    // SAVE PROFILE LOCALLY
    // -----------------------------------------

    fun saveProfileInfo(context: Context) {
        prefs(context).edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PHONE, phone)
            .putString(KEY_ROLE, role)
            .putBoolean(KEY_LARGE_TEXT, isLargeTextEnabled)
            .putBoolean(KEY_VOICE_GUIDANCE, isVoiceGuidanceEnabled)
            .putFloat(KEY_VOICE_VOLUME, voiceVolume)
            .putBoolean(KEY_MED_REMINDERS, isMedicationReminderEnabled)
            .putBoolean(KEY_FAMILY_ALERTS, isFamilyAlertEnabled)
            .putBoolean(
                KEY_NOTIFICATIONS_ENABLED,
                isNotificationsEnabled
            )
            .apply()
    }

    // -----------------------------------------
    // LOGIN STATE - LOCAL MOCK
    // -----------------------------------------

    fun isLoggedIn(context: Context): Boolean {
        return prefs(context).getBoolean(
            KEY_IS_LOGGED_IN,
            false
        )
    }

    fun setLoggedIn(
        context: Context,
        loggedIn: Boolean
    ) {
        prefs(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            .apply()
    }

    // -----------------------------------------
    // INITIAL SETUP STATE
    // -----------------------------------------

    fun isSetupCompleted(context: Context): Boolean {
        return prefs(context).getBoolean(
            KEY_SETUP_COMPLETED,
            false
        )
    }

    fun setSetupCompleted(
        context: Context,
        completed: Boolean
    ) {
        prefs(context).edit()
            .putBoolean(KEY_SETUP_COMPLETED, completed)
            .apply()
    }

    // -----------------------------------------
    // AVATAR MANAGEMENT
    // -----------------------------------------

    fun hasCustomAvatar(context: Context): Boolean {
        return prefs(context).getBoolean(
            KEY_HAS_CUSTOM_AVATAR,
            false
        ) && avatarFile(context).isFile
    }

    fun saveAvatarBitmap(
        context: Context,
        bitmap: Bitmap
    ): Boolean {
        return try {
            val file = avatarFile(context)

            val temporary = File(
                file.parentFile,
                file.name + ".tmp"
            )

            FileOutputStream(temporary).use { output ->
                check(
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        90,
                        output
                    )
                ) {
                    "Không thể mã hóa ảnh đại diện."
                }

                output.flush()
            }

            if (file.exists()) {
                check(file.delete()) {
                    "Không thể thay thế ảnh đại diện cũ."
                }
            }

            check(temporary.renameTo(file)) {
                "Không thể lưu ảnh đại diện."
            }

            prefs(context).edit()
                .putBoolean(KEY_HAS_CUSTOM_AVATAR, true)
                .apply()

            currentAvatarBitmap = bitmap
            tempCapturedBitmap = null
            tempSelectedUri = null

            true
        } catch (error: Exception) {
            Log.e(TAG, "Không thể lưu ảnh đại diện.", error)
            false
        }
    }

    fun deleteAvatar(context: Context): Boolean {
        return try {
            val file = avatarFile(context)

            check(!file.exists() || file.delete()) {
                "Không thể xóa ảnh đại diện."
            }

            prefs(context).edit()
                .putBoolean(KEY_HAS_CUSTOM_AVATAR, false)
                .apply()

            currentAvatarBitmap = null
            tempCapturedBitmap = null
            tempSelectedUri = null

            true
        } catch (error: Exception) {
            Log.e(TAG, "Không thể xóa ảnh đại diện.", error)
            false
        }
    }

    fun loadBitmapFromUri(
        context: Context,
        uri: Uri
    ): Bitmap? {
        return try {
            context.contentResolver
                .openInputStream(uri)
                ?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
        } catch (error: Exception) {
            Log.e(TAG, "Không thể đọc ảnh.", error)
            null
        }
    }

    // -----------------------------------------
    // AVATAR INITIALS
    // -----------------------------------------

    fun getInitials(): String {
        val parts = name
            .trim()
            .ifBlank { DEFAULT_NAME }
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        return when {
            parts.size >= 2 ->
                "${parts.first().first()}${parts.last().first()}"
                    .uppercase()

            parts.size == 1 ->
                parts[0].take(2).uppercase()

            else -> "ND"
        }
    }

    // -----------------------------------------
    // RESET LOCAL STATE FOR TESTING
    // -----------------------------------------

    fun resetForTesting(context: Context) {
        prefs(context).edit().clear().apply()

        val file = avatarFile(context)
        if (file.exists()) {
            file.delete()
        }

        name = DEFAULT_NAME
        role = DEFAULT_ROLE
        email = ""
        phone = ""

        isLargeTextEnabled = true
        isVoiceGuidanceEnabled = true
        voiceVolume = 0.7f
        isMedicationReminderEnabled = true
        isFamilyAlertEnabled = true
        isNotificationsEnabled = true

        currentAvatarBitmap = null
        tempCapturedBitmap = null
        tempSelectedUri = null

        initialized = true
    }
}
