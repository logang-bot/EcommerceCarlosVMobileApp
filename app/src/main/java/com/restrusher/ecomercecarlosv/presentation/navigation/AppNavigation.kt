package com.restrusher.ecomercecarlosv.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.presentation.screens.CreateMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.EditarPerfilRoute
import com.restrusher.ecomercecarlosv.presentation.screens.GestionUsuariosRoute
import com.restrusher.ecomercecarlosv.presentation.screens.HomeRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CrearUsuarioRoute
import com.restrusher.ecomercecarlosv.presentation.screens.LoginRoute
import com.restrusher.ecomercecarlosv.presentation.screens.PerfilRoute
import com.restrusher.ecomercecarlosv.presentation.screens.UsuarioDetalleRoute
import com.restrusher.ecomercecarlosv.ui.screen.auth.LoginScreen
import com.restrusher.ecomercecarlosv.ui.screen.home.HomeScreen
import com.restrusher.ecomercecarlosv.ui.screen.mercado.CreateMercadoScreen
import com.restrusher.ecomercecarlosv.ui.screen.mercado.DetalleMercadoScreen
import com.restrusher.ecomercecarlosv.ui.screen.perfil.EditarPerfilScreen
import com.restrusher.ecomercecarlosv.ui.screen.perfil.PerfilScreen
import com.restrusher.ecomercecarlosv.ui.screen.usuario.GestionUsuariosScreen
import com.restrusher.ecomercecarlosv.ui.screen.usuario.CrearUsuarioScreen
import com.restrusher.ecomercecarlosv.ui.screen.usuario.UsuarioDetalleScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(onLoginSuccess = {
                navController.navigate(HomeRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            })
        }
        composable<HomeRoute> {
            HomeScreen(navController = navController)
        }
        composable<DetalleMercadoRoute> {
            DetalleMercadoScreen(navController = navController)
        }
        composable<CreateMercadoRoute> {
            CreateMercadoScreen(navController = navController)
        }
        composable<PerfilRoute> {
            PerfilScreen(navController = navController)
        }
        composable<EditarPerfilRoute> {
            EditarPerfilScreen(navController = navController)
        }
        composable<GestionUsuariosRoute> {
            GestionUsuariosScreen(navController = navController)
        }
        composable<UsuarioDetalleRoute> {
            UsuarioDetalleScreen(navController = navController)
        }
        composable<CrearUsuarioRoute> {
            CrearUsuarioScreen(navController = navController)
        }
    }
}
