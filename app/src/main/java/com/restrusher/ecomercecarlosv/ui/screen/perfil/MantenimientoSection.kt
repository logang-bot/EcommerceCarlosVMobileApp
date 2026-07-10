package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.SettingRow
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun MantenimientoSection(
    onMantenimientoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors

    Column(modifier = modifier) {
        SectionHeader(stringResource(R.string.perfil_section_mantenimiento))
        SettingRow(
            icon = Icons.Default.Delete,
            title = stringResource(R.string.perfil_mantenimiento_title),
            subtitle = stringResource(R.string.perfil_mantenimiento_subtitle),
            iconColor = ext.redText,
            iconBg = ext.redTint,
            onClick = onMantenimientoClick,
            trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ext.text4, modifier = Modifier.size(17.dp)) },
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun MantenimientoSectionPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        MantenimientoSection(onMantenimientoClick = {})
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun MantenimientoSectionDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        MantenimientoSection(onMantenimientoClick = {})
    }
}
