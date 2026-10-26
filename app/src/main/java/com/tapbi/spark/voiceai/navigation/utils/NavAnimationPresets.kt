package com.tapbi.spark.voiceai.navigation.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.animatedComposable(
    route: String,
    animType: NavAnimType = NavAnimType.Horizontal,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = {
            enterTransitionFor(animType)
        },
        exitTransition = {
            exitTransitionFor(animType)
        },
        popEnterTransition = {
            popEnterTransitionFor(animType)
        },
        popExitTransition = {
            popExitTransitionFor(animType)
        },
        content = { content(it) }
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransitionFor(
    animType: NavAnimType
): EnterTransition {
    return when (animType) {
        NavAnimType.SlideAndFade -> {
            slideInHorizontally(
                initialOffsetX = {
                    NavAnimationSpecResolver.slideAndFadeOffsetSign(
                        NavAnimationPhase.Enter
                    ) * (it / 3)
                },
                animationSpec = tween(600)
            ) + fadeIn(tween(600))
        }

        NavAnimType.Expand -> {
            expandIn(expandFrom = Alignment.Center, animationSpec = tween(500)) +
                scaleIn(initialScale = 0.8f) + fadeIn()
        }

        NavAnimType.Elevate -> {
            scaleIn(
                initialScale = NavAnimationSpecResolver.elevateScale(NavAnimationPhase.Enter),
                animationSpec = spring(Spring.DampingRatioLowBouncy)
            ) + fadeIn()
        }

        NavAnimType.Horizontal,
        NavAnimType.Vertical -> slideIntoContainer(
            NavAnimationSpecResolver.slideDirection(animType, NavAnimationPhase.Enter)
                .toComposeSlideDirection(),
            tween(500)
        )

        NavAnimType.Fade -> fadeIn(tween(500))
        NavAnimType.Scale -> scaleIn(initialScale = 0.8f) + fadeIn()
        NavAnimType.None -> EnterTransition.None
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransitionFor(
    animType: NavAnimType
): ExitTransition {
    return when (animType) {
        NavAnimType.SlideAndFade -> {
            slideOutHorizontally(
                targetOffsetX = {
                    NavAnimationSpecResolver.slideAndFadeOffsetSign(
                        NavAnimationPhase.Exit
                    ) * (it / 3)
                },
                animationSpec = tween(600)
            ) + fadeOut(tween(600))
        }

        NavAnimType.Expand -> {
            shrinkOut(
                shrinkTowards = Alignment.Center,
                animationSpec = tween(500)
            ) + scaleOut(targetScale = 0.8f) + fadeOut()
        }

        NavAnimType.Elevate -> {
            scaleOut(
                targetScale = NavAnimationSpecResolver.elevateScale(NavAnimationPhase.Exit),
                animationSpec = tween(500)
            ) + fadeOut()
        }

        NavAnimType.Horizontal,
        NavAnimType.Vertical -> slideOutOfContainer(
            NavAnimationSpecResolver.slideDirection(animType, NavAnimationPhase.Exit)
                .toComposeSlideDirection(),
            tween(500)
        )

        NavAnimType.None -> ExitTransition.None
        else -> fadeOut(tween(500))
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransitionFor(
    animType: NavAnimType
): EnterTransition {
    return when (animType) {
        NavAnimType.SlideAndFade -> {
            slideInHorizontally(
                initialOffsetX = {
                    NavAnimationSpecResolver.slideAndFadeOffsetSign(
                        NavAnimationPhase.PopEnter
                    ) * (it / 3)
                },
                animationSpec = tween(600)
            ) + fadeIn(tween(600))
        }

        NavAnimType.Expand -> {
            expandIn(expandFrom = Alignment.Center, animationSpec = tween(500)) +
                scaleIn(initialScale = 0.8f) + fadeIn()
        }

        NavAnimType.Elevate -> {
            scaleIn(
                initialScale = NavAnimationSpecResolver.elevateScale(NavAnimationPhase.PopEnter),
                animationSpec = spring(Spring.DampingRatioLowBouncy)
            ) + fadeIn()
        }

        NavAnimType.Horizontal,
        NavAnimType.Vertical -> slideIntoContainer(
            NavAnimationSpecResolver.slideDirection(animType, NavAnimationPhase.PopEnter)
                .toComposeSlideDirection(),
            tween(500)
        )

        NavAnimType.Fade -> fadeIn(tween(500))
        NavAnimType.Scale -> scaleIn(initialScale = 0.8f) + fadeIn()
        NavAnimType.None -> EnterTransition.None
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransitionFor(
    animType: NavAnimType
): ExitTransition {
    return when (animType) {
        NavAnimType.SlideAndFade -> {
            slideOutHorizontally(
                targetOffsetX = {
                    NavAnimationSpecResolver.slideAndFadeOffsetSign(
                        NavAnimationPhase.PopExit
                    ) * (it / 3)
                },
                animationSpec = tween(600)
            ) + fadeOut(tween(600))
        }

        NavAnimType.Expand -> {
            shrinkOut(
                shrinkTowards = Alignment.Center,
                animationSpec = tween(500)
            ) + scaleOut(targetScale = 0.8f) + fadeOut()
        }

        NavAnimType.Elevate -> {
            scaleOut(
                targetScale = NavAnimationSpecResolver.elevateScale(NavAnimationPhase.PopExit),
                animationSpec = tween(500)
            ) + fadeOut()
        }

        NavAnimType.Horizontal,
        NavAnimType.Vertical -> slideOutOfContainer(
            NavAnimationSpecResolver.slideDirection(animType, NavAnimationPhase.PopExit)
                .toComposeSlideDirection(),
            tween(500)
        )

        NavAnimType.None -> ExitTransition.None
        else -> fadeOut(tween(500))
    }
}

private fun NavSlideDirection.toComposeSlideDirection(): AnimatedContentTransitionScope.SlideDirection {
    return when (this) {
        NavSlideDirection.Left -> AnimatedContentTransitionScope.SlideDirection.Left
        NavSlideDirection.Right -> AnimatedContentTransitionScope.SlideDirection.Right
        NavSlideDirection.Up -> AnimatedContentTransitionScope.SlideDirection.Up
        NavSlideDirection.Down -> AnimatedContentTransitionScope.SlideDirection.Down
    }
}

enum class NavAnimType {
    Horizontal, Vertical, Fade, Scale, None,
    SlideAndFade,
    Expand,
    Elevate
}
