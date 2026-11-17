package com.example.dattcnpm.ui.screens.family

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.dattcnpm.R
import com.example.dattcnpm.ui.components.MMRABottomBar

private val PrimaryTeal = Color(0xFF0F5145)

@Composable
fun RelativeTrackingScreen(
    navController: NavHostController? = null,
    @Suppress("UNUSED_PARAMETER") memberId: String = "1",
    @Suppress("UNUSED_PARAMETER") onBackClick: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onRemindClick: () -> Unit = {},
    onCallClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onAddPersonClick: () -> Unit = {},
    onViewHistoryClick: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            if (navController != null) {
                val navBackStackEntry = navController.currentBackStackEntryAsState().value
                val currentRoute = navBackStackEntry?.destination?.route ?: "family"

                MMRABottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            } else {
                MMRABottomBar(
                    currentRoute = "family",
                    onTabSelected = {}
                )
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. TOP BAR ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Theo dõi người thân",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Cập nhật lúc 13:24",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Nút thêm người thân
                    IconButton(
                        onClick = onAddPersonClick,
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFFEFF3F5), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user_plus),
                            contentDescription = "Thêm người thân",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Avatar người dùng "NL" + chữ "Tài khoản"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFFD7EFE9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NL",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                        Text(
                            text = "Tài khoản",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. THẺ HỒ SƠ NGƯỜI THÂN ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar tròn lớn
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFD7EFE9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NL",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Nguyễn\nThị Lan",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mẹ • 68 tuổi • 3 thuốc",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Badge "ĐANG HOẠT ĐỘNG"
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFE6F4EA)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_wifi),
                                contentDescription = null,
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "ĐANG HOẠT ĐỘNG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 3. THẺ CẢNH BÁO TRỄ LIỀU (Nền hồng/đỏ nhạt) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Đầu card: Icon tam giác đỏ + Tiêu đề
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFB91C1C), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_triangle_alert),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Đã trễ liều 24 phút",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Liều 13:00 • Metformin 500 mg",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Khung con màu trắng bên trong
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "1 viên • Cùng bữa trưa",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF2F2)
                            ) {
                                Text(
                                    text = "CHƯA XÁC NHẬN",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                    }

                    // Hàng 2 nút bấm chia đều
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onCallClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_phone),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Gọi cho mẹ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onMessageClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, PrimaryTeal),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_message_circle),
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Nhắn kiểm tra",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryTeal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. THẺ "TIẾN ĐỘ HÔM NAY" ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tiến độ hôm nay",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "1 / 3 liều",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Ô 1: 08:00 Đã uống
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE6F4EA)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "08:00 Đã uống",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }

                        // Ô 2: 13:00 Trễ
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF2F2)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_triangle_alert),
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "13:00 Trễ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }

                        // Ô 3: 20:00 Sắp tới
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_clock),
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "20:00 Sắp tới",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 5. HOẠT ĐỘNG GẦN ĐÂY ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hoạt động gần đây",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                TextButton(
                    onClick = onViewHistoryClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Xem lịch sử",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActivityItem(
                    iconRes = R.drawable.ic_message_circle,
                    iconBgColor = Color(0xFFEFF6FF),
                    iconTint = Color(0xFF2563EB),
                    title = "Đã gửi lời nhắc kiểm tra",
                    subtitle = "Bạn nhắn: “Mẹ nhớ uống thuốc nhé.”",
                    time = "13:22"
                )
                ActivityItem(
                    iconRes = R.drawable.ic_check,
                    iconBgColor = Color(0xFFE6F4EA),
                    iconTint = Color(0xFF16A34A),
                    title = "Đã uống Amlodipine 5 mg",
                    subtitle = "Đúng giờ • Xác nhận bởi cô Lan",
                    time = "08:03"
                )
                ActivityItem(
                    iconRes = R.drawable.ic_check,
                    iconBgColor = Color(0xFFE6F4EA),
                    iconTint = Color(0xFF16A34A),
                    title = "Hoàn thành 3/3 liều",
                    subtitle = "Tất cả liều đúng lịch",
                    time = "Hôm qua"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ActivityItem(
    iconRes: Int,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    time: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Text(
                text = time,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC, heightDp = 892, widthDp = 412)
@Composable
fun RelativeTrackingScreenPreview() {
    RelativeTrackingScreen()
}
