package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun AgregarListaNegraScreen(
    navController: NavController,
    viewModel: AgregarListaNegraViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AgregarListaNegraContent(
        state = state,
        onBack = { navController.popBackStack() },
        onAmountChange = viewModel::onAmountChange,
        onReasonChange = viewModel::onReasonChange,
        onTotalModeChange = viewModel::onTotalModeChange,
        onConfirm = { viewModel.onConfirm { navController.popBackStack() } },
    )
}

@Composable
private fun AgregarListaNegraContent(
    state: AgregarListaNegraUiState,
    onBack: () -> Unit,
    onAmountChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onTotalModeChange: (TotalMode) -> Unit,
    onConfirm: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.agregar_lista_negra_title),
                subtitle = state.clienteName.ifBlank { null },
                onBack = onBack,
            )
        },
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Button(
                    onClick = onConfirm,
                    enabled = state.canConfirm && !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ext.redTint,
                        contentColor = ext.redText,
                        disabledContainerColor = ext.surface2,
                        disabledContentColor = ext.text3,
                    ),
                    border = BorderStroke(1.dp, ext.redText.copy(alpha = 0.25f)),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = ext.redText, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.agregar_lista_negra_confirm), fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            SectionLabel(stringResource(R.string.agregar_lista_negra_pending_section))
            PendingPedidosList(pedidos = state.pendingPedidos)

            Spacer(Modifier.height(22.dp))
            SectionLabel(stringResource(R.string.agregar_lista_negra_total_section))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TotalModeCard(
                    selected = state.totalMode == TotalMode.AUTO,
                    onClick = { onTotalModeChange(TotalMode.AUTO) },
                    title = stringResource(R.string.agregar_lista_negra_auto_title),
                    subtitle = stringResource(R.string.agregar_lista_negra_auto_subtitle),
                    trailingContent = if (state.totalMode == TotalMode.AUTO) {
                        {
                            Text(
                                text = "Bs. ${"%.2f".format(state.autoAmount)}",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else null,
                )
                Column {
                    TotalModeCard(
                        selected = state.totalMode == TotalMode.MANUAL,
                        onClick = { onTotalModeChange(TotalMode.MANUAL) },
                        title = stringResource(R.string.agregar_lista_negra_manual_title),
                        subtitle = stringResource(R.string.agregar_lista_negra_manual_subtitle),
                    )
                    if (state.totalMode == TotalMode.MANUAL) {
                        OutlinedTextField(
                            value = state.manualAmount,
                            onValueChange = onAmountChange,
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            label = { Text(stringResource(R.string.agregar_lista_negra_amount_label)) },
                            placeholder = { Text("0,00", color = ext.text3, fontFamily = FontFamily.Monospace) },
                            prefix = { Text("Bs. ", fontFamily = FontFamily.Monospace, color = ext.text2) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = ext.border2,
                                unfocusedBorderColor = ext.border,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            SectionLabel(stringResource(R.string.agregar_lista_negra_reason_section))
            OutlinedTextField(
                value = state.reason,
                onValueChange = onReasonChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 120.dp),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text(stringResource(R.string.agregar_lista_negra_reason_placeholder), color = ext.text3) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = ext.border2,
                    unfocusedBorderColor = ext.border,
                ),
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.extendedColors.text3,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 10.dp),
    )
}

private val previewPedidos = listOf(
    Pedido(id = "o1", clienteId = "c1", status = PedidoStatus.PENDING, total = 47.50, paid = 0.0, createdAt = 1748822400000L),
    Pedido(id = "o2", clienteId = "c1", status = PedidoStatus.PARTIAL, total = 112.00, paid = 50.0, createdAt = 1748390400000L),
    Pedido(id = "o3", clienteId = "c1", status = PedidoStatus.PENDING, total = 60.00, paid = 0.0, createdAt = 1747958400000L, isSaldoExtra = true),
)

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun AgregarListaNegraContentPreview() {
    EcomerceCarlosVTheme {
        AgregarListaNegraContent(
            state = AgregarListaNegraUiState(
                clienteName = "Ana Rodríguez",
                pendingPedidos = previewPedidos,
                totalMode = TotalMode.AUTO,
                isLoading = false,
            ),
            onBack = {},
            onAmountChange = {},
            onReasonChange = {},
            onTotalModeChange = {},
            onConfirm = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AgregarListaNegraContentDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        AgregarListaNegraContent(
            state = AgregarListaNegraUiState(
                clienteName = "Ana Rodríguez",
                pendingPedidos = previewPedidos,
                totalMode = TotalMode.MANUAL,
                manualAmount = "219.50",
                reason = "Tres pedidos vencidos sin respuesta",
                isLoading = false,
            ),
            onBack = {},
            onAmountChange = {},
            onReasonChange = {},
            onTotalModeChange = {},
            onConfirm = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun AgregarListaNegraEmptyPreview() {
    EcomerceCarlosVTheme {
        AgregarListaNegraContent(
            state = AgregarListaNegraUiState(
                clienteName = "Carlos Pérez",
                pendingPedidos = emptyList(),
                totalMode = TotalMode.MANUAL,
                isLoading = false,
            ),
            onBack = {},
            onAmountChange = {},
            onReasonChange = {},
            onTotalModeChange = {},
            onConfirm = {},
        )
    }
}
