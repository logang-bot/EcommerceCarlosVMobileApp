package com.restrusher.ecomercecarlosv.ui.screen.perfil

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun UmbralesScreen(
    navController: NavController,
    viewModel: UmbralesViewModel = hiltViewModel(),
) {
    val montoText by viewModel.montoText.collectAsStateWithLifecycle()
    val diasText by viewModel.diasText.collectAsStateWithLifecycle()
    val canSave by viewModel.canSave.collectAsStateWithLifecycle()

    UmbralesContent(
        montoText = montoText,
        diasText = diasText,
        canSave = canSave,
        onMontoChange = viewModel::onMontoChange,
        onDiasChange = viewModel::onDiasChange,
        onBack = { navController.popBackStack() },
        onSave = {
            viewModel.save()
            navController.popBackStack()
        },
    )
}

@Composable
private fun UmbralesContent(
    montoText: String,
    diasText: String,
    canSave: Boolean,
    onMontoChange: (String) -> Unit,
    onDiasChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.umbrales_title),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                TextButton(
                    onClick = onSave,
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(if (canSave) MaterialTheme.colorScheme.primary else ext.surface3),
                ) {
                    Text(
                        stringResource(R.string.umbrales_guardar),
                        color = if (canSave) Color.White else ext.text3,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.umbrales_header),
                style = MaterialTheme.typography.bodyMedium,
                color = ext.text2,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                StatusRuleCard(
                    dotColor = ext.green,
                    ringColor = ext.greenTint,
                    labelColor = ext.greenText,
                    label = stringResource(R.string.status_al_dia),
                    description = stringResource(R.string.umbrales_al_dia_desc),
                    automatic = true,
                    focus = false,
                )
                StatusRuleCard(
                    dotColor = ext.amber,
                    ringColor = ext.amberTint,
                    labelColor = ext.amberText,
                    label = stringResource(R.string.status_advertencia),
                    description = stringResource(R.string.umbrales_advertencia_desc),
                    automatic = true,
                    focus = false,
                )
                StatusRuleCard(
                    dotColor = MaterialTheme.colorScheme.error,
                    ringColor = ext.redTint,
                    labelColor = ext.redText,
                    label = stringResource(R.string.status_critico),
                    description = stringResource(R.string.umbrales_critico_desc),
                    automatic = false,
                    focus = true,
                ) {
                    Column(
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        CritTrigger(
                            icon = Icons.Default.Payments,
                            title = stringResource(R.string.umbrales_monto_maximo),
                            hint = stringResource(R.string.umbrales_monto_hint),
                        ) {
                            TriggerInput(
                                value = montoText,
                                onValueChange = onMontoChange,
                                prefix = stringResource(R.string.umbrales_monto_prefix),
                                keyboardType = KeyboardType.Decimal,
                            )
                        }
                        OrDivider()
                        CritTrigger(
                            icon = Icons.Default.CalendarToday,
                            title = stringResource(R.string.umbrales_dias_label),
                            hint = stringResource(R.string.umbrales_dias_hint),
                        ) {
                            TriggerInput(
                                value = diasText,
                                onValueChange = onDiasChange,
                                prefix = stringResource(R.string.umbrales_dias_prefix),
                                suffix = stringResource(R.string.umbrales_dias_suffix),
                                keyboardType = KeyboardType.Number,
                            )
                        }
                    }
                }
                InfoNote(text = stringResource(R.string.umbrales_info_note))
            }
        }
    }
}

@Composable
private fun StatusRuleCard(
    dotColor: Color,
    ringColor: Color,
    labelColor: Color,
    label: String,
    description: String,
    automatic: Boolean,
    focus: Boolean,
    content: @Composable (() -> Unit)? = null,
) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(16.dp)
    val borderMod = if (focus) Modifier.border(1.5.dp, dotColor, shape) else Modifier.border(1.dp, ext.border, shape)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ext.surface2)
            .then(borderMod),
    ) {
        Row(
            modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 13.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatusDot(color = dotColor, ringColor = ringColor)
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = labelColor,
                letterSpacing = (-0.2).sp,
            )
            Spacer(Modifier.weight(1f))
            if (automatic) {
                AutomaticBadge()
            }
        }
        Text(
            text = description,
            fontSize = 13.sp,
            color = ext.text2,
            lineHeight = 19.5.sp,
            modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = if (content != null) 2.dp else 14.dp),
        )
        content?.invoke()
    }
}

@Composable
private fun StatusDot(color: Color, ringColor: Color) {
    Box(
        modifier = Modifier
            .size(19.dp)
            .clip(CircleShape)
            .background(ringColor),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
private fun AutomaticBadge() {
    val ext = MaterialTheme.extendedColors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ext.surface3)
            .border(1.dp, ext.border, RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.umbrales_automatico).uppercase(),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = ext.text3,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun CritTrigger(
    icon: ImageVector,
    title: String,
    hint: String,
    content: @Composable () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icon, contentDescription = null, tint = ext.redText, modifier = Modifier.size(16.dp))
            Text(title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        }
        content()
        Text(hint, fontSize = 12.sp, color = ext.text3, lineHeight = 16.8.sp)
    }
}

@Composable
private fun TriggerInput(
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal,
) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(13.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, MaterialTheme.colorScheme.error, shape)
            .padding(start = 15.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(prefix, fontSize = 14.5.sp, color = ext.text2, fontWeight = FontWeight.Medium)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
        )
        if (suffix != null) {
            Text(suffix, fontSize = 14.5.sp, color = ext.text2, fontWeight = FontWeight.Medium)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(ext.surface3)
                .border(1.dp, ext.border, RoundedCornerShape(9.dp)),
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = ext.text2, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable
private fun OrDivider() {
    val ext = MaterialTheme.extendedColors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = ext.border2)
        Text(
            text = stringResource(R.string.umbrales_or_connector),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ext.redText,
            letterSpacing = 1.5.sp,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = ext.border2)
    }
}

@Composable
private fun InfoNote(text: String) {
    val ext = MaterialTheme.extendedColors
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ext.surface2)
            .border(1.dp, ext.border, shape)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = ext.text3,
            modifier = Modifier.size(18.dp).padding(top = 1.dp),
        )
        Text(text, fontSize = 12.5.sp, color = ext.text2, lineHeight = 18.75.sp)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun UmbralesDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        UmbralesContent(
            montoText = "200",
            diasText = "30",
            canSave = true,
            onMontoChange = {},
            onDiasChange = {},
            onBack = {},
            onSave = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun UmbralesLightPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        UmbralesContent(
            montoText = "200",
            diasText = "30",
            canSave = true,
            onMontoChange = {},
            onDiasChange = {},
            onBack = {},
            onSave = {},
        )
    }
}
