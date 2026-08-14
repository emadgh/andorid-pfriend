package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ServerScreen(loading: Boolean, error: String?, onConnect: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
        Text("PFriend", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Connect this native app to your PFriend PHP server.")
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = url, onValueChange = { url = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Server URL") }, placeholder = { Text("https://example.com/pfriend/") }, singleLine = true
        )
        if (!error.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(error, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onConnect(url) }, enabled = !loading && url.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "Connecting…" else "Connect")
        }
    }
}
