package com.restrusher.ecomercecarlosv.ui.screen.cliente

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuitarListaNegraSheet(
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
    onMarkAllPaid: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        QuitarListaNegraSheetContent(onRestore = onRestore, onMarkAllPaid = onMarkAllPaid)
    }
}

@Composable
private fun QuitarListaNegraSheetContent(
    onRestore: () -> Unit,
    onMarkAllPaid: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
    ) {
        Text(
            stringResource(R.string.quitar_lista_negra_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.quitar_lista_negra_sheet_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = ext.text2,
        )
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ext.surface2)
                .border(1.dp, ext.border, RoundedCornerShape(16.dp)),
        ) {
            SheetOptionRow(
                icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) },
                title = stringResource(R.string.quitar_lista_negra_restore_title),
                description = stringResource(R.string.quitar_lista_negra_restore_desc),
                onClick = onRestore,
            )
            HorizontalDivider(color = ext.border)
            SheetOptionRow(
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ext.greenText, modifier = Modifier.size(22.dp)) },
                title = stringResource(R.string.quitar_lista_negra_mark_paid_title),
                description = stringResource(R.string.quitar_lista_negra_mark_paid_desc),
                onClick = onMarkAllPaid,
            )
        }
    }
}

@Composable
private fun SheetOptionRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        icon()
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.5.sp)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = ext.text2,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "QuitarListaNegraSheet — dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun QuitarListaNegraSheetPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Surface {
            QuitarListaNegraSheetContent(onRestore = {}, onMarkAllPaid = {})
        }
    }
}
