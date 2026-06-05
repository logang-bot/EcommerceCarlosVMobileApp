package com.restrusher.ecomercecarlosv.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.ui.screen.mercado.MercadosScreen

@Composable
fun HomeScreen(navController: NavController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    when (selectedTab) {
        0 -> MercadosScreen(
            navController = navController,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )
        1 -> ProductosStubScreen(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )
        2 -> ReporteStubScreen(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )
    }
}
