package com.emadgh.pfriend.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emadgh.pfriend.ui.screens.*

@Composable
fun PFriendApp() {
    val context = LocalContext.current
    val vm: PFriendViewModel = viewModel(factory = PFriendViewModel.factory(context))

    when {
        !vm.serverConfigured -> ServerScreen(vm.loading, vm.error) { vm.configureServer(it) }
        !vm.authenticated -> AuthScreen(
            loading = vm.loading,
            error = vm.error,
            onLogin = vm::login,
            onRegister = vm::register,
            onChangeServer = vm::resetServer
        )
        else -> MainShell(vm)
    }
}

@Composable
private fun MainShell(vm: PFriendViewModel) {
    val currentDestination = vm.destination
    val topLevel = currentDestination.isTopLevelDestination()
    val backDestination = currentDestination.backDestination()

    BackHandler(enabled = backDestination != null) {
        backDestination?.let(vm::navigate)
    }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        val sharedTransitionScope = this

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = topLevel,
                    enter = fadeIn(tween(180, delayMillis = 60)) +
                        slideInVertically(tween(240)) { it / 4 },
                    exit = fadeOut(tween(120)) +
                        slideOutVertically(tween(180)) { it / 4 }
                ) {
                    NavigationBar {
                        NavItem("Home", currentDestination is Destination.Home) { vm.navigate(Destination.Home) }
                        NavItem("People", currentDestination is Destination.People) { vm.navigate(Destination.People) }
                        NavItem(
                            "Compare",
                            currentDestination is Destination.Compare && currentDestination.circleId == null
                        ) { vm.navigate(Destination.Compare()) }
                        NavItem("Circles", currentDestination is Destination.Circles) { vm.navigate(Destination.Circles) }
                        NavItem("Me", currentDestination is Destination.Profile) { vm.navigate(Destination.Profile) }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = { materialScreenTransform(initialState, targetState) },
                    contentAlignment = Alignment.TopStart,
                    label = "pfriend-destination",
                    modifier = Modifier.fillMaxSize()
                ) { destination ->
                    val animatedVisibilityScope = this
                    CompositionLocalProvider(
                        LocalPFriendSharedTransitionScope provides sharedTransitionScope,
                        LocalPFriendAnimatedVisibilityScope provides animatedVisibilityScope
                    ) {
                        DestinationContent(destination, vm)
                    }
                }

                vm.error?.takeIf { topLevel }?.let { message ->
                    Snackbar(Modifier.padding(bottom = 78.dp)) { Text(message) }
                }
            }
        }
    }
}

