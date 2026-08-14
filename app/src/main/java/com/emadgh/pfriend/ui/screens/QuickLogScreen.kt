package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emadgh.pfriend.ui.Page

@Composable
fun QuickLogScreen(type: String, loading: Boolean, error: String?, onBack: () -> Unit, onSave: (Double?, String?, String?, String?) -> Unit) {
    var amount by remember(type) { mutableStateOf("") }
    var label by remember(type) { mutableStateOf("") }
    var note by remember(type) { mutableStateOf("") }
    val title = when (type) { "water" -> "Log water"; "food" -> "Log food"; "urine" -> "Log urination"; else -> "Log bowel movement" }
    val unit = when (type) { "water", "urine" -> "ml"; "food" -> "g"; else -> null }

    Page(title, onBack) {
        if (type == "water") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(150, 250, 330, 500).forEach { v -> FilterChip(selected = amount == v.toString(), onClick = { amount = v.toString() }, label = { Text("$v ml") }) }
            }
        }
        if (type != "bowel") {
            Spacer(Modifier.height(12.dp)); OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Amount${unit?.let { " ($it)" } ?: ""}") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        if (type == "food") {
            Spacer(Modifier.height(12.dp)); OutlinedTextField(label, { label = it }, label = { Text("Food / meal") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
        if (type == "bowel") {
            Spacer(Modifier.height(12.dp)); Text("Amount")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Small", "Medium", "Large").forEach { v -> FilterChip(selected = label == v.lowercase(), onClick = { label = v.lowercase() }, label = { Text(v) }) } }
        }
        Spacer(Modifier.height(12.dp)); OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        if (!error.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(error, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(18.dp))
        Button(onClick = { onSave(amount.toDoubleOrNull(), unit, label.ifBlank { null }, note.ifBlank { null }) }, enabled = !loading, modifier = Modifier.fillMaxWidth()) { Text(if (loading) "Saving…" else "Save") }
    }
}
