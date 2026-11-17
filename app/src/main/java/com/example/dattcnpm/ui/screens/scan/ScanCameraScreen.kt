package com.example.dattcnpm.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.example.dattcnpm.R

private val DarkTealBg = Color(0xFF132A24)
private val PrimaryTeal = Color(0xFF0F5145)
private val LightMint = Color(0xFFD7EFE9)

@Composable
fun ScanCameraScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavHostController? = null,
    onCloseClick: () -> Unit = {},
    onFlashClick: () -> Unit = {},
    onCaptureClick: () -> Unit = {},
    onManualInputClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isPreviewMode = LocalInspectionMode.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Logic kiểm tra và yêu cầu quyền Camera
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && !isPreviewMode) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Photo Picker launcher chọn ảnh từ thư viện
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onGalleryClick()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkTealBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- 1. TOP BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkTealBg)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Đóng",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Quét vỏ hộp thuốc",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Bước 1 / 3 • Nhận diện thuốc",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            IconButton(
                onClick = onFlashClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_zap),
                    contentDescription = "Bật đèn flash",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // --- 2. VÙNG CAMERA + KHUNG NGẮM ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            if (!isPreviewMode && !hasCameraPermission) {
                // Giao diện thông báo khi chưa được cấp quyền Camera
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Cần cấp quyền camera để quét vỏ hộp thuốc",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cấp quyền camera",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (!isPreviewMode) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (_: Exception) {}
                        }, ContextCompat.getMainExecutor(context))
                    }
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.sample_medicine_box),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Lớp phủ tối mờ toàn màn hình camera
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )

            // Layout nội dung đè lên Camera
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // 1. Hộp hướng dẫn bo góc ở đỉnh
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF19352E).copy(alpha = 0.92f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Badge "GIỮ MÁY ỔN ĐỊNH"
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = LightMint
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_scan),
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "GIỮ MÁY ỔN ĐỊNH",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )
                            }
                        }

                        Text(
                            text = "Đặt toàn bộ mặt trước vỏ hộp vào khung",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 2. Khung ngắm chính viền trắng
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(310.dp)
                        .border(2.5.dp, Color.White, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Đường kẻ ngang trung tâm
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(1.5.dp)
                            .background(Color.White.copy(alpha = 0.85f))
                    )

                    // 4 gạch ngang bo tròn ở 4 góc bên trong khung ngắm
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .width(36.dp)
                                .height(6.dp)
                                .background(LightMint, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .width(36.dp)
                                .height(6.dp)
                                .background(LightMint, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .width(36.dp)
                                .height(6.dp)
                                .background(LightMint, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .width(36.dp)
                                .height(6.dp)
                                .background(LightMint, CircleShape)
                        )
                    }
                }

                // 3. Badge "Đủ sáng • Không bị lóa"
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF19352E)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sun),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Đủ sáng • Không bị lóa",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // --- 3. BẢNG ĐIỀU KHIỂN ĐÁY (WHITE SHEET) ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color(0xFFF8FAF9)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nút chụp ảnh dạng vòng tròn đôi
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .border(3.5.dp, PrimaryTeal, CircleShape)
                        .clickable { onCaptureClick() }
                        .padding(5.dp)
                        .background(PrimaryTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Chụp ảnh",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Nút 1: Nhập thông tin thủ công
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onManualInputClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_keyboard),
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Nhập thông tin thủ công",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }

                // Nút 2: Hoặc chọn ảnh từ thư viện
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_image),
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Hoặc chọn ảnh từ thư viện",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }

        // --- 4. DẢI NỀN XANH ĐẬM DƯỚI CÙNG ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(DarkTealBg)
        )
    }
}

@ComposePreview(showBackground = true, heightDp = 892, widthDp = 412)
@Composable
fun ScanCameraScreenPreview() {
    ScanCameraScreen()
}
