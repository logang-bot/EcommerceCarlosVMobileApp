package com.restrusher.ecomercecarlosv.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

@Composable
fun PayChip(
    modifier: Modifier = Modifier,
    status: PedidoStatus,
) {
    val ext = MaterialTheme.extendedColors
    val (bg, textColor, label) = when (status) {
        PedidoStatus.PAID -> Triple(ext.greenTint, ext.greenText, stringResource(R.string.pedidos_estado_pagado))
        PedidoStatus.PARTIAL -> Triple(ext.accentSoft, MaterialTheme.colorScheme.primary, stringResource(R.string.pedidos_estado_parcial))
        PedidoStatus.PENDING -> Triple(ext.amberTint, ext.amberText, stringResource(R.string.pedidos_estado_pendiente))
    }
    Text(
        text = label,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}
