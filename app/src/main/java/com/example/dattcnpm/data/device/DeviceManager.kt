
package com.example.dattcnpm.data.device

import android.content.Context
import android.util.Log
import com.example.dattcnpm.data.model.UserProfileManager
import com.example.dattcnpm.data.remote.FirebaseProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.UUID

object DeviceManager {

    private const val TAG = "MMRA_FCM"
    private const val PREFS_NAME = "mmra_device_info"
    private const val KEY_DEVICE_ID = "device_id"

    // Không cho ghi token và xóa thiết bị chạy chồng nhau.
    private val operationMutex = Mutex()

    @Synchronized
    private fun getDeviceId(context: Context): String {
        val prefs = context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

        val existing = prefs.getString(KEY_DEVICE_ID, null)

        if (!existing.isNullOrBlank()) {
            return existing
        }

        val deviceId = UUID.randomUUID().toString()

        check(
            prefs.edit()
                .putString(KEY_DEVICE_ID, deviceId)
                .commit()
        ) {
            "Không thể lưu mã thiết bị."
        }

        return deviceId
    }

    suspend fun syncCurrentDevice(
        context: Context,
        expectedUid: String,
        updatedToken: String? = null
    ): Boolean = operationMutex.withLock {

        val auth = FirebaseProvider.auth

        if (
            expectedUid.isBlank() ||
            auth.currentUser?.uid != expectedUid
        ) {
            return@withLock false
        }

        try {
            val token = updatedToken
                ?.takeIf { it.isNotBlank() }
                ?: FirebaseProvider.messaging.token.await()

            if (token.isBlank()) {
                Log.w(TAG, "FCM token rỗng.")
                return@withLock false
            }

            if (auth.currentUser?.uid != expectedUid) {
                return@withLock false
            }

            val deviceId = getDeviceId(context)

            val document = FirebaseProvider
                .devicesCollection(expectedUid)
                .document(deviceId)

            val data = mapOf(
                "deviceId" to deviceId,
                "fcmToken" to token,
                "platform" to "android",
                "updatedAt" to FieldValue.serverTimestamp()
            )

            document.set(
                data,
                SetOptions.merge()
            ).await()

            val stillCurrent =
                auth.currentUser?.uid == expectedUid

            if (stillCurrent) {
                Log.d(
                    TAG,
                    "Đồng bộ thiết bị lên Firestore thành công."
                )
            }

            stillCurrent

        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Log.e(
                TAG,
                "Không đồng bộ được thiết bị.",
                error
            )
            false
        }
    }

    // Dùng cho cả Đăng xuất và Chuyển tài khoản.
    // Mutex giữ cho các thao tác sync/remove tuần tự.
    suspend fun removeDeviceAndSignOut(
        context: Context
    ) = operationMutex.withLock {

        val auth = FirebaseProvider.auth
        val uid = auth.currentUser?.uid

        if (!uid.isNullOrBlank()) {
            val deviceId = getDeviceId(context)

            FirebaseProvider
                .devicesCollection(uid)
                .document(deviceId)
                .delete()
                .await()

            check(auth.currentUser?.uid == uid) {
                "Phiên đăng nhập đã thay đổi."
            }

            Log.d(
                TAG,
                "Đã gỡ thiết bị khỏi Firestore."
            )
        }

        auth.signOut()
        UserProfileManager.clearSession()

        Log.d(
            TAG,
            "Đã đăng xuất và xóa phiên hồ sơ cục bộ."
        )
    }
}
