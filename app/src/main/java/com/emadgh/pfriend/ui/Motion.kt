package com.emadgh.pfriend.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

enum class SharedMotionType {
    USER,
    CIRCLE,
    QUICK_LOG,
    CIRCLE_COMPARE
}

data class SharedMotionKey(
    val type: SharedMotionType,
    val origin: String,
    val id: String
)

object MotionKeys {
    fun peopleUser(userId: Long) = SharedMotionKey(SharedMotionType.USER, "people", userId.toString())
    fun homeEntry(entryId: Long) = SharedMotionKey(SharedMotionType.USER, "home-feed", entryId.toString())
    fun compareUser(userId: Long, circleId: Long?) = SharedMotionKey(
        SharedMotionType.USER,
        circleId?.let { "compare-circle-$it" } ?: "compare-all",
        userId.toString()
    )
    fun circleMember(circleId: Long, userId: Long) =
        SharedMotionKey(SharedMotionType.USER, "circle-$circleId", userId.toString())

    fun circle(circleId: Long) = SharedMotionKey(SharedMotionType.CIRCLE, "circles", circleId.toString())
    fun quickLog(type: String) = SharedMotionKey(SharedMotionType.QUICK_LOG, "home-quick-log", type)
    fun circleCompare(circleId: Long) =
        SharedMotionKey(SharedMotionType.CIRCLE_COMPARE, "circle-detail", circleId.toString())
}

val LocalPFriendSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalPFriendAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

@Composable
fun Modifier.materialSharedBounds(key: SharedMotionKey): Modifier {
    val sharedTransitionScope = LocalPFriendSharedTransitionScope.current ?: return this
    val animatedVisibilityScope = LocalPFriendAnimatedVisibilityScope.current ?: return this

    return with(sharedTransitionScope) {
        this@materialSharedBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = { _, _ ->
                tween(durationMillis = 340, easing = FastOutSlowInEasing)
            },
            enter = fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = 50)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120))
        )
    }
}
