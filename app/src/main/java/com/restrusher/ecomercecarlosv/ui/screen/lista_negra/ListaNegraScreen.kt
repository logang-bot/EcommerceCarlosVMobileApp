package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.ClienteAvatar
import com.restrusher.ecomercecarlosv.ui.common.EmptyState
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ListaNegraScreen(
    navController: NavController,
    viewModel: ListaNegraViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ListaNegraContent(
        state = state,
        onBack = { navController.popBackStack() },
        onQueryChange = viewModel::onQueryChange,
    )
}

@Composable
private fun ListaNegraContent(
    state: ListaNegraUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    var searchActive by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                PedidosTopBar(
                    title = stringResource(R.string.lista_negra_title),
                    subtitle = stringResource(R.string.lista_negra_subtitle),
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = { searchActive = !searchActive }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = if (searchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
                AnimatedVisibility(visible = searchActive) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.lista_negra_search_placeholder), color = ext.text3) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ext.text2, modifier = Modifier.size(20.dp)) },
                        trailingIcon = if (state.query.isNotEmpty()) {
                            { IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Close, contentDescription = null) } }
                        } else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = ext.border2,
                            unfocusedBorderColor = ext.border,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

            state.items.isEmpty() -> EmptyState(
                modifier = Modifier.padding(innerPadding),
                icon = Icons.Default.Block,
                title = stringResource(R.string.lista_negra_empty_title),
                subtitle = stringResource(R.string.lista_negra_empty_subtitle),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 4.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                item { ListaNegraBanner(count = state.items.size, totalBalance = state.totalBalance) }
                if (state.filtered.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = stringResource(R.string.lista_negra_no_results_title),
                            subtitle = stringResource(R.string.lista_negra_no_results_subtitle),
                            compact = true,
                            modifier = Modifier.height(280.dp),
                        )
                    }
                } else {
                    items(state.filtered, key = { it.clienteId }) { model ->
                        BlacklistRow(model = model)
                        HorizontalDivider(modifier = Modifier.padding(start = 79.dp), color = ext.border)
                    }
                }
            }
        }
    }
}

@Composable
private fun ListaNegraBanner(count: Int, totalBalance: Double) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(ext.redTint)
            .border(1.dp, ext.redText.copy(alpha = 0.20f), RoundedCornerShape(13.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Icon(Icons.Default.Block, contentDescription = null, tint = ext.redText, modifier = Modifier.size(20.dp).padding(top = 1.dp))
        Column {
            Text(
                text = stringResource(R.string.lista_negra_banner_count, count),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = ext.redText,
            )
            Text(
                text = stringResource(R.string.lista_negra_banner_total, formatBalanceLn(totalBalance)),
                style = MaterialTheme.typography.bodySmall,
                color = ext.text2,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun BlacklistRow(model: BlacklistUiModel) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        ClienteAvatar(name = model.name, photoUrl = model.photoUrl, size = 46.dp)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = model.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.5.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatBalanceLn(model.blacklistBalance),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ext.redText,
                )
            }
            Text(model.mercadoName, style = MaterialTheme.typography.bodySmall, color = ext.text2, modifier = Modifier.padding(top = 2.dp))
            if (!model.blacklistReason.isNullOrBlank()) {
                Row(modifier = Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = ext.redText, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                    Text(
                        text = model.blacklistReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.text3,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            model.blacklistedAt?.let { at ->
                Text(
                    text = stringResource(R.string.lista_negra_added_on, formatDateLn(at)),
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.text4,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }
    }
}

private fun formatBalanceLn(amount: Double): String {
    val formatted = String.format("%.2f", amount).replace('.', ',')
    return "Bs. $formatted"
}

private fun formatDateLn(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale("es")).format(Date(millis))
