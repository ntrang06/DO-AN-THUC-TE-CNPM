
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
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

object UserProfileManager {

    private const val TAG = "UserProfileManager"
    private const val PREFS_PREFIX = "dattcnpm_profile_"

    private const val DEFAULT_NAME = "Người dùng"
    private const val DEFAULT_ROLE = "Người uống thuốc"

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

    private const val KEY_SETUP_COMPLETED = "is_setup_completed"
    private const val KEY_HAS_CUSTOM_AVATAR = "has_custom_avatar"

    private var initializedUid: String? = null
    private var initialized = false

    // Trạng thái hồ sơ hiển thị trong Compose.
    var name by mutableStateOf(DEFAULT_NAME)
    var email by mutableStateOf("")
    var phone by mutableStateOf("")
    var role by mutableStateOf(DEFAULT_ROLE)

    // Cài đặt ứng dụng.
    var isLargeTextEnabled by mutableStateOf(true)
    var isVoiceGuidanceEnabled by mutableStateOf(true)
    var voiceVolume by mutableFloatStateOf(0.7f)
    var isMedicationReminderEnabled by mutableStateOf(true)
    var isFamilyAlertEnabled by mutableStateOf(true)
    var isNotificationsEnabled by mutableStateOf(true)

    // Ảnh đại diện.
    var currentAvatarBitmap by mutableStateOf<Bitmap?>(null)
    var tempCapturedBitmap by mutableStateOf<Bitmap?>(null)
    var tempSelectedUri by mutableStateOf<Uri?>(null)

    private fun currentUid(): String? {
        return FirebaseAuth.getInstance()
            .currentUser
            ?.uid
            ?.takeIf { it.isNotBlank() }
    }

    private fun prefs(
        context: Context,
        uid: String
    ): SharedPreferences {
        return context.applicationContext
            .getSharedPreferences(
                PREFS_PREFIX + uid,
                Context.MODE_PRIVATE
            )
    }

    private fun avatarFile(
        context: Context,
        uid: String
    ): File {
        val safeUid = uid.replace(
            Regex("[^A-Za-z0-9_-]"),
            "_"
        )

        return File(
            context.applicationContext.filesDir,
            "avatar_$safeUid.jpg"
        )
    }

    private fun resetState() {
        name = DEFAULT_NAME
        email = ""
        phone = ""
        role = DEFAULT_ROLE

        isLargeTextEnabled = true
        isVoiceGuidanceEnabled = true
        voiceVolume = 0.7f

        isMedicationReminderEnabled = true
        isFamilyAlertEnabled = true
        isNotificationsEnabled = true

        currentAvatarBitmap = null
        tempCapturedBitmap = null
        tempSelectedUri = null
    }

    // Tải dữ liệu theo UID đang đăng nhập.
    @Synchronized
    fun init(context: Context) {
        val uid = currentUid()

        if (initialized && initializedUid == uid) {
            return
        }

        resetState()

        initializedUid = uid
        initialized = true

        // Không có Firebase user: không tải hồ sơ cũ.
        if (uid == null) {
            return
        }

        val saved = prefs(context, uid)

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

        val file = avatarFile(context, uid)

        currentAvatarBitmap = if (
            saved.getBoolean(
                KEY_HAS_CUSTOM_AVATAR,
                false
            ) && file.isFile
        ) {
            BitmapFactory.decodeFile(
                file.absolutePath
            )
        } else {
            null
        }
    }

