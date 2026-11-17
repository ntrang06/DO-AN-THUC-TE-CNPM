package com.example.dattcnpm.ui.screens.scan

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
import com.example.dattcnpm.R

@Composable
fun ScanResultScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onRescanClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- TOP BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFEFF3F5), CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Quay lại",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Kiểm tra thuốc",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Bước 2 / 3 • Xác nhận kết quả",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- THẺ GỢI Ý NHẬN DIỆN ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sparkles),
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(22.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Gợi ý nhận diện — chưa phải tư vấn y tế",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Hãy đối chiếu với vỏ hộp, đơn thuốc hoặc dược sĩ trước khi xác nhận.",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- THẺ THÔNG TIN KẾT QUẢ CHÍNH ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hình minh họa thuốc M500
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.5.dp, Color(0xFF2563EB))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_pill),
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "M500",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Badge Khớp cao
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_scan),
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "KHỚP CAO • 92%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }

                        Text(
                            text = "Metformin 500 mg",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Text(
                            text = "Viên nén bao phim • Màu trắng",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Banner xanh lá đối chiếu cơ sở dữ liệu
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE6F4EA)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF0F5145), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_shield_check),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "Đã đối chiếu cơ sở dữ liệu tin cậy",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F5145),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Cục Quản lý Dược • Cập nhật 09/2026",
                                color = Color(0xFF15803D),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- THÔNG TIN ĐỌC TỪ VỎ HỘP ---
        Text(
            text = "Thông tin đọc từ vỏ hộp",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ResultDetailItem(
                    iconRes = R.drawable.ic_pill,
                    label = "Hoạt chất",
                    value = "Metformin hydrochloride"
                )
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ResultDetailItem(
                    iconRes = R.drawable.ic_factory,
                    label = "Nhà sản xuất",
                    value = "Mekophar"
                )
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                ResultDetailItem(
                    iconRes = R.drawable.ic_package,
                    label = "Quy cách",
                    value = "Hộp 3 vỉ × 10 viên"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- BANNER CÂU HỎI ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFEFF6FF)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_circle_help),
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Tên và hàm lượng có đúng với thuốc của bạn?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E40AF)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- NÚT HÀNH ĐỘNG CỦA ĐÁY MÀN HÌNH ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRescanClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFF0F5145)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F5145))
            ) {
                Text(
                    text = "Quét lại",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0F5145)
                )
            }

            Button(
                onClick = onConfirmClick,
                modifier = Modifier
                    .weight(1.2f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5145))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Đúng, tiếp tục",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ResultDetailItem(
    iconRes: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFE6F4EA), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color(0xFF0F5145),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC, heightDp = 892, widthDp = 412)
@Composable
fun ScanResultScreenPreview() {
    ScanResultScreen()
}
