package com.restrusher.ecomercecarlosv.ui.screen.perfil

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
internal fun HelpTopicCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ext.border, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(ext.surface2)
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = ext.text3,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (expanded) 180f else 0f),
            )
        }
        if (expanded) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = ext.text3,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
private fun HelpTopicCardPreview() {
    EcomerceCarlosVTheme(darkTheme = false) {
        HelpTopicCard(
            title = "Mercados",
            body = "Un mercado agrupa a los clientes de una misma zona o punto de venta.",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun HelpTopicCardDarkPreview() {
    EcomerceCarlosVTheme(darkTheme = true) {
        HelpTopicCard(
            title = "Mercados",
            body = "Un mercado agrupa a los clientes de una misma zona o punto de venta.",
            modifier = Modifier.padding(16.dp),
        )
    }
}
