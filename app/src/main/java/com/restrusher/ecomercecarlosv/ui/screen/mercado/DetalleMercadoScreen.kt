package com.restrusher.ecomercecarlosv.ui.screen.mercado

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme

@Composable
fun DetalleMercadoScreen(
    navController: NavController,
    viewModel: DetalleMercadoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetalleMercadoContent(
        state = state,
        onBack = { navController.popBackStack() },
        onAddClienteClick = { /* TODO: navigate to CreateClienteRoute — Phase 3 */ },
        onListaNegraClick = { /* TODO: navigate to ListaNegraRoute(mercadoId) — Phase 7 */ },
    )
}

@Composable
private fun DetalleMercadoContent(
    state: DetalleMercadoUiState,
    onBack: () -> Unit,
    onAddClienteClick: () -> Unit,
    onListaNegraClick: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = state.mercado?.name ?: "",
                onBack = onBack,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.clientes_fab)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onAddClienteClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            )
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            else -> EmptyState(
                modifier = Modifier.padding(innerPadding),
                icon = Icons.Default.Person,
                title = stringResource(R.string.detalle_mercado_empty_title),
                subtitle = stringResource(R.string.detalle_mercado_empty_subtitle),
                hint = stringResource(R.string.clientes_fab),
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun DetalleMercadoScreenDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        DetalleMercadoContent(DetalleMercadoUiState(isLoading = false), {}, {}, {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun DetalleMercadoScreenPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        DetalleMercadoContent(DetalleMercadoUiState(isLoading = false), {}, {}, {})
    }
}
