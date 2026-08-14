package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emadgh.pfriend.model.ComparisonRow

@Composable
fun CompareScreen(rows: List<ComparisonRow>, circleMode: Boolean, onBack: (() -> Unit)? = null, onUser: (Long) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) TextButton(onClick = onBack) { Text("‹ Back") }
            Column {
                Text("Compare today", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(if (circleMode) "Circle members" else "All registered users", style = MaterialTheme.typography.bodyMedium)
            }
        }
        val scroll = rememberScrollState()
        Column(Modifier.fillMaxWidth().horizontalScroll(scroll)) {
            CompareHeader()
            LazyColumn(Modifier.width(760.dp).fillMaxHeight()) {
                items(rows, key = { it.user.id }) { row -> CompareRowView(row, onUser) }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}

@Composable
private fun CompareHeader() {
    Surface(tonalElevation = 2.dp) {
        Row(Modifier.width(760.dp).padding(vertical = 12.dp, horizontal = 12.dp)) {
            HeaderCell("Person", 170f); HeaderCell("Water", 100f); HeaderCell("Food", 100f); HeaderCell("Urine", 85f); HeaderCell("Urine ml", 100f); HeaderCell("Bowel", 85f)
        }
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, width: Float) {
    Text(text, modifier = Modifier.width(width.dp), fontWeight = FontWeight.SemiBold)
}

@Composable
private fun CompareRowView(row: ComparisonRow, onUser: (Long) -> Unit) {
    TextButton(onClick = { onUser(row.user.id) }, contentPadding = PaddingValues(0.dp)) {
        Row(Modifier.width(760.dp).padding(vertical = 10.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(170.dp)) { Text(row.user.displayName, fontWeight = FontWeight.SemiBold); Text("@${row.user.username}", style = MaterialTheme.typography.bodySmall) }
            Text("${row.summary.waterMl.toInt()} ml", Modifier.width(100.dp))
            Text("${row.summary.foodGrams.toInt()} g", Modifier.width(100.dp))
            Text(row.summary.urineCount.toString(), Modifier.width(85.dp))
            Text("${row.summary.urineMl.toInt()} ml", Modifier.width(100.dp))
            Text(row.summary.bowelCount.toString(), Modifier.width(85.dp))
        }
    }
    HorizontalDivider(Modifier.width(760.dp))
}
