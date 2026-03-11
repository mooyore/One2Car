package com.example.one2car

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.one2car.ui.theme.One2carTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            One2carTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}