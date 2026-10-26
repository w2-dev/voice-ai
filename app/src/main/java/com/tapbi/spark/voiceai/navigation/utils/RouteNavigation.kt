package com.tapbi.spark.voiceai.navigation.utils

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.ArrayDeque
import java.util.Deque

enum class ScreenRoute { HOME }

interface RouteNavigation {

    fun finishApp()

    fun navigate(route: String)

    fun navigate(
        route: String,
        popToRoute: String,
        inclusive: Boolean = false,
    )

    fun popUpTo(
        route: String,
        inclusive: Boolean = false,
    )

    fun pop()

    fun hasRoute(route: String): Boolean

    fun setBackStack(routes: List<String>)

    fun <T> setBackStackArgument(
        key: String,
        value: T,
    )

    fun <T> setBackStackArgument(
        route: String,
        key: String,
        value: T,
    )

    val navTransaction: SharedFlow<NavTransaction>

    fun getScreenRoute(): ScreenRoute

    fun setScreenRoute(screenRoute: ScreenRoute)
}

@Singleton
class InternalNavigation @Inject constructor() : RouteNavigation {
    private var screenRoute = ScreenRoute.HOME
    private val backstack: Deque<String> = ArrayDeque()
    private val navState = MutableSharedFlow<NavTransaction>(extraBufferCapacity = 10)

    override fun finishApp() {
        navState.tryEmit(NavTransaction.FinishApp)
    }

    override fun navigate(route: String) {
        backstack.push(route)
        navState.tryEmit(NavTransaction.NavigateToRoute(route))
    }

    override fun navigate(
        route: String,
        popToRoute: String,
        inclusive: Boolean,
    ) {
        popUpTo(popToRoute, inclusive)
        backstack.push(route)
        navState.tryEmit(NavTransaction.NavigateToRoute(route))
    }

    override fun popUpTo(
        route: String,
        inclusive: Boolean,
    ) {
        if (backstack.contains(route)) {
            while (backstack.peek() != route) {
                backstack.pop()
            }
            if (inclusive) backstack.pop()
        }
        navState.tryEmit(NavTransaction.PopToRoute(route, inclusive))
    }

    override fun pop() {
        if (backstack.isNotEmpty()) {
            backstack.pop()
        }
        navState.tryEmit(NavTransaction.Pop)
    }

    override fun hasRoute(route: String): Boolean = backstack.contains(route)

    override fun setBackStack(routes: List<String>) {
        backstack.clear()
        routes.forEach {
            backstack.push(it)
        }
    }

    override fun <T> setBackStackArgument(
        key: String,
        value: T,
    ) {
        navState.tryEmit(NavTransaction.SetPreviousBackstackArgument(key, value))
    }

    override fun <T> setBackStackArgument(
        route: String,
        key: String,
        value: T,
    ) {
        navState.tryEmit(NavTransaction.SetBackstackArgument(route, key, value))
    }

    override val navTransaction: SharedFlow<NavTransaction>
        get() = navState.asSharedFlow()

    override fun getScreenRoute(): ScreenRoute = screenRoute

    override fun setScreenRoute(screenRoute: ScreenRoute) {
        this.screenRoute = screenRoute
    }
}
