package com.restrusher.ecomercecarlosv.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.presentation.screens.AgregarListaNegraRoute
import com.restrusher.ecomercecarlosv.presentation.screens.EditarPedidoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.BusquedaRoute
import com.restrusher.ecomercecarlosv.presentation.screens.SaldoExtraRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreacionPedidoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetallePedidoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.ListaNegraRoute
import com.restrusher.ecomercecarlosv.presentation.screens.ClientesRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreateClienteRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreateMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CreateProductoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleClienteRoute
import com.restrusher.ecomercecarlosv.presentation.screens.DetalleMercadoRoute
import com.restrusher.ecomercecarlosv.presentation.screens.EditarPerfilRoute
import com.restrusher.ecomercecarlosv.presentation.screens.GestionUsuariosRoute
import com.restrusher.ecomercecarlosv.presentation.screens.HomeRoute
import com.restrusher.ecomercecarlosv.presentation.screens.CrearUsuarioRoute
import com.restrusher.ecomercecarlosv.presentation.screens.LoginRoute
import com.restrusher.ecomercecarlosv.presentation.screens.PerfilRoute
import com.restrusher.ecomercecarlosv.presentation.screens.UmbralesRoute
import com.restrusher.ecomercecarlosv.presentation.screens.UsuarioDetalleRoute
import com.restrusher.ecomercecarlosv.ui.screen.auth.LoginScreen
import com.restrusher.ecomercecarlosv.ui.screen.busqueda.BusquedaScreen
import com.restrusher.ecomercecarlosv.ui.screen.cliente.ClientesScreen
import com.restrusher.ecomercecarlosv.ui.screen.cliente.CreateClienteScreen
import com.restrusher.ecomercecarlosv.ui.screen.cliente.DetalleClienteScreen
import com.restrusher.ecomercecarlosv.ui.screen.cliente.SaldoExtraScreen
import com.restrusher.ecomercecarlosv.ui.screen.lista_negra.AgregarListaNegraScreen
import com.restrusher.ecomercecarlosv.ui.screen.lista_negra.ListaNegraScreen
import com.restrusher.ecomercecarlosv.ui.screen.pedido.CreacionPedidoScreen
import com.restrusher.ecomercecarlosv.ui.screen.pedido.DetallePedidoScreen
import com.restrusher.ecomercecarlosv.presentation.screens.ReporteClienteRoute
import com.restrusher.ecomercecarlosv.ui.screen.pedido.EditarPedidoScreen
import com.restrusher.ecomercecarlosv.ui.screen.reporte.ReporteClienteScreen
import com.restrusher.ecomercecarlosv.ui.screen.producto.CreateProductoScreen
import com.restrusher.ecomercecarlosv.ui.screen.home.HomeScreen
import com.restrusher.ecomercecarlosv.ui.screen.mercado.CreateMercadoScreen
import com.restrusher.ecomercecarlosv.ui.screen.mercado.DetalleMercadoScreen
import com.restrusher.ecomercecarlosv.ui.screen.perfil.EditarPerfilScreen
import com.restrusher.ecomercecarlosv.ui.screen.perfil.PerfilScreen
import com.restrusher.ecomercecarlosv.ui.screen.perfil.UmbralesScreen
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
        composable<UmbralesRoute> {
            UmbralesScreen(navController = navController)
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
        composable<BusquedaRoute> {
            BusquedaScreen(navController = navController)
        }
        composable<ClientesRoute> {
            ClientesScreen(navController = navController)
        }
        composable<DetalleClienteRoute> {
            DetalleClienteScreen(navController = navController)
        }
        composable<CreateClienteRoute> {
            CreateClienteScreen(navController = navController)
        }
        composable<CreateProductoRoute> {
            CreateProductoScreen(navController = navController)
        }
        composable<ListaNegraRoute> {
            ListaNegraScreen(navController = navController)
        }
        composable<AgregarListaNegraRoute> {
            AgregarListaNegraScreen(navController = navController)
        }
        composable<CreacionPedidoRoute> {
            CreacionPedidoScreen(navController = navController)
        }
        composable<SaldoExtraRoute> {
            SaldoExtraScreen(navController = navController)
        }
        composable<DetallePedidoRoute> {
            DetallePedidoScreen(navController = navController)
        }
        composable<EditarPedidoRoute> {
            EditarPedidoScreen(navController = navController)
        }
        composable<ReporteClienteRoute> {
            ReporteClienteScreen(navController = navController)
        }
    }
}
