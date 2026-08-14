package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emadgh.pfriend.model.CircleDetail
import com.emadgh.pfriend.ui.Page

@Composable
fun CircleDetailScreen(detail: CircleDetail?, loading: Boolean, onBack: () -> Unit, onAddMember: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    Page(detail?.circle?.name ?: "Circle", onBack) {
        Text("Members", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(8.dp))
        detail?.members?.forEach { member ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Column(Modifier.padding(14.dp)) { Text(member.displayName, fontWeight = FontWeight.SemiBold); Text("@${member.username}") } }
        }
        Spacer(Modifier.height(16.dp)); Text("Add member by username"); Spacer(Modifier.height(6.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp)); Button(onClick = { onAddMember(username); username = "" }, enabled = !loading && username.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Add member") }
    }
}
