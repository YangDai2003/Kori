package org.yangdai.kori.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry

expect fun getEnterTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)
expect fun getExitTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition)
expect fun getPopEnterTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)
expect fun getPopExitTransition(): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition)