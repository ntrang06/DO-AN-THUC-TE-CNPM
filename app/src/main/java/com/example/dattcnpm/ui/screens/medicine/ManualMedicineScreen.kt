package com.example.dattcnpm.ui.screens.medicine

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dattcnpm.data.mock.MockCalendar
import com.example.dattcnpm.data.mock.MockMedicineSchedule
import com.example.dattcnpm.data.mock.MockMedicines
import com.example.dattcnpm.data.mock.isPositiveDoseQuantity


@Composable
fun ManualMedicineScreen(
    medicineId: String?,
    onClose: () -> Unit
) {
    val isNew = medicineId == null
    val medicine = MockMedicines.findById(medicineId)

    if (!isNew && medicine == null) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Không tìm thấy thuốc cần sửa.")
            TextButton(onClick = onClose) { Text("Quay lại") }
        }
        return
    }

    val original = medicine?.draft() ?: ManualMedicineDraft(
        medicineName = "",
        strength = "",
        strengthUnit = "mg",
        dosageForm = "",
        note = ""
    )
    val schedule = medicine?.schedule

    var editingSchedule by rememberSaveable(medicineId) { mutableStateOf(false) }
    var name by rememberSaveable(medicineId) { mutableStateOf(original.medicineName) }
    var strength by rememberSaveable(medicineId) { mutableStateOf(original.strength) }
    var strengthUnit by rememberSaveable(medicineId) { mutableStateOf(original.strengthUnit) }
    var form by rememberSaveable(medicineId) { mutableStateOf(original.dosageForm) }
    var note by rememberSaveable(medicineId) { mutableStateOf(original.note) }

    var quantity by rememberSaveable(medicineId) {
        mutableStateOf(schedule?.quantity.orEmpty())
    }
    var unit by rememberSaveable(medicineId) {
        mutableStateOf(schedule?.unit ?: "viên")
    }
    var times by rememberSaveable(medicineId) {
        mutableStateOf(schedule?.times.orEmpty().joinToString(", ") { MockCalendar.time(it) })
    }
    var meal by rememberSaveable(medicineId) {
        mutableStateOf(schedule?.meal.orEmpty())
    }
    var error by rememberSaveable(medicineId) { mutableStateOf<String?>(null) }

    val back: () -> Unit = {
        if (editingSchedule) {
            editingSchedule = false
            error = null
        } else {
            onClose()
        }
    }

    BackHandler(enabled = editingSchedule, onBack = back)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = back) { Text("Quay lại") }

        Text(
            text = when {
                editingSchedule && isNew -> "Thiết lập lịch uống"
                editingSchedule -> "Chỉnh lịch uống"
                isNew -> "Thêm thuốc"
                else -> "Chỉnh sửa thuốc"
            },
            style = MaterialTheme.typography.headlineSmall
        )

        if (!editingSchedule) {
            EditField("Tên thuốc", name, { name = it })
            EditField("Hàm lượng", strength, { strength = it }, KeyboardType.Decimal)
            EditField("Đơn vị hàm lượng", strengthUnit, { strengthUnit = it })
            EditField("Dạng thuốc", form, { form = it })
            EditField("Ghi chú", note, { note = it })
        } else {
            Text(name.trim(), style = MaterialTheme.typography.titleMedium)
            EditField("Số lượng mỗi lần", quantity, { quantity = it }, KeyboardType.Decimal)
            EditField("Đơn vị uống", unit, { unit = it })
            EditField("Giờ uống", times, { times = it })
            Text(
                "Nhập theo giờ 24 giờ, ví dụ: 08:00, 20:00.",
                style = MaterialTheme.typography.bodySmall
            )
            EditField("Hướng dẫn theo đơn", meal, { meal = it })
        }

        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (!editingSchedule) {
                    error = when {
                        name.isBlank() -> "Vui lòng nhập tên thuốc."
                        strength.isNotBlank() && !isPositiveDoseQuantity(strength.trim()) ->
                            "Hàm lượng phải là số lớn hơn 0."
                        strength.isNotBlank() && strengthUnit.isBlank() ->
                            "Vui lòng nhập đơn vị hàm lượng."
                        else -> null
                    }
                    if (error == null) editingSchedule = true
                } else {
                    val minutes = parseMedicineTimes(times)
                    when {
                        !isPositiveDoseQuantity(quantity.trim()) ->
                            error = "Số lượng mỗi lần phải lớn hơn 0."

                        unit.isBlank() ->
                            error = "Vui lòng nhập đơn vị uống."

                        minutes == null ->
                            error = "Giờ uống chưa hợp lệ hoặc bị trùng. Ví dụ: 08:00, 20:00."

                        else -> {
                            val updatedDraft = original.copy(
                                medicineName = name.trim(),
                                strength = strength.trim(),
                                strengthUnit = strengthUnit.trim(),
                                dosageForm = form.trim(),
                                note = note.trim()
                            )

                            // Giữ nguyên ngày, thứ trong tuần và các thiết lập
                            // khác của lịch cũ; chỉ đổi những ô vừa chỉnh.
                            val updatedSchedule = schedule?.copy(
                                quantity = quantity.trim(),
                                unit = unit.trim(),
                                times = minutes,
                                meal = meal.trim()
                            ) ?: MockMedicineSchedule(
                                quantity = quantity.trim(),
                                unit = unit.trim(),
                                times = minutes,
                                meal = meal.trim()
                            )

                            MockMedicines.save(updatedDraft, updatedSchedule, medicineId)
                            onClose()
                        }
                    }
                }
            }
        ) {
            Text(if (editingSchedule) "Lưu lịch uống" else "Tiếp tục")
        }
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

private fun parseMedicineTimes(input: String): List<Int>? {
    val pattern = Regex("""(?:[01]?\d|2[0-3]):[0-5]\d""")
    val result = mutableListOf<Int>()

    for (part in input.split(",")) {
        val time = part.trim()
        if (!pattern.matches(time)) return null

        val (hour, minute) = time.split(":")
        result.add(hour.toInt() * 60 + minute.toInt())
    }

    return result.sorted().takeIf { it.isNotEmpty() && it.distinct().size == it.size }
}