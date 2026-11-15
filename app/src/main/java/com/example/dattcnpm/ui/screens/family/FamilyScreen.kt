package com.example.dattcnpm.ui.screens.family

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dattcnpm.data.mock.FamilyMember
import com.example.dattcnpm.data.mock.MockFamilyData
import com.example.dattcnpm.ui.theme.MMRATheme

// Bảng màu chuẩn theo Figma của nhóm
private val FigmaPrimaryTeal = Color(0xFF0F5145)
private val FigmaLightTealBg = Color(0xFFE8F2F0)
private val FigmaPageBackground = Color(0xFFF8FAFA)
private val FigmaBorder = Color(0xFFE2E8F0)
private val FigmaWarningBg = Color(0xFFFEF3C7)
private val FigmaWarningText = Color(0xFFB45309)
private val FigmaErrorBg = Color(0xFFFEE2E2)
private val FigmaErrorText = Color(0xFFB91C1C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    onConnectClick: () -> Unit,
    onMemberClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val linkedMembers = MockFamilyData.mockMembers.take(2)

    Scaffold(
        containerColor = FigmaPageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gia đình",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Cùng chăm sóc người thân",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, FigmaBorder),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Thông báo",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(20.dp)
                            )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Banner & Nút mời người thân
            item {
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
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConnectClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FigmaPrimaryTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "+  Mời người thân",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // Tiêu đề Người thân của bạn
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Người thân của bạn",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FigmaLightTealBg
                    ) {
                        Text(
                            text = "${linkedMembers.size} đã liên kết",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FigmaPrimaryTeal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Danh sách thành viên
            items(linkedMembers.size) { index ->
                FamilyMemberFigmaCard(
                    member = linkedMembers[index],
                    onClick = { onMemberClick(linkedMembers[index].id) }
                )
            }

            // Thẻ liên kết khác với quyền dữ liệu
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEFF6F5),
                    border = BorderStroke(1.dp, Color(0xFFD1E5E2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = FigmaPrimaryTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Liên kết khác với quyền dữ liệu",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaPrimaryTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Liên kết hoặc chấp nhận lời mời không tự động cấp quyền dữ liệu sức khỏe. Quyền được cấp riêng; chỉ xem tiến độ và cảnh báo trong quyền đã cấp.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // Mục Lời mời người thân (Đã nhận & Đã gửi)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lời mời người thân",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "LỜI MỜI ĐÃ NHẬN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaPrimaryTeal
                )
                Spacer(modifier = Modifier.height(6.dp))
                InviteFigmaCard(
                    title = "Từ Nguyễn Văn A",
                    subtitle = "Nhận hôm nay • Minh họa",
                    badgeText = "Đang chờ phản hồi",
                    badgeBg = Color(0xFFF1F5F9),
                    badgeTextColor = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LỜI MỜI ĐÃ GỬI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FigmaPrimaryTeal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InviteFigmaCard(
                        title = "Gửi cho Nguyễn Thị B",
                        subtitle = "Lời mời mẫu • Minh họa",
                        badgeText = "Đang chờ chấp nhận",
                        badgeBg = FigmaWarningBg,
                        badgeTextColor = FigmaWarningText
                    )
                    InviteFigmaCard(
                        title = "Gửi cho Nguyễn Văn C",
                        subtitle = "Lời mời mẫu • Minh họa",
                        badgeText = "Đã từ chối",
                        badgeBg = FigmaErrorBg,
                        badgeTextColor = FigmaErrorText
                    )
                    InviteFigmaCard(
                        title = "Gửi cho Nguyễn Thị D",
                        subtitle = "Lời mời mẫu • Minh họa",
                        badgeText = "Đã hết hạn",
                        badgeBg = Color(0xFFF1F5F9),
                        badgeTextColor = Color(0xFF64748B)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FamilyMemberFigmaCard(
    member: FamilyMember,
    onClick: () -> Unit
) {
    val initials = member.fullName.split(" ")
        .takeLast(2)
        .mapNotNull { it.firstOrNull()?.toString() }
        .joinToString("")

    val progress = if (member.todayTotalCount > 0) {
        member.todayTakenCount.toFloat() / member.todayTotalCount.toFloat()
    } else 0f

    val pendingCount = (member.todayTotalCount - member.todayTakenCount - if (member.hasLateDose) 1 else 0).coerceAtLeast(0)
    val lateCount = if (member.hasLateDose) 1 else 0

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, FigmaBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FigmaLightTealBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FigmaPrimaryTeal
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = member.fullName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${member.relationship} • Hồ sơ minh họa",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
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
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FigmaLightTealBg
                ) {
                    Text(
                        text = "Được xem lịch uống",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FigmaPrimaryTeal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chỉ xem",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã xác nhận ${member.todayTakenCount}/${member.todayTotalCount} liều hôm nay",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (member.hasLateDose) FigmaErrorBg else FigmaWarningBg
                ) {
                    Text(
                        text = if (member.hasLateDose) "Cần chú ý" else "Chờ xác nhận",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (member.hasLateDose) FigmaErrorText else FigmaWarningText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = FigmaPrimaryTeal,
                trackColor = Color(0xFFE2E8F0),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${member.todayTakenCount} đã xác nhận • $pendingCount chờ xác nhận\n$lateCount quá giờ chưa xác nhận",
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = Color(0xFF64748B)
            )

            if (member.hasLateDose) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FigmaErrorBg.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = FigmaErrorText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Liều 08:00 quá giờ chưa xác nhận",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = FigmaErrorText
                        )
                    }
                }
            }

            HorizontalDivider(
                color = FigmaBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = FigmaPrimaryTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Quyền chia sẻ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaPrimaryTeal
                        )
                        Text(
                            text = "Chưa được cấp quyền quản lý thuốc",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun InviteFigmaCard(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeBg: Color,
    badgeTextColor: Color
) {
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
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeTextColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Chưa liên kết • Chưa chia sẻ dữ liệu sức khỏe",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1800)
@Composable
fun FamilyScreenPreview() {
    MMRATheme {
        FamilyScreen(
            onConnectClick = {},
            onMemberClick = {}
        )
    }
}