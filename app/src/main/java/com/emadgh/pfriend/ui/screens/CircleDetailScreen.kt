package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emadgh.pfriend.model.CircleDetail
import com.emadgh.pfriend.model.CircleMember
import com.emadgh.pfriend.ui.MotionKeys
import com.emadgh.pfriend.ui.Page
import com.emadgh.pfriend.ui.materialSharedBounds

@Composable
fun CircleDetailScreen(
    circleId: Long,
    previewName: String,
    detail: CircleDetail?,
    loading: Boolean,
    onBack: () -> Unit,
    onAddMember: (String) -> Unit,
    onUser: (CircleMember) -> Unit,
    onCompare: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    val title = detail?.circle?.takeIf { it.id == circleId }?.name ?: previewName.ifBlank { "Circle" }

    Page(
        title = title,
        onBack = onBack,
        headerModifier = Modifier.materialSharedBounds(MotionKeys.circle(circleId))
    ) {
        Button(
            onClick = onCompare,
            modifier = Modifier
                .fillMaxWidth()
                .materialSharedBounds(MotionKeys.circleCompare(circleId))
        ) {
            Text("Compare members today")
        }
        Spacer(Modifier.height(14.dp))
        Text("Members", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        detail?.takeIf { it.circle.id == circleId }?.members?.forEach { member ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .materialSharedBounds(MotionKeys.circleMember(circleId, member.id))
                    .clickable { onUser(member) }
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(member.displayName, fontWeight = FontWeight.SemiBold)
                    Text("@${member.username}")
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Add member by username")
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            username,
            { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onAddMember(username); username = "" },
            enabled = !loading && username.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add member")
        }
    }
}
