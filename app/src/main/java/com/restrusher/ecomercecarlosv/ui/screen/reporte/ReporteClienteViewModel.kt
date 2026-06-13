package com.restrusher.ecomercecarlosv.ui.screen.reporte

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import com.restrusher.ecomercecarlosv.presentation.screens.ReporteClienteRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

private const val WARNING_THRESHOLD = 50

@HiltViewModel
class ReporteClienteViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val mercadoRepository: MercadoRepository,
    private val pedidoRepository: PedidoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val clienteId: String = savedStateHandle.toRoute<ReporteClienteRoute>().clienteId
    private val _mercadoName = MutableStateFlow("")
    private val _preset = MutableStateFlow(ReporteClientePreset.HOY)
    private val _customFrom = MutableStateFlow<Long?>(null)
    private val _customTo = MutableStateFlow<Long?>(null)

    private data class Input(
        val preset: ReporteClientePreset,
        val customFrom: Long?,
        val customTo: Long?,
        val mercadoName: String,
    )

    private val _input = combine(_preset, _customFrom, _customTo, _mercadoName) { p, cf, ct, m ->
        Input(p, cf, ct, m)
    }

    val uiState = combine(
        clienteRepository.getByIdFlow(clienteId),
        pedidoRepository.getByClienteWithLines(clienteId),
        _input,
    ) { cliente, pedidos, input ->
        if (cliente == null) return@combine ReporteClienteUiState(isLoading = false)

        val (fromMs, toMs) = computeRange(input.preset, input.customFrom, input.customTo)
        val inRange = if (fromMs > 0L && toMs > 0L) {
            pedidos.filter { it.createdAt in fromMs..toMs }
        } else {
            emptyList()
        }

        ReporteClienteUiState(
            clienteName = cliente.name,
            clienteDesc = cliente.description,
            clientePhone = cliente.phones.firstOrNull().orEmpty(),
            mercadoName = input.mercadoName,
            preset = input.preset,
            fromMs = fromMs,
            toMs = toMs,
            customFrom = input.customFrom,
            customTo = input.customTo,
            pedidosInRange = inRange,
            pedidosCount = inRange.size,
            montoTotal = inRange.sumOf { it.total },
            showWarning = inRange.size > WARNING_THRESHOLD,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReporteClienteUiState(),
    )

    init {
        viewModelScope.launch {
            clienteRepository.getByIdFlow(clienteId).first { it != null }?.let { cliente ->
                _mercadoName.value = mercadoRepository.getById(cliente.mercadoId)?.name.orEmpty()
            }
        }
    }

    fun setPreset(preset: ReporteClientePreset) { _preset.value = preset }
    fun setCustomFrom(utcMs: Long) { _customFrom.value = utcMs }
    fun setCustomTo(utcMs: Long) { _customTo.value = utcMs }

    private fun computeRange(
        preset: ReporteClientePreset,
        customFrom: Long?,
        customTo: Long?,
    ): Pair<Long, Long> {
        return when (preset) {
            ReporteClientePreset.HOY -> {
                val from = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val to = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                from to to
            }
            ReporteClientePreset.SEMANA -> {
                val from = Calendar.getInstance().apply {
                    val dow = get(Calendar.DAY_OF_WEEK)
                    val daysFromMon = (dow - Calendar.MONDAY + 7) % 7
                    add(Calendar.DAY_OF_YEAR, -daysFromMon)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val to = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                from to to
            }
            ReporteClientePreset.MES -> {
                val from = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val to = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                from to to
            }
            ReporteClientePreset.PERSONALIZADO -> {
                val from = customFrom?.let { utcMs -> localStartOfDay(utcMs) } ?: 0L
                val to = customTo?.let { utcMs -> localEndOfDay(utcMs) } ?: 0L
                from to to
            }
        }
    }

    private fun localStartOfDay(utcMs: Long): Long {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMs }
        return Calendar.getInstance().apply {
            set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun localEndOfDay(utcMs: Long): Long {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMs }
        return Calendar.getInstance().apply {
            set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
