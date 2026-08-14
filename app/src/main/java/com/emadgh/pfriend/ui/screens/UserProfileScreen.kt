package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emadgh.pfriend.model.DailySummary
import com.emadgh.pfriend.model.Entry
import com.emadgh.pfriend.model.User
import com.emadgh.pfriend.ui.EntryRow
import com.emadgh.pfriend.ui.SharedMotionKey
import com.emadgh.pfriend.ui.SummaryGrid
import com.emadgh.pfriend.ui.materialSharedBounds

@Composable
fun UserProfileScreen(
    userId: Long,
    user: User?,
    previewDisplayName: String,
    previewUsername: String,
    summary: DailySummary,
    entries: List<Entry>,
    ownUserId: Long?,
    loading: Boolean,
    sharedKey: SharedMotionKey,
    onBack: () -> Unit,
    onFollow: () -> Unit
) {
    val resolvedUser = user?.takeIf { it.id == userId }
    val displayName = resolvedUser?.displayName ?: previewDisplayName.ifBlank { "User" }
    val username = resolvedUser?.username ?: previewUsername

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onBack) { Text("‹ Back") }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .materialSharedBounds(sharedKey),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 1.dp
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    if (username.isNotBlank()) Text("@$username")
                    if (resolvedUser != null && resolvedUser.id != ownUserId) {
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = onFollow, enabled = !loading) {
                            Text(if (resolvedUser.isFollowing) "Unfollow" else "Follow")
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Today's public stats", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            SummaryGrid(summary)
            Spacer(Modifier.height(16.dp))
            Text("Recent logs", style = MaterialTheme.typography.titleLarge)
        }
        items(entries, key = { it.id }) { EntryRow(it) }
        item { Spacer(Modifier.height(40.dp)) }
    }
}
