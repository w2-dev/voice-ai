package com.tapbi.spark.voiceai.navigation.utils

enum class NavAnimationPhase {
    Enter,
    Exit,
    PopEnter,
    PopExit
}

enum class NavSlideDirection {
    Left,
    Right,
    Up,
    Down
}

object NavAnimationSpecResolver {
    fun slideDirection(animType: NavAnimType, phase: NavAnimationPhase): NavSlideDirection {
        return when (animType) {
            NavAnimType.Horizontal -> when (phase) {
                NavAnimationPhase.Enter, NavAnimationPhase.Exit -> NavSlideDirection.Left
                NavAnimationPhase.PopEnter, NavAnimationPhase.PopExit -> NavSlideDirection.Right
            }

            NavAnimType.Vertical -> when (phase) {
                NavAnimationPhase.Enter, NavAnimationPhase.Exit -> NavSlideDirection.Up
                NavAnimationPhase.PopEnter, NavAnimationPhase.PopExit -> NavSlideDirection.Down
            }

            else -> error("Slide direction is only supported for directional animations")
        }
    }

    fun slideAndFadeOffsetSign(phase: NavAnimationPhase): Int {
        return when (phase) {
            NavAnimationPhase.Enter -> 1
            NavAnimationPhase.Exit -> -1
            NavAnimationPhase.PopEnter -> -1
            NavAnimationPhase.PopExit -> 1
        }
    }

    fun elevateScale(phase: NavAnimationPhase): Float {
        return when (phase) {
            NavAnimationPhase.Enter, NavAnimationPhase.PopExit -> 0.85f
            NavAnimationPhase.Exit, NavAnimationPhase.PopEnter -> 1.15f
        }
    }
}

