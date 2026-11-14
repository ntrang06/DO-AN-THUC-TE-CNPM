package com.example.dattcnpm.ui.screens.medicine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dattcnpm.data.mock.MockCalendar
import com.example.dattcnpm.data.mock.MockMedicines
import com.example.dattcnpm.ui.components.MMRASearchField

@Composable
fun MedicineScreen(modifier: Modifier = Modifier) {
    var searchText by rememberSaveable { mutableStateOf("") }

    val medicines = MockMedicines.medicines
    val query = searchText.trim()
    val results = medicines.filter { medicine ->
        medicine.medicineName.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MedicineHeader(medicineCount = medicines.size)
        }

        item {
            MMRASearchField(
                value = searchText,
                onValueChange = { searchText = it }
            )
        }

        if (results.isEmpty()) {
            item {
                Text(
                    text = if (medicines.isEmpty()) {
                        "Bạn chưa thêm thuốc nào."
                    } else {
                        "Không tìm thấy thuốc phù hợp."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(results, key = { it.medicineId }) { medicine ->
                val schedule = medicine.schedule
                val times = schedule?.times.orEmpty()
                    .sorted()
                    .joinToString(", ") { MockCalendar.time(it) }

                MedicineCard(
                    medicineName = medicine.medicineName,
                    quantityText = schedule?.quantityText ?: medicine.quantityText,
                    scheduleText = if (times.isBlank()) {
                        "Chưa thiết lập giờ uống"
                    } else {
                        "Giờ uống: $times"
                    }
                )
            }
        }
    }
}