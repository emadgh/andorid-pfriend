package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emadgh.pfriend.model.User

@Composable
fun ProfileScreen(user: User?, onLogout: () -> Unit, onResetServer: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(Modifier.height(16.dp)); Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.height(18.dp))
        Text(user?.displayName ?: ""); Text(user?.let { "@${it.username}" } ?: ""); user?.email?.let { Text(it) }
        Spacer(Modifier.height(22.dp))
        Card { Text("Visibility rule: all tracker data you add is visible to other authenticated PFriend users. There are no per-entry privacy controls in this build.", Modifier.padding(16.dp)) }
        Spacer(Modifier.height(18.dp)); Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
        Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = onResetServer, modifier = Modifier.fillMaxWidth()) { Text("Forget server and account") }
    }
}
