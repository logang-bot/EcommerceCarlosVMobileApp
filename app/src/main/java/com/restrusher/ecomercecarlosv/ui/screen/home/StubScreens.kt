package com.restrusher.ecomercecarlosv.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar

@Composable
fun ProductosStubScreen(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { PedidosTopBar(title = stringResource(R.string.nav_productos)) },
        bottomBar = { AppBottomNavBar(selectedTab = selectedTab, onTabSelected = onTabSelected) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            EmptyState(
                icon = Icons.Default.Sell,
                title = stringResource(R.string.stub_productos_title),
                subtitle = stringResource(R.string.stub_productos_subtitle),
            )
        }
    }
}
