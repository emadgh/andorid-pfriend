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
import com.emadgh.pfriend.model.Circle
import com.emadgh.pfriend.ui.MotionKeys
import com.emadgh.pfriend.ui.materialSharedBounds

@Composable
fun CirclesScreen(
    circles: List<Circle>,
    loading: Boolean,
    onCreate: (String) -> Unit,
    onCircle: (Circle) -> Unit
) {
    var name by remember { mutableStateOf("") }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))
            Text("Circles", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Create membership groups. Tracker data itself remains visible to all registered users.")
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text("New circle") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = { onCreate(name); name = "" },
                    enabled = !loading && name.isNotBlank()
                ) { Text("Create") }
            }
            Spacer(Modifier.height(10.dp))
        }
        items(circles, key = { it.id }) { circle ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .materialSharedBounds(MotionKeys.circle(circle.id))
                    .clickable { onCircle(circle) },
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(circle.name, fontWeight = FontWeight.SemiBold)
                    Text("${circle.memberCount} members")
                }
            }
        }
        item { Spacer(Modifier.height(100.dp)) }
    }
}