@Composable
private fun DestinationContent(destination: Destination, vm: PFriendViewModel) {
    when (destination) {
        Destination.Home -> HomeScreen(
            name = vm.currentUser?.displayName ?: vm.currentUser?.username ?: "",
            summary = vm.daily,
            feed = vm.feed,
            onQuickLog = { vm.navigate(Destination.QuickLog(it)) },
            onUser = { entry ->
                vm.navigate(
                    Destination.UserDetail(
                        id = entry.userId,
                        sharedKey = MotionKeys.homeEntry(entry.id),
                        back = Destination.Home,
                        previewDisplayName = entry.displayName ?: entry.username ?: "User",
                        previewUsername = entry.username.orEmpty()
                    )
                )
            }
        )

        Destination.People -> PeopleScreen(vm.people) { user ->
            vm.navigate(
                Destination.UserDetail(
                    id = user.id,
                    sharedKey = MotionKeys.peopleUser(user.id),
                    back = Destination.People,
                    previewDisplayName = user.displayName,
                    previewUsername = user.username
                )
            )
        }

        Destination.Circles -> CirclesScreen(vm.circles, vm.loading, vm::createCircle) { circle ->
            vm.navigate(Destination.CircleDetail(circle.id, circle.name))
        }

        is Destination.Compare -> CompareScreen(
            rows = vm.comparison,
            circleId = destination.circleId,
            onBack = destination.circleId?.let { { vm.navigate(Destination.CircleDetail(it, destination.circleName.orEmpty())) } },
            onUser = { row ->
                vm.navigate(
                    Destination.UserDetail(
                        id = row.user.id,
                        sharedKey = MotionKeys.compareUser(row.user.id, destination.circleId),
                        back = destination,
                        previewDisplayName = row.user.displayName,
                        previewUsername = row.user.username
                    )
                )
            }
        )

        Destination.Profile -> ProfileScreen(vm.currentUser, vm::logout, vm::resetServer)

        is Destination.QuickLog -> QuickLogScreen(
            type = destination.type,
            loading = vm.loading,
            error = vm.error,
            onBack = { vm.navigate(Destination.Home) }
        ) { amount, unit, label, note ->
            vm.addEntry(destination.type, amount, unit, label, note) {}
        }

        is Destination.UserDetail -> UserProfileScreen(
            userId = destination.id,
            user = vm.selectedUser,
            previewDisplayName = destination.previewDisplayName,
            previewUsername = destination.previewUsername,
            summary = vm.selectedUserDaily,
            entries = vm.selectedUserEntries,
            ownUserId = vm.currentUser?.id,
            loading = vm.loading,
            sharedKey = destination.sharedKey,
            onBack = { vm.navigate(destination.back) },
            onFollow = vm::toggleFollow
        )

        is Destination.CircleDetail -> CircleDetailScreen(
            circleId = destination.id,
            previewName = destination.previewName,
            detail = vm.selectedCircle,
            loading = vm.loading,
            onBack = { vm.navigate(Destination.Circles) },
            onAddMember = vm::addCircleMember,
            onUser = { member ->
                vm.navigate(
                    Destination.UserDetail(
                        id = member.id,
                        sharedKey = MotionKeys.circleMember(destination.id, member.id),
                        back = destination,
                        previewDisplayName = member.displayName,
                        previewUsername = member.username
                    )
                )
            },
            onCompare = {
                val circleName = vm.selectedCircle?.circle?.name ?: destination.previewName
                vm.navigate(Destination.Compare(destination.id, circleName))
            }
        )
    }
}

private fun Destination.isTopLevelDestination(): Boolean =
    this is Destination.Home || this is Destination.People || this is Destination.Circles ||
        this is Destination.Profile || (this is Destination.Compare && circleId == null)

private fun Destination.backDestination(): Destination? = when (this) {
    is Destination.QuickLog -> Destination.Home
    is Destination.UserDetail -> back
    is Destination.CircleDetail -> Destination.Circles
    is Destination.Compare -> circleId?.let { Destination.CircleDetail(it, circleName.orEmpty()) }
    else -> null
}

private fun Destination.isDetailDestination(): Boolean = when (this) {
    is Destination.QuickLog,
    is Destination.UserDetail,
    is Destination.CircleDetail -> true
    is Destination.Compare -> circleId != null
    else -> false
}

private fun materialScreenTransform(initial: Destination, target: Destination): ContentTransform {
    val openingDetail = !initial.isDetailDestination() && target.isDetailDestination()
    val closingDetail = initial.isDetailDestination() && !target.isDetailDestination()

    return when {
        openingDetail ->
            (fadeIn(tween(220, delayMillis = 60)) + scaleIn(tween(300), initialScale = 0.985f))
                .togetherWith(fadeOut(tween(130)) + scaleOut(tween(180), targetScale = 0.995f))

        closingDetail ->
            (fadeIn(tween(190, delayMillis = 30)) + scaleIn(tween(260), initialScale = 1.015f))
                .togetherWith(fadeOut(tween(140)))

        else ->
            (fadeIn(tween(180, delayMillis = 55)) + scaleIn(tween(240), initialScale = 0.98f))
                .togetherWith(fadeOut(tween(120)) + scaleOut(tween(180), targetScale = 1.01f))
    }
}

@Composable
private fun RowScope.NavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Text(label.take(1)) },
        label = { Text(label) }
    )
}
