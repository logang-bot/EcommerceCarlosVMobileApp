package com.restrusher.ecomercecarlosv.ui.screen.mercado

import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.ui.common.SyncIconState

data class MercadoStat(
    val activeClientCount: Int = 0,
    val hasWarning: Boolean = false,
    val hasCritical: Boolean = false,
)

data class MercadosUiState(
    val mercados: List<Mercado> = emptyList(),
    val stats: Map<String, MercadoStat> = emptyMap(),
    val isLoading: Boolean = false,
    val currentUserInitials: String = "",
    val currentUserPhotoUrl: String? = null,
    val selectedMercadoId: String? = null,
    val canWrite: Boolean = true,
    val syncIconState: SyncIconState = SyncIconState.SYNCED,
    val isRefreshing: Boolean = false,
)
