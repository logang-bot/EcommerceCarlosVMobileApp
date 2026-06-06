package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
private fun SaldoExtraFieldLabel(text: String, required: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.extendedColors.text2)
        if (required) {
            Text(" *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
internal fun SaldoExtraCategoryField() {
    val ext = MaterialTheme.extendedColors
    Column {
        SaldoExtraFieldLabel(stringResource(R.string.saldo_extra_categoria_label))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, ext.border, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(ext.amberTint, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Tag, contentDescription = null, tint = ext.amberText, modifier = Modifier.size(17.dp))
            }
            Text(
                text = stringResource(R.string.saldo_extra_categoria),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.5.sp,
                modifier = Modifier.weight(1f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = ext.text3, modifier = Modifier.size(14.dp))
                Text(stringResource(R.string.saldo_extra_categoria_fijo), fontSize = 11.5.sp, color = ext.text3)
            }
        }
    }
}

@Composable
internal fun SaldoExtraAmountHero(value: String, isError: Boolean, onValueChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.saldo_extra_monto),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ext.text2,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(0.75f),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Bs.", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = ext.text2)
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = "0,00",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        color = ext.text3,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(2.dp)
                .background(if (isError) MaterialTheme.colorScheme.error else accent, RoundedCornerShape(2.dp)),
        )
        if (isError) {
            Text(
                text = stringResource(R.string.saldo_extra_monto_error),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
internal fun SaldoExtraDescriptionField(value: String, isError: Boolean, onValueChange: (String) -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column {
        SaldoExtraFieldLabel(stringResource(R.string.saldo_extra_descripcion), required = true)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 88.dp),
            shape = RoundedCornerShape(14.dp),
            placeholder = { Text(stringResource(R.string.saldo_extra_descripcion_placeholder), color = ext.text3) },
            isError = isError,
            supportingText = if (isError) ({ Text(stringResource(R.string.saldo_extra_descripcion_error)) }) else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ext.surface2,
                unfocusedContainerColor = ext.surface2,
                focusedBorderColor = ext.border2,
                unfocusedBorderColor = if (value.isEmpty()) ext.border else ext.border2,
            ),
        )
    }
}

@Composable
internal fun SaldoExtraDateField(label: String, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Column {
        SaldoExtraFieldLabel(stringResource(R.string.saldo_extra_fecha))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ext.surface2)
                .border(1.dp, ext.border2, RoundedCornerShape(14.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontWeight = FontWeight.Medium, fontSize = 15.5.sp)
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun SaldoExtraFieldsPreview() {
    EcomerceCarlosVTheme {
        Surface {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                SaldoExtraCategoryField()
                SaldoExtraAmountHero("60,00", false, {})
                SaldoExtraDescriptionField("Envases retornables no devueltos", false, {})
                SaldoExtraDateField("3 de junio de 2026", {})
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SaldoExtraFieldsDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                SaldoExtraCategoryField()
                SaldoExtraAmountHero("", true, {})
                SaldoExtraDescriptionField("", true, {})
                SaldoExtraDateField("3 de junio de 2026", {})
            }
        }
    }
}
