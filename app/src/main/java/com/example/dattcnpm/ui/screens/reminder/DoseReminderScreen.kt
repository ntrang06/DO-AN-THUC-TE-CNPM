package com.example.dattcnpm.ui.screens.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dattcnpm.data.mock.MockCalendar
import com.example.dattcnpm.data.mock.MockDoseAction
import com.example.dattcnpm.data.mock.MockDoseActions
import com.example.dattcnpm.data.mock.MockReminderDoses

@Composable
fun DoseReminderScreen(
    doseId: String?,
    readOnly: Boolean,
    onBack: () -> Unit
) {
    val dose = MockReminderDoses.findById(doseId)

    if (dose == null) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Không tìm thấy liều thuốc.")
            TextButton(onClick = onBack) { Text("Quay lại") }
        }
        return
    }

    val action = MockDoseActions.get(dose.doseId)?.action
    val finished = MockDoseActions.isFinished(dose.doseId)
    val isToday = MockCalendar.day(dose.scheduledAtMillis) == MockCalendar.today()
    val canAct = isToday && !readOnly && !finished

    // Chỉ lưu lựa chọn đang chờ. Bấm “Xác nhận” mới ghi vào lịch.
    var pending by rememberSaveable(dose.doseId) {
        mutableStateOf<MockDoseAction?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("Quay lại") }

        Text("Chi tiết liều thuốc", style = MaterialTheme.typography.headlineSmall)
        Text(dose.medicineName, style = MaterialTheme.typography.titleLarge)
        Text("Giờ uống: ${dose.timeLabel}")
        Text("Liều dùng: ${dose.quantityText}")

        if (dose.mealInstruction.isNotBlank()) {
            Text(dose.mealInstruction)
        }

        val status = when (action) {
            MockDoseAction.TAKEN -> "Đã uống"
            MockDoseAction.SKIPPED -> "Đã bỏ qua"
            MockDoseAction.SNOOZED -> "Nhắc lại"
            null -> "Chưa ghi nhận"
        }
        Text("Trạng thái: $status", color = MaterialTheme.colorScheme.primary)

        if (canAct) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { pending = MockDoseAction.TAKEN }
            ) { Text("Đã uống") }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { pending = MockDoseAction.SKIPPED }
            ) { Text("Bỏ qua") }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = action != MockDoseAction.SNOOZED,
                onClick = { pending = MockDoseAction.SNOOZED }
            ) { Text("Nhắc lại") }
        } else {
            Text(
                text = when {
                    finished -> "Liều này đã được ghi nhận."
                    !isToday -> "Bạn đang xem lịch của ngày khác."
                    else -> "Bạn đang ở chế độ chỉ xem."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    val choice = pending
    if (choice != null && canAct) {
        AlertDialog(
            onDismissRequest = { pending = null },
            title = {
                Text(
                    when (choice) {
                        MockDoseAction.TAKEN -> "Xác nhận đã uống?"
                        MockDoseAction.SKIPPED -> "Xác nhận bỏ qua?"
                        MockDoseAction.SNOOZED -> "Đánh dấu nhắc lại?"
                    }
                )
            },
            text = { Text("${dose.medicineName} • ${dose.timeLabel}") },
            confirmButton = {
                TextButton(onClick = {
                    MockDoseActions.record(dose.doseId, choice)
                    pending = null
                    onBack()
                }) { Text("Xác nhận") }
            },
            dismissButton = {
                TextButton(onClick = { pending = null }) { Text("Hủy") }
            }
        )
    }
}