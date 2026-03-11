package com.example.one2car.filenew

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*


@Composable
fun NavGraph() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeDealerScreen(navController)
        }

        composable("addcar") {
            AddCarScreen(navController)
        }

        composable("edit_car/{id}") { backStackEntry ->

            val id = backStackEntry.arguments
                ?.getString("id")
                ?.toInt() ?: 0

            EditCarScreen(
                navController = navController,
                carId = id
            )
        }

        composable("car_detail/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toInt() ?: 0
            CarDetailScreen(navController = navController, id = id)
        }

    }

}
