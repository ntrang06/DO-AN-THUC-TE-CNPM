package com.example.dattcnpm.ui.screens.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScanResultScreen(
    scanResult: ScanResult,
    onRescanClick: () -> Unit,
    onConfirmClick: (ScanResult) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var medicineName by rememberSaveable(scanResult.medicineName) {
        mutableStateOf(scanResult.medicineName)
    }

    var dosage by rememberSaveable(scanResult.dosage) {
        mutableStateOf(scanResult.dosage)
    }

    var dosageForm by rememberSaveable(scanResult.dosageForm) {
        mutableStateOf(scanResult.dosageForm)
    }

    var editingField by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var editingValue by rememberSaveable {
        mutableStateOf("")
    }

    fun edit(label: String, value: String) {
        editingField = label
        editingValue = value
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6FAF9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBackClick,
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color(0xFFE8F2F0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kiểm tra thuốc",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Kiểm tra thông tin trước khi tiếp tục",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF7ED),
                border = BorderStroke(1.dp, Color(0xFFFED7AA))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Dữ liệu minh họa để kiểm thử giao diện",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9A3412)
                    )

                    Text(
                        text = "Thông tin dưới đây không được nhận diện " +
                                "từ ảnh bạn vừa chọn. Bạn có thể sửa các " +
                                "trường để kiểm tra luồng nhập thuốc.",
                        color = Color(0xFFC2410C),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2ECEA))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF0F7F5),
                        border = BorderStroke(1.dp, Color(0xFFB1D4CC))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = Color(0xFF126B62),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDDF2EE)
                        ) {
                            Text(
                                text = "BẢN THỬ GIAO DIỆN",
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                ),
                                color = Color(0xFF126B62),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = medicineName.ifBlank { "Chưa nhập tên thuốc" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = listOf(dosage, dosageForm)
                                .filter { it.isNotBlank() }
                                .joinToString(" • "),
                            color = Color(0xFF6B7280),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Text(
                text = "Thông tin thuốc",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column {
                    ScanEditableInfoRow(
                        label = "Tên thuốc",
                        value = medicineName,
                        onClick = { edit("Tên thuốc", medicineName) }
                    )

                    HorizontalDivider(color = Color(0xFFF3F4F6))

                    ScanEditableInfoRow(
                        label = "Hàm lượng",
                        value = dosage,
                        onClick = { edit("Hàm lượng", dosage) }
                    )

                    HorizontalDivider(color = Color(0xFFF3F4F6))

                    ScanEditableInfoRow(
                        label = "Dạng thuốc",
                        value = dosageForm,
                        onClick = { edit("Dạng thuốc", dosageForm) }
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF)
            ) {
                Text(
                    text = "Nhấn vào từng dòng để chỉnh thông tin. " +
                            "Nút Tiếp tục chỉ chuyển sang form nhập, chưa lưu thuốc.",
                    modifier = Modifier.padding(14.dp),
                    color = Color(0xFF1D4ED8),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(10.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRescanClick,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF126B62))
                ) {
                    Text("Quét lại")
                }

                Button(
                    onClick = {
                        onConfirmClick(
                            scanResult.copy(
                                medicineName = medicineName.trim(),
                                dosage = dosage.trim(),
                                dosageForm = dosageForm.trim()
                            )
                        )
                    },
                    enabled = medicineName.isNotBlank(),
                    modifier = Modifier
                        .weight(1.3f)
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF126B62)
                    )
                ) {
                    Text("Tiếp tục")
                }
            }
        }
    }

    val field = editingField

    if (field != null) {
        AlertDialog(
            onDismissRequest = { editingField = null },
            title = { Text("Chỉnh sửa $field") },
            text = {
                OutlinedTextField(
                    value = editingValue,
                    onValueChange = { editingValue = it },
                    label = { Text(field) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    enabled = field != "Tên thuốc" ||
                            editingValue.isNotBlank(),
                    onClick = {
                        when (field) {
                            "Tên thuốc" -> medicineName = editingValue.trim()
                            "Hàm lượng" -> dosage = editingValue.trim()
                            "Dạng thuốc" -> dosageForm = editingValue.trim()
                        }

                        editingField = null
                    }
                ) {
                    Text("Áp dụng")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun ScanEditableInfoRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )

            Text(
                text = value.ifBlank { "Chưa nhập" },
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937)
            )
        }

        Spacer(Modifier.width(12.dp))

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Sửa $label",
            tint = Color(0xFF126B62),
            modifier = Modifier.size(20.dp)
        )
    }
}