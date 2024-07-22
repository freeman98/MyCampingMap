/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.testcomposeui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {
    data object Home : Screen("home")

    data object Splash : Screen(
        route = "Splash",
        navArguments = listOf(navArgument("plantId") {
            type = NavType.StringType
        })
    ) {
//        fun createRoute(plantId: String) = "plantDetail/${plantId}"
    }

    data object Login : Screen(
        route = "Login",
        navArguments = listOf(navArgument("plantName") {
            type = NavType.StringType
        })
    ) {
//        fun createRoute(plantName: String) = "gallery/${plantName}"=
    }

    data object Signup : Screen(
        route = "Signup",
        navArguments = listOf(navArgument("plantName") {
            type = NavType.StringType
        })
    ) {
//        fun createRoute(plantName: String) = "gallery/${plantName}"=
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController)
        }
        composable(Screen.Home.route) {
            MainScreen()
        }
    }
}

@Preview
@Composable
fun NavGraphPreview() {
    NavGraph(navController = NavHostController(LocalContext.current))
}