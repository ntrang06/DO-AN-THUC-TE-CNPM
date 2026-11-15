package com.example.dattcnpm.ui.screens.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dattcnpm.ui.theme.MMRATheme

// Màu sắc chuẩn theo bộ nhận diện Figma của dự án
private val FigmaPrimaryTeal = Color(0xFF0F5145)
private val FigmaLightTealBg = Color(0xFFE8F2F0)
private val FigmaPageBackground = Color(0xFFF8FAFA)
private val FigmaBorder = Color(0xFFE2E8F0)
private val FigmaWarningBg = Color(0xFFFEF3C7)
private val FigmaWarningText = Color(0xFFB45309)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onScanCaptured: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(1) } // 0: Thuốc của tôi, 1: Thuốc người thân

    Scaffold(
        containerColor = FigmaPageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Quét thuốc",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Chọn thuốc của ai",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, FigmaBorder),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            IconButton(onClick = {}) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "QR Code",
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FigmaLightTealBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MA",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaPrimaryTeal
                            )
                        }
                        Text(
                            text = "Tài khoản",
                            fontSize = 10.sp,
                            color = FigmaPrimaryTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FigmaPageBackground)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FigmaPageBackground)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Banner minh họa
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEEF2F6),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Dữ liệu minh họa • Không phải người dùng thật",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            Text(
                text = "Bạn muốn quét thuốc cho ai?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            // Tab chọn Thuốc của tôi / Thuốc người thân
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E8F0).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                ScanTabButton(
                    text = "Thuốc của tôi",
                    selected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                ScanTabButton(
                    text = "Thuốc người thân",
                    selected = selectedTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
            }

            // Chọn người thân đã liên kết (Dropdown card)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, FigmaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FigmaLightTealBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "TM",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FigmaPrimaryTeal
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Trần Văn Minh • Bố",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "2 người thân đã liên kết",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }
            }

            // Trạng thái đang quét cho ai
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, FigmaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Đang quét thuốc cho",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Trần Văn Minh • Bố",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FigmaLightTealBg
                        ) {
                            Text(
                                text = "Đã liên kết",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FigmaPrimaryTeal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FigmaLightTealBg
                        ) {
                            Text(
                                text = "Được xem lịch uống",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FigmaPrimaryTeal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Thẻ lưu ý bảo mật 1 (Xanh nhạt)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEFF6F5),
                border = BorderStroke(1.dp, Color(0xFFD1E5E2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = FigmaPrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Quét thuốc không tự động lưu kết quả.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaPrimaryTeal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Bạn có thể chụp hoặc chọn ảnh để xem lại, không cần lưu vào hồ sơ thuốc.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // Thẻ cảnh báo quyền (Vàng nhạt)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = FigmaWarningBg.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = FigmaWarningText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Chưa thể lưu vào hồ sơ thuốc của Bố",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaWarningText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lưu vào hồ sơ thuốc của Trần Văn Minh cần chọn đúng người và được cấp riêng quyền quản lý thuốc. Hiện chưa có quyền này; quét thuốc không cấp quyền.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Nút chụp ảnh chính (Nền xanh teal)
            Button(
                onClick = onScanCaptured,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FigmaPrimaryTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tiếp tục chụp vỏ hộp thuốc",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Nút chọn ảnh từ thư viện (Nền trắng viền xám)
            OutlinedButton(
                onClick = onScanCaptured,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, FigmaBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chọn ảnh từ thư viện",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ScanTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) FigmaPrimaryTeal else Color.Transparent,
        modifier = modifier.height(38.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (selected) Color.White else Color(0xFF64748B),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun ScanScreenPreview() {
    MMRATheme {
        ScanScreen(onScanCaptured = {})
    }
}