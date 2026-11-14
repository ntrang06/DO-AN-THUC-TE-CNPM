package com.example.dattcnpm.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MMRASearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Tìm tên thuốc"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        trailingIcon = {
            if (value.isNotEmpty()) {
                TextButton(onClick = { onValueChange("") }) {
                    Text("Xóa")
                }
            }
        }
    )
}