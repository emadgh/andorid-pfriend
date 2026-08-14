package com.emadgh.pfriend.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emadgh.pfriend.ui.screens.*

@Composable
fun PFriendApp() {
    val context = LocalContext.current
    val vm: PFriendViewModel = viewModel(factory = PFriendViewModel.factory(context))

    when {
        !vm.serverConfigured -> ServerScreen(vm.loading, vm.error) { vm.configureServer(it) }
        !vm.authenticated -> AuthScreen(
            loading = vm.loading, error = vm.error,
            onLogin = vm::login, onRegister = vm::register,
            onChangeServer = vm::resetServer
        )
        else -> MainShell(vm)
    }
}

@Composable
private fun MainShell(vm: PFriendViewModel) {
    val currentDestination = vm.destination
    val topLevel = currentDestination is Destination.Home || currentDestination is Destination.People ||
        currentDestination is Destination.Circles || currentDestination is Destination.Profile ||
        (currentDestination is Destination.Compare && currentDestination.circleId == null)
    Scaffold(
        bottomBar = {
            if (topLevel) NavigationBar {
                NavItem("Home", currentDestination is Destination.Home) { vm.navigate(Destination.Home) }
                NavItem("People", currentDestination is Destination.People) { vm.navigate(Destination.People) }
                NavItem("Compare", currentDestination is Destination.Compare && currentDestination.circleId == null) { vm.navigate(Destination.Compare()) }
                NavItem("Circles", currentDestination is Destination.Circles) { vm.navigate(Destination.Circles) }
                NavItem("Me", currentDestination is Destination.Profile) { vm.navigate(Destination.Profile) }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val d = vm.destination) {
                Destination.Home -> HomeScreen(vm.currentUser?.displayName ?: vm.currentUser?.username ?: "", vm.daily, vm.feed, { vm.navigate(Destination.QuickLog(it)) }, { vm.navigate(Destination.UserDetail(it)) })
                Destination.People -> PeopleScreen(vm.people) { vm.navigate(Destination.UserDetail(it)) }
                Destination.Circles -> CirclesScreen(vm.circles, vm.loading, vm::createCircle) { vm.navigate(Destination.CircleDetail(it)) }
                is Destination.Compare -> CompareScreen(vm.comparison, d.circleId != null, d.circleId?.let { circleId -> ({ vm.navigate(Destination.CircleDetail(circleId)) }) }) { vm.navigate(Destination.UserDetail(it)) }
                Destination.Profile -> ProfileScreen(vm.currentUser, vm::logout, vm::resetServer)
                is Destination.QuickLog -> QuickLogScreen(d.type, vm.loading, vm.error, { vm.navigate(Destination.Home) }) { amount, unit, label, note -> vm.addEntry(d.type, amount, unit, label, note) {} }
                is Destination.UserDetail -> UserProfileScreen(vm.selectedUser, vm.selectedUserDaily, vm.selectedUserEntries, vm.currentUser?.id, vm.loading, { vm.navigate(Destination.People) }, vm::toggleFollow)
                is Destination.CircleDetail -> CircleDetailScreen(vm.selectedCircle, vm.loading, { vm.navigate(Destination.Circles) }, vm::addCircleMember) { vm.navigate(Destination.Compare(d.id)) }
            }
            vm.error?.takeIf { topLevel }?.let { message ->
                Snackbar(Modifier.padding(bottom = androidx.compose.ui.unit.dp(78))) { Text(message) }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(selected = selected, onClick = onClick, icon = { Text(label.take(1)) }, label = { Text(label) })
}
