package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emadgh.pfriend.model.User
import com.emadgh.pfriend.ui.MotionKeys
import com.emadgh.pfriend.ui.materialSharedBounds

@Composable
fun PeopleScreen(users: List<User>, onUser: (User) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = users.filter { it.username.contains(query, true) || it.displayName.contains(query, true) }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text("People", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                query,
                { query = it },
                label = { Text("Search users") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
        }
        items(filtered, key = { it.id }) { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .materialSharedBounds(MotionKeys.peopleUser(user.id))
                    .clickable { onUser(user) },
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(user.displayName, fontWeight = FontWeight.SemiBold)
                    Text("@${user.username}")
                    if (user.isFollowing) Text("Following", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}
