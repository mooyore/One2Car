package com.example.one2car.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.one2car.Screen
import com.example.one2car.SharedPreferencesManager
import com.example.one2car.UserViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: UserViewModel
) {

    val context = LocalContext.current
    val sharedPref = SharedPreferencesManager(context)

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val loginResult by viewModel.loginResult.collectAsStateWithLifecycle()

    LaunchedEffect(loginResult) {
        loginResult?.let {
            val user = it.user
            if (user != null) {
                sharedPref.saveLoginStatus(
                    isLoggedIn = true,
                    userId = user.userId.toString(),
                    role = user.role,
                    email = email.trim()
                )
                viewModel.resetLoginResult()
                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()

                when (user.role) {
                    "admin" -> {
                        navController.navigate(Screen.AdminMain.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                    "seller", "dealer" -> {
                        navController.navigate("home_seller") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                    else -> {
                        navController.navigate("home_buyer") {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            } else if (it.error || it.message != null) {
                viewModel.resetLoginResult()
                Toast.makeText(context, it.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Log In",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.login(
                    email = email.trim(),
                    password = password.trim()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5C6BC0)
            )
        ) {
            Text(
                text = "Login",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                color = Color.Black
            )
            TextButton(
                onClick = {
                    navController.navigate(Screen.Register.route)
                }
            ) {
                Text(
                    text = "Register",
                    color = Color(0xFF3F51B5),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
