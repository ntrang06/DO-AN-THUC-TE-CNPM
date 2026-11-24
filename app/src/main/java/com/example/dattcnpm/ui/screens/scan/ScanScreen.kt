package com.example.dattcnpm.ui.screens.scan

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.dattcnpm.data.scan.ScanCropRegion
import kotlinx.coroutines.delay

data class ScanResult(
    val medicineName: String = "",
    val dosage: String = "",
    val dosageForm: String = ""
)

private enum class ScanFlowState {
    CAMERA,
    PREVIEW,
    PROCESSING,
    RESULT
}

private val ScanCropRegionSaver = listSaver<ScanCropRegion, Float>(
    save = {
        listOf(it.left, it.top, it.right, it.bottom)
    },
    restore = {
        ScanCropRegion(
            left = it[0],
            top = it[1],
            right = it[2],
            bottom = it[3]
        ).normalized()
    }
)

@Composable
fun ScanScreen(
    onScanSuccess: (ScanResult) -> Unit = {},
    onManualInputClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {}
) {
    var state by rememberSaveable {
        mutableStateOf(ScanFlowState.CAMERA)
    }

    var imageUri by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var fromGallery by rememberSaveable {
        mutableStateOf(false)
    }

    var cropRegion by rememberSaveable(
        stateSaver = ScanCropRegionSaver
    ) {
        mutableStateOf(ScanCropRegion.Full)
    }

    var showGalleryInfo by rememberSaveable {
        mutableStateOf(false)
    }

    var galleryInfoAccepted by rememberSaveable {
        mutableStateOf(false)
    }

    // Thay đổi mỗi lần nhận ảnh để tạo lại form kết quả.
    var photoVersion by rememberSaveable {
        mutableStateOf(0)
    }

    fun selectImage(uri: Uri, gallery: Boolean) {
        imageUri = uri.toString()
        fromGallery = gallery
        cropRegion = ScanCropRegion.Full
        photoVersion += 1
        state = ScanFlowState.PREVIEW
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectImage(uri, gallery = true)
        }
    }

    fun launchPhotoPicker() {
        photoPicker.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    fun openGallery() {
        if (galleryInfoAccepted) {
            launchPhotoPicker()
        } else {
            showGalleryInfo = true
        }
    }

    fun goBack() {
        when (state) {
            ScanFlowState.CAMERA -> onClose()

            ScanFlowState.PREVIEW -> {
                state = ScanFlowState.CAMERA
            }

            ScanFlowState.PROCESSING,
            ScanFlowState.RESULT -> {
                state = ScanFlowState.PREVIEW
            }
        }
    }

    BackHandler {
        goBack()
    }

    LaunchedEffect(state, imageUri, photoVersion) {
        if (state == ScanFlowState.PROCESSING) {
            if (imageUri == null) {
                state = ScanFlowState.CAMERA
            } else {
                // Chỉ mô phỏng trạng thái chờ để kiểm thử FE.
                // Chưa thực hiện OCR hoặc gọi API.
                delay(1500)
                state = ScanFlowState.RESULT
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            ScanFlowState.CAMERA -> {
                ScanCameraScreen(
                    onCaptureClick = { uri ->
                        selectImage(uri, gallery = false)
                    },
                    onPickImageClick = {
                        openGallery()
                    },
                    onManualInputClick = onManualInputClick,
                    onCloseClick = onClose
                )
            }

            ScanFlowState.PREVIEW -> {
                ScanPreviewScreen(
                    imageUri = imageUri?.let(Uri::parse),
                    isFromGallery = fromGallery,
                    cropRegion = cropRegion,
                    onCropRegionChange = {
                        cropRegion = it
                    },
                    onRetakeClick = {
                        if (fromGallery) {
                            openGallery()
                        } else {
                            state = ScanFlowState.CAMERA
                        }
                    },
                    onUsePhotoClick = {
                        state = ScanFlowState.PROCESSING
                    },
                    onBackClick = {
                        goBack()
                    }
                )
            }

            ScanFlowState.PROCESSING -> {
                ScanProcessingScreen(
                    imageUri = imageUri,
                    cropRegion = cropRegion,
                    onBackClick = {
                        goBack()
                    }
                )
            }

            ScanFlowState.RESULT -> {
                key(photoVersion) {
                    ScanResultScreen(
                        scanResult = ScanResult(
                            medicineName = "Thuốc minh họa",
                            dosage = "",
                            dosageForm = ""
                        ),
                        onRescanClick = {
                            state = ScanFlowState.CAMERA
                        },
                        onConfirmClick = onScanSuccess,
                        onBackClick = {
                            goBack()
                        }
                    )
                }
            }
        }
    }

    if (showGalleryInfo) {
        AlertDialog(
            onDismissRequest = {
                showGalleryInfo = false
            },
            title = {
                Text("Chọn ảnh thuốc")
            },
            text = {
                Text(
                    "Chọn ảnh hộp hoặc vỉ thuốc để xem lại và khoanh vùng. " +
                            "Bản FE hiện tại chỉ hiển thị kết quả mẫu, " +
                            "chưa nhận diện thông tin từ ảnh."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showGalleryInfo = false
                        galleryInfoAccepted = true
                        launchPhotoPicker()
                    }
                ) {
                    Text("Chọn ảnh")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showGalleryInfo = false
                    }
                ) {
                    Text("Để sau")
                }
            }
        )
    }
}