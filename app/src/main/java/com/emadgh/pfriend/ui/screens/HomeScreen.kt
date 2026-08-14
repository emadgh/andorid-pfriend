package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emadgh.pfriend.model.DailySummary
import com.emadgh.pfriend.model.Entry
import com.emadgh.pfriend.ui.EntryRow
import com.emadgh.pfriend.ui.SummaryGrid

@Composable
fun HomeScreen(name: String, summary: DailySummary, feed: List<Entry>, onQuickLog: (String) -> Unit, onUser: (Long) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Spacer(Modifier.height(16.dp))
            Text("Hi, $name", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Today")
            Spacer(Modifier.height(14.dp)); SummaryGrid(summary); Spacer(Modifier.height(18.dp))
            Text("Quick log", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { onQuickLog("water") }, label = { Text("Water") })
                AssistChip(onClick = { onQuickLog("food") }, label = { Text("Food") })
                AssistChip(onClick = { onQuickLog("urine") }, label = { Text("Urine") })
                AssistChip(onClick = { onQuickLog("bowel") }, label = { Text("Bowel") })
            }
            Spacer(Modifier.height(16.dp)); Text("Everyone's recent logs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(6.dp))
        }
        items(feed, key = { it.id }) { entry -> EntryRow(entry, onClick = { onUser(entry.userId) }) }
        item { Spacer(Modifier.height(100.dp)) }
    }
}
