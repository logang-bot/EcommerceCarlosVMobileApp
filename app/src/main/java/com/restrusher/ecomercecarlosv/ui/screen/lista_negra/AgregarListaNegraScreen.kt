package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
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
        onConfirm = { viewModel.onConfirm { navController.popBackStack() } },
    )
}

@Composable
private fun AgregarListaNegraContent(
    state: AgregarListaNegraUiState,
    onBack: () -> Unit,
    onAmountChange: (String) -> Unit,
    onReasonChange: (String) -> Unit,
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, ext.redText.copy(alpha = 0.25f)),
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
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Pedidos pendientes (Phase 4 placeholder) ──────────────────
            Spacer(Modifier.height(12.dp))
            SectionLabel(stringResource(R.string.agregar_lista_negra_pending_section))
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ext.surface2)
                    .border(1.dp, ext.border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, tint = ext.text3, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.agregar_lista_negra_pending_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ext.text3,
                )
            }

            // ── Total adeudado ────────────────────────────────────────────
            Spacer(Modifier.height(22.dp))
            SectionLabel(stringResource(R.string.agregar_lista_negra_total_section))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // "Calcular automáticamente" — Phase 4 placeholder, shown as disabled
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ext.surface2)
                        .border(1.dp, ext.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(20.dp).clip(CircleShape).border(1.6.dp, ext.border3, CircleShape))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.agregar_lista_negra_auto_title), fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = ext.text3)
                        Text(stringResource(R.string.agregar_lista_negra_auto_unavailable), fontSize = 12.sp, color = ext.text3, modifier = Modifier.padding(top = 2.dp))
                    }
                }
                // "Ingresar manualmente" — always selected
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ext.accentTint)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.agregar_lista_negra_manual_title), fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(stringResource(R.string.agregar_lista_negra_manual_subtitle), fontSize = 12.sp, color = ext.text2, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
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

            // ── Motivo del veto ───────────────────────────────────────────
            Spacer(Modifier.height(22.dp))
            SectionLabel(stringResource(R.string.agregar_lista_negra_reason_section))
            OutlinedTextField(
                value = state.reason,
                onValueChange = onReasonChange,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
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
        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp),
    )
}