    // Lưu hồ sơ vào vùng riêng của UID.
    fun saveProfileInfo(context: Context) {
        init(context)

        val uid = currentUid() ?: return

        if (uid != initializedUid) return

        prefs(context, uid).edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PHONE, phone)
            .putString(KEY_ROLE, role)
            .putBoolean(
                KEY_LARGE_TEXT,
                isLargeTextEnabled
            )
            .putBoolean(
                KEY_VOICE_GUIDANCE,
                isVoiceGuidanceEnabled
            )
            .putFloat(
                KEY_VOICE_VOLUME,
                voiceVolume
            )
            .putBoolean(
                KEY_MED_REMINDERS,
                isMedicationReminderEnabled
            )
            .putBoolean(
                KEY_FAMILY_ALERTS,
                isFamilyAlertEnabled
            )
            .putBoolean(
                KEY_NOTIFICATIONS_ENABLED,
                isNotificationsEnabled
            )
            .apply()
    }

    // Firebase Auth là nguồn xác thực chính.
    fun isLoggedIn(context: Context): Boolean {
        return currentUid() != null
    }

    // Giữ hàm cũ để tương thích với code màn hình.
    // Không được dùng SharedPreferences để tự tạo phiên.
    fun setLoggedIn(
        context: Context,
        loggedIn: Boolean
    ) {
        if (loggedIn && currentUid() != null) {
            init(context)
        } else if (!loggedIn) {
            clearSession()
        }
    }

    fun isSetupCompleted(context: Context): Boolean {
        val uid = currentUid() ?: return false

        return prefs(context, uid).getBoolean(
            KEY_SETUP_COMPLETED,
            false
        )
    }

    fun setSetupCompleted(
        context: Context,
        completed: Boolean
    ) {
        val uid = currentUid() ?: return

        prefs(context, uid).edit()
            .putBoolean(
                KEY_SETUP_COMPLETED,
                completed
            )
            .apply()
    }

    // Xóa dữ liệu khỏi bộ nhớ khi đổi tài khoản.
    // Không xóa hồ sơ đã lưu của tài khoản cũ.
    @Synchronized
    fun clearSession() {
        resetState()
        initializedUid = null
        initialized = false
    }

    fun hasCustomAvatar(context: Context): Boolean {
        val uid = currentUid() ?: return false

        return prefs(context, uid).getBoolean(
            KEY_HAS_CUSTOM_AVATAR,
            false
        ) && avatarFile(context, uid).isFile
    }

    fun saveAvatarBitmap(
        context: Context,
        bitmap: Bitmap
    ): Boolean {
        val uid = currentUid() ?: return false

        init(context)

        return try {
            val file = avatarFile(context, uid)
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

            check(currentUid() == uid) {
                "Tài khoản đã thay đổi."
            }

            if (file.exists()) {
                check(file.delete()) {
                    "Không thể thay ảnh cũ."
                }
            }

            check(temporary.renameTo(file)) {
                "Không thể lưu ảnh đại diện."
            }

            prefs(context, uid).edit()
                .putBoolean(
                    KEY_HAS_CUSTOM_AVATAR,
                    true
                )
                .apply()

            currentAvatarBitmap = bitmap
            tempCapturedBitmap = null
            tempSelectedUri = null

            true
        } catch (error: Exception) {
            Log.e(
                TAG,
                "Không thể lưu ảnh đại diện.",
                error
            )
            false
        }
    }

    fun deleteAvatar(context: Context): Boolean {
        val uid = currentUid() ?: return false

        init(context)

        return try {
            val file = avatarFile(context, uid)

            check(!file.exists() || file.delete()) {
                "Không thể xóa ảnh đại diện."
            }

            prefs(context, uid).edit()
                .putBoolean(
                    KEY_HAS_CUSTOM_AVATAR,
                    false
                )
                .apply()

            currentAvatarBitmap = null
            tempCapturedBitmap = null
            tempSelectedUri = null

            true
        } catch (error: Exception) {
            Log.e(
                TAG,
                "Không thể xóa ảnh đại diện.",
                error
            )
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
            Log.e(
                TAG,
                "Không thể đọc ảnh.",
                error
            )
            null
        }
    }

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

    // Chỉ xóa dữ liệu cục bộ của UID hiện tại.
    // Không xóa tài khoản Firebase hay Firestore.
    fun resetForTesting(context: Context) {
        val uid = currentUid()

        if (uid != null) {
            prefs(context, uid)
                .edit()
                .clear()
                .commit()

            val file = avatarFile(context, uid)

            if (file.exists()) {
                file.delete()
            }
        }

        clearSession()
        init(context)
    }
}
