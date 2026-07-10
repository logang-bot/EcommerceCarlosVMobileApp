package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.ThemeMode
import com.restrusher.ecomercecarlosv.ui.common.SettingRow
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun AjustesSection(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    showUmbrales: Boolean,
    umbralesSummary: String?,
    onUmbralesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors

    Column(modifier = modifier) {
        SectionHeader(stringResource(R.string.perfil_section_ajustes))
        AppearanceCard(
            themeMode = themeMode,
            onSelect = onThemeChange,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (showUmbrales) {
            Spacer(Modifier.height(10.dp))
            SettingRow(
                icon = Icons.Default.BarChart,
                title = stringResource(R.string.perfil_umbrales_title),
                subtitle = umbralesSummary,
                onClick = onUmbralesClick,
                trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp)) },
            )
        }
    }
}

@Composable
private fun AppearanceCard(
    themeMode: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ext.border, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(ext.surface2)
            .padding(bottom = 12.dp),
    ) {
        // Header row
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 13.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(ext.surface3),
            ) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = ext.text2,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.perfil_apariencia_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.perfil_apariencia_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.text3,
                )
            }
        }

        // Segment selector
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(ext.surface3)
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ThemeOption(
                icon = Icons.Default.WbSunny,
                label = stringResource(R.string.perfil_theme_light),
                selected = themeMode == ThemeMode.LIGHT,
                onClick = { onSelect(ThemeMode.LIGHT) },
                modifier = Modifier.weight(1f),
            )
            ThemeOption(
                icon = Icons.Default.DarkMode,
                label = stringResource(R.string.perfil_theme_dark),
                selected = themeMode == ThemeMode.DARK,
                onClick = { onSelect(ThemeMode.DARK) },
                modifier = Modifier.weight(1f),
            )
            ThemeOption(
                icon = Icons.Default.SettingsBrightness,
                label = stringResource(R.string.perfil_theme_system),
                selected = themeMode == ThemeMode.SYSTEM,
                onClick = { onSelect(ThemeMode.SYSTEM) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val onAccent = MaterialTheme.colorScheme.onPrimary
    val ext = MaterialTheme.extendedColors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) accent else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) onAccent else ext.text2,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) onAccent else ext.text2,
            fontSize = 12.5.sp,
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun AjustesSectionPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        AjustesSection(
            themeMode = ThemeMode.SYSTEM,
            onThemeChange = {},
            showUmbrales = false,
            umbralesSummary = null,
            onUmbralesClick = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AjustesSectionSuperusuarioDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        AjustesSection(
            themeMode = ThemeMode.DARK,
            onThemeChange = {},
            showUmbrales = true,
            umbralesSummary = "Bs. 500 / Bs. 2.000",
            onUmbralesClick = {},
        )
    }
}
