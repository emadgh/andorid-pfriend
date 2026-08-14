package com.emadgh.pfriend.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(loading: Boolean, error: String?, onLogin: (String, String) -> Unit, onRegister: (String, String, String, String, Boolean) -> Unit, onChangeServer: () -> Unit) {
    var register by remember { mutableStateOf(false) }
    var identity by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp), verticalArrangement = Arrangement.Center) {
        Text("PFriend", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(if (register) "Create account" else "Sign in")
        Spacer(Modifier.height(24.dp))
        if (register) {
            OutlinedTextField(username, { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp)); OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp)); OutlinedTextField(name, { name = it }, label = { Text("Display name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        } else OutlinedTextField(identity, { identity = it }, label = { Text("Username or email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), singleLine = true)
        if (register) {
            Spacer(Modifier.height(14.dp)); Row { Checkbox(accepted, { accepted = it }); Text("I understand and accept that all tracker data I enter can be viewed by other registered PFriend users.", modifier = Modifier.padding(top = 10.dp)) }
        }
        if (!error.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(error, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (register) onRegister(username, email, name, password, accepted) else onLogin(identity, password) }, enabled = !loading && password.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text(if (loading) "Please wait…" else if (register) "Create account" else "Sign in") }
        TextButton(onClick = { register = !register }, modifier = Modifier.fillMaxWidth()) { Text(if (register) "Already have an account? Sign in" else "New here? Create account") }
        TextButton(onClick = onChangeServer, modifier = Modifier.fillMaxWidth()) { Text("Change server") }
    }
}
