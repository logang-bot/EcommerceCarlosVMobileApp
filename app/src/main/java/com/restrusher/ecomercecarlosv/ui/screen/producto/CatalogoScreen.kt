package com.restrusher.ecomercecarlosv.ui.screen.producto

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Producto
import com.restrusher.ecomercecarlosv.presentation.screens.CreateProductoRoute
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.LoadingOverlay
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.common.PhotoThumbnail
import com.restrusher.ecomercecarlosv.ui.screen.home.AppBottomNavBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun CatalogoScreen(
    navController: NavController,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    viewModel: CatalogoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogoContent(
        state = state,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        onProductoClick = { navController.navigate(CreateProductoRoute(productId = it)) },
        onCreateClick = { navController.navigate(CreateProductoRoute()) },
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onRefresh = viewModel::onRefresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogoContent(
    state: CatalogoUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onProductoClick: (String) -> Unit,
    onCreateClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit = {},
) {
    val ext = MaterialTheme.extendedColors
    val count = state.productos.size

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.productos_title),
                subtitle = stringResource(R.string.catalogo_subtitle, count),
                large = true,
                actions = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ext.text2,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(24.dp),
                    )
                },
            )
        },
        bottomBar = { AppBottomNavBar(selectedTab = selectedTab, onTabSelected = onTabSelected) },
        floatingActionButton = {
            if (state.canWrite) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.productos_fab)) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = onCreateClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LoadingOverlay(isLoading = state.isLoading) {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                if (state.productos.isEmpty() && !state.isLoading) {
                    EmptyState(
                        modifier = Modifier.padding(innerPadding),
                        icon = Icons.Default.Sell,
                        title = stringResource(R.string.catalogo_empty_title),
                        subtitle = stringResource(R.string.catalogo_empty_subtitle),
                        hint = stringResource(R.string.catalogo_empty_hint),
                        onActionClick = onCreateClick,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = innerPadding.calculateTopPadding(),
                            bottom = innerPadding.calculateBottomPadding() + 16.dp,
                        ),
                    ) {
                        item {
                            SearchBar(
                                query = state.searchQuery,
                                onQueryChange = onSearchQueryChange,
                            )
                        }
                        items(state.productos, key = { it.id }) { producto ->
                            ProductoRow(
                                producto = producto,
                                onClick = if (state.canWrite) ({ onProductoClick(producto.id) }) else null,
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 83.dp),
                                color = ext.border,
                            )
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
        placeholder = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = ext.text3,
                    modifier = Modifier
                        .size(19.dp)
                        .padding(end = 4.dp),
                )
                Text(
                    text = stringResource(R.string.catalogo_search_placeholder),
                    color = ext.text3,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(13.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = ext.surface2,
            focusedContainerColor = ext.surface2,
            unfocusedBorderColor = ext.border,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun ProductoRow(producto: Producto, onClick: (() -> Unit)?) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductoTile(photoUrl = producto.photoUrl)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 13.dp),
        ) {
            Text(
                text = producto.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            if (!producto.description.isNullOrBlank()) {
                Text(
                    text = producto.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text2,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                )
            }
        }
        Text(
            text = "Bs. %.2f".format(producto.price),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ext.text4,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(17.dp),
            )
        }
    }
}

@Composable
private fun ProductoTile(modifier: Modifier = Modifier, photoUrl: String? = null) {
    val ext = MaterialTheme.extendedColors
    PhotoThumbnail(
        photoUrl = photoUrl,
        modifier = modifier
            .size(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ext.surface2)
            .border(1.dp, ext.border, RoundedCornerShape(12.dp)),
        fallback = {
            Icon(
                imageVector = Icons.Default.Sell,
                contentDescription = null,
                tint = ext.text2,
                modifier = Modifier.size(24.dp),
            )
        },
    )
}

private val previewProductos = listOf(
    Producto(id = "1", name = "Arroz premium", description = "Arroz de grano largo", price = 12.50, createdAt = 0),
    Producto(id = "2", name = "Aceite girasol 1L", price = 18.00, createdAt = 0),
    Producto(id = "3", name = "Azúcar blanca", description = "Bolsa 1kg", price = 8.50, createdAt = 0),
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun CatalogoContentPreview() {
    EcomerceCarlosVTheme {
        CatalogoContent(
            state = CatalogoUiState(productos = previewProductos),
            selectedTab = 1,
            onTabSelected = {},
            onProductoClick = {},
            onCreateClick = {},
            onSearchQueryChange = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CatalogoContentDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        CatalogoContent(
            state = CatalogoUiState(productos = previewProductos),
            selectedTab = 1,
            onTabSelected = {},
            onProductoClick = {},
            onCreateClick = {},
            onSearchQueryChange = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ProductoRowPreview() {
    EcomerceCarlosVTheme {
        ProductoRow(producto = previewProductos.first(), onClick = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ProductoRowDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        ProductoRow(producto = previewProductos.first(), onClick = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun SearchBarPreview() {
    EcomerceCarlosVTheme {
        SearchBar(query = "Arroz", onQueryChange = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SearchBarDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        SearchBar(query = "", onQueryChange = {})
    }
}
