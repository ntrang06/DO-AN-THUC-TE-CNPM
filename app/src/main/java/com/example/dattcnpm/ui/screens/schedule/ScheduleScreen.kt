package com.example.dattcnpm.ui.screens.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dattcnpm.data.mock.MockCalendar
import com.example.dattcnpm.data.mock.MockDoseAction
import com.example.dattcnpm.data.mock.MockDoseActions
import com.example.dattcnpm.data.mock.MockReminderDoses
import com.example.dattcnpm.ui.screens.reminder.ReminderDoseUiModel

@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier,
    onDoseClick: (String) -> Unit
) {
    var selectedDay by rememberSaveable {
        mutableLongStateOf(MockCalendar.today())
    }

    val doses = MockReminderDoses.forDate(selectedDay)
    val isToday = selectedDay == MockCalendar.today()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Lịch uống",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { selectedDay = MockCalendar.plusDays(selectedDay, -1) }
            ) {
                Text("Ngày trước")
            }

            TextButton(
                onClick = { selectedDay = MockCalendar.today() },
                enabled = !isToday
            ) {
                Text("Hôm nay")
            }

            TextButton(
                onClick = { selectedDay = MockCalendar.plusDays(selectedDay, 1) }
            ) {
                Text("Ngày sau")
            }
        }

        Text(
            text = if (isToday) {
                "Hôm nay • ${MockCalendar.date(selectedDay)}"
            } else {
                MockCalendar.date(selectedDay)
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "${doses.size} liều trong ngày",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (doses.isEmpty()) {
                item {
                    Text(
                        text = "Không có lịch uống thuốc trong ngày này.",
                        modifier = Modifier.padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(doses, key = { it.doseId }) { dose ->
                    ScheduleDoseItem(
                        dose = dose,
                        action = MockDoseActions.get(dose.doseId)?.action,
                        onClick = { onDoseClick(dose.doseId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleDoseItem(
    dose: ReminderDoseUiModel,
    action: MockDoseAction?,
    onClick: () -> Unit
) {
    val status = when (action) {
        MockDoseAction.TAKEN -> "Đã uống"
        MockDoseAction.SKIPPED -> "Đã bỏ qua"
        MockDoseAction.SNOOZED -> "Nhắc lại"
        null -> "Chưa ghi nhận"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dose.timeLabel.substringBefore(" •"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = status,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = dose.medicineName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = listOf(dose.quantityText, dose.mealInstruction)
                    .filter { it.isNotBlank() }
                    .joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}