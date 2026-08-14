package com.emadgh.pfriend.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emadgh.pfriend.model.DailySummary
import com.emadgh.pfriend.model.Entry

@Composable
fun Page(title: String, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) TextButton(onClick = onBack) { Text("‹ Back") }
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
fun SummaryGrid(summary: DailySummary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard("Water", "${summary.waterMl.toInt()} ml", Modifier.weight(1f))
        StatCard("Food", "${summary.foodGrams.toInt()} g", Modifier.weight(1f))
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard("Urination", "${summary.urineCount} times", Modifier.weight(1f))
        StatCard("Bowel", "${summary.bowelCount} times", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(5.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun EntryRow(entry: Entry, onClick: (() -> Unit)? = null) {
    val title = entry.displayName ?: entry.username ?: "User"
    val value = when (entry.type) {
        "water" -> "${entry.amount?.toInt() ?: 0} ml water"
        "food" -> "${entry.label ?: "Food"}${entry.amount?.let { " · ${it.toInt()} g" } ?: ""}"
        "urine" -> entry.amount?.let { "Urination · ${it.toInt()} ml" } ?: "Urination"
        "bowel" -> "Bowel movement${entry.label?.let { " · $it" } ?: ""}"
        else -> entry.type
    }
    Surface(
        modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(value)
            Text(entry.occurredAt, style = MaterialTheme.typography.bodySmall)
        }
    }
}
