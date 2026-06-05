package com.restrusher.ecomercecarlosv.ui.screen.busqueda

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun BusquedaScreen(
    navController: NavController,
    viewModel: BusquedaViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    BusquedaContent(
        state = state,
        focusRequester = focusRequester,
        onBack = { navController.popBackStack() },
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::clearQuery,
        onResultClick = { /* TODO Phase 3: navController.navigate(DetalleClienteRoute(it)) */ },
    )
}

@Composable
private fun BusquedaContent(
    state: BusquedaUiState,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onResultClick: (String) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                SearchField(
                    query = state.query,
                    focusRequester = focusRequester,
                    onQueryChange = onQueryChange,
                    onClearQuery = onClearQuery,
                    modifier = Modifier.weight(1f),
                )
            }

            when {
                state.query.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = stringResource(R.string.busqueda_no_query_title),
                        subtitle = stringResource(R.string.busqueda_no_query_subtitle),
                    )
                }
                state.results.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Person,
                        title = stringResource(R.string.busqueda_empty_title),
                        subtitle = stringResource(R.string.busqueda_empty_subtitle),
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.busqueda_results_count, state.results.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.text3,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                    )
                    LazyColumn {
                        items(state.results, key = { it.clienteId }) { result ->
                            SearchResultRow(result = result, onClick = { onResultClick(result.clienteId) })
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 75.dp),
                                color = ext.border,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.focusRequester(focusRequester),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(ext.surface2)
                    .border(1.dp, ext.border2, RoundedCornerShape(13.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = ext.text2, modifier = Modifier.size(19.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.busqueda_placeholder),
                            style = MaterialTheme.typography.bodyLarge,
                            color = ext.text3,
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClearQuery,
                        modifier = Modifier.size(22.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(ext.surface3),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = ext.text2, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun SearchResultRow(result: ClienteSearchResult, onClick: () -> Unit) {
    // TODO Phase 3: render avatar, status badge — implemented when clientes exist
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Text(
                text = result.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(result.name, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            Text(result.mercadoName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.extendedColors.text2)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun BusquedaScreenDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        BusquedaContent(BusquedaUiState(), onBack = {}, onQueryChange = {}, onClearQuery = {}, onResultClick = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun BusquedaScreenPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        BusquedaContent(BusquedaUiState(), onBack = {}, onQueryChange = {}, onClearQuery = {}, onResultClick = {})
    }
}
