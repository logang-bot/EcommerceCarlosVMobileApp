package com.restrusher.ecomercecarlosv.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.presentation.screens.CreateMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.LoginRoute
import com.restrusher.ecomercecarlosv.presentation.screens.MercadosRoute
import com.restrusher.ecomercecarlosv.ui.screen.auth.LoginScreen
import com.restrusher.ecomercecarlosv.ui.screen.mercado.CreateMercadoScreen
import com.restrusher.ecomercecarlosv.ui.screen.mercado.DetalleMercadoScreen
import com.restrusher.ecomercecarlosv.ui.screen.mercado.MercadosScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(onLoginSuccess = {
                navController.navigate(MercadosRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            })
        }
        composable<MercadosRoute> {
            MercadosScreen(navController = navController)
        }
        composable<DetalleMercadoRoute> {
            DetalleMercadoScreen(navController = navController)
        }
        composable<CreateMercadoRoute> {
            CreateMercadoScreen(navController = navController)
        }
    }
}
