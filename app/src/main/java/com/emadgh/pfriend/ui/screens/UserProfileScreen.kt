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
import com.emadgh.pfriend.model.User
import com.emadgh.pfriend.ui.EntryRow
import com.emadgh.pfriend.ui.SummaryGrid

@Composable
fun UserProfileScreen(user: User?, summary: DailySummary, entries: List<Entry>, ownUserId: Long?, loading: Boolean, onBack: () -> Unit, onFollow: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Spacer(Modifier.height(10.dp)); TextButton(onClick = onBack) { Text("‹ Back") }
            Text(user?.displayName ?: "User", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text(user?.let { "@${it.username}" } ?: "")
            if (user != null && user.id != ownUserId) { Spacer(Modifier.height(8.dp)); Button(onClick = onFollow, enabled = !loading) { Text(if (user.isFollowing) "Unfollow" else "Follow") } }
            Spacer(Modifier.height(16.dp)); Text("Today's public stats", style = MaterialTheme.typography.titleLarge); Spacer(Modifier.height(8.dp)); SummaryGrid(summary)
            Spacer(Modifier.height(16.dp)); Text("Recent logs", style = MaterialTheme.typography.titleLarge)
        }
        items(entries, key = { it.id }) { EntryRow(it) }
        item { Spacer(Modifier.height(40.dp)) }
    }
}
