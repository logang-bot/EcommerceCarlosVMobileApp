package com.restrusher.ecomercecarlosv.ui.screen.reporte

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun ReportDocPreview(modifier: Modifier = Modifier) {
    val ex = MaterialTheme.extendedColors
    val headerGreen = ex.greenText
    val accentAmber = ex.amberText

    Column(
        modifier = modifier.size(width = 150.dp, height = 197.dp)
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(9.dp))
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White)
            .border(0.5.dp, ex.border.copy(alpha = 0.3f), RoundedCornerShape(9.dp)),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(24.dp).background(headerGreen),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(modifier = Modifier.size(10.dp).background(Color.White.copy(alpha = 0.4f), CircleShape))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.width(50.dp).height(3.dp).background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(99.dp)))
                    Box(modifier = Modifier.width(32.dp).height(2.dp).background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(99.dp)))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            MiniStatBox(bg = ex.greenTint, valueColor = headerGreen, modifier = Modifier.weight(1f))
            MiniStatBox(bg = ex.accentTint, valueColor = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            MiniStatBox(bg = ex.amberTint, valueColor = accentAmber, modifier = Modifier.weight(1f))
        }
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp),
        ) {
            Column {
                Box(modifier = Modifier.width(60.dp).height(3.dp).background(ex.surface3, RoundedCornerShape(99.dp)))
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ex.border))
                Spacer(Modifier.height(4.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 7.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(6) { i -> MiniTableRow(accentRow = i % 2 == 0) }
        }
    }
}

@Composable
private fun MiniStatBox(bg: Color, valueColor: Color, modifier: Modifier = Modifier) {
    val ex = MaterialTheme.extendedColors
    Column(
        modifier = modifier.background(bg, RoundedCornerShape(5.dp)).padding(horizontal = 5.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(modifier = Modifier.width(18.dp).height(5.dp).background(valueColor.copy(alpha = 0.85f), RoundedCornerShape(99.dp)))
        Box(modifier = Modifier.width(28.dp).height(2.5.dp).background(ex.text4, RoundedCornerShape(99.dp)))
    }
}

@Composable
private fun MiniTableRow(accentRow: Boolean) {
    val ex = MaterialTheme.extendedColors
    val rowBg = if (accentRow) ex.surface2 else Color.Transparent
    Row(
        modifier = Modifier.fillMaxWidth().background(rowBg, RoundedCornerShape(3.dp)).padding(horizontal = 3.dp, vertical = 2.5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1.2f).height(2.5.dp).background(ex.text4, RoundedCornerShape(99.dp)))
        Box(modifier = Modifier.weight(0.7f).height(2.5.dp).background(ex.text4.copy(alpha = 0.5f), RoundedCornerShape(99.dp)))
        Box(modifier = Modifier.weight(0.8f).height(2.5.dp).background(ex.text4.copy(alpha = 0.5f), RoundedCornerShape(99.dp)))
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun ReportDocPreviewLight() {
    EcomerceCarlosVTheme {
        Box(modifier = Modifier.padding(24.dp)) { ReportDocPreview() }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ReportDocPreviewDark() {
    EcomerceCarlosVTheme(darkTheme = true) {
        Box(modifier = Modifier.padding(24.dp)) { ReportDocPreview() }
    }
}
