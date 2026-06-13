package com.restrusher.ecomercecarlosv.ui.screen.reporte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.ecomercecarlosv.domain.model.Cliente
import com.restrusher.ecomercecarlosv.domain.model.Mercado
import com.restrusher.ecomercecarlosv.domain.model.Pedido
import com.restrusher.ecomercecarlosv.domain.repository.ClienteRepository
import com.restrusher.ecomercecarlosv.domain.repository.MercadoRepository
import com.restrusher.ecomercecarlosv.domain.repository.PedidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReporteViewModel @Inject constructor(
    pedidoRepository: PedidoRepository,
    clienteRepository: ClienteRepository,
    mercadoRepository: MercadoRepository,
) : ViewModel() {

    private data class ReporteInput(
        val mode: ReporteMode = ReporteMode.DIARIO,
        val diarioPreset: DiarioPreset = DiarioPreset.HOY,
        val clientePreset: ClientePreset = ClientePreset.MES,
        val selectedClienteId: String? = null,
        val customDiarioFrom: Long? = null,
        val customDiarioTo: Long? = null,
        val customClienteFrom: Long? = null,
        val customClienteTo: Long? = null,
    )

    private val _input = MutableStateFlow(ReporteInput())
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es"))

    val uiState = combine(
        _input,
        combine(
            pedidoRepository.getAll(),
            clienteRepository.getAll(),
            mercadoRepository.getAll(),
        ) { pedidos, clientes, mercados -> Triple(pedidos, clientes, mercados) },
    ) { input, repoData ->
        val allPedidos = repoData.first
        val allClientes = repoData.second
        val allMercados = repoData.third

        val mercadoIndex = allMercados.associateBy { it.id }
        val clienteIndex = allClientes.associateBy { it.id }

        val allClienteOptions = allClientes
            .filter { !it.isBlacklisted }
            .map { c ->
                ClienteOption(
                    id = c.id,
                    name = c.name,
                    photoUrl = c.photoUrl,
                    mercadoName = mercadoIndex[c.mercadoId]?.name.orEmpty(),
                )
            }
            .sortedBy { it.name }

        when (input.mode) {
            ReporteMode.DIARIO -> buildDiarioState(input, allPedidos, clienteIndex, mercadoIndex, allClienteOptions)
            ReporteMode.POR_CLIENTE -> buildClienteState(input, allPedidos, clienteIndex, mercadoIndex, allClienteOptions)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReporteUiState(isLoading = true),
    )

    private fun buildDiarioState(
        input: ReporteInput,
        allPedidos: List<Pedido>,
        clienteIndex: Map<String, Cliente>,
        mercadoIndex: Map<String, Mercado>,
        allClienteOptions: List<ClienteOption>,
    ): ReporteUiState {
        val (from, to) = diarioRange(input.diarioPreset, input.customDiarioFrom, input.customDiarioTo)

        val cobros = allPedidos.filter { it.paidAt != null && it.paidAt in from..to && it.paid > 0 }
        val createdInRange = allPedidos.filter { it.createdAt in from..to && !it.isSaldoExtra }
        val cobrosIds = cobros.map { it.id }.toSet()

        val movimientosCobros = cobros.map { p ->
            val cliente = clienteIndex[p.clienteId]
            MovimientoItem(
                pedidoId = p.id,
                clienteName = cliente?.name.orEmpty(),
                mercadoName = mercadoIndex[cliente?.mercadoId.orEmpty()]?.name.orEmpty(),
                type = MovimientoType.COBRO,
                amount = p.paid,
                timestamp = p.paidAt!!,
            )
        }
        val movimientosPedidos = createdInRange
            .filter { it.id !in cobrosIds }
            .map { p ->
                val cliente = clienteIndex[p.clienteId]
                MovimientoItem(
                    pedidoId = p.id,
                    clienteName = cliente?.name.orEmpty(),
                    mercadoName = mercadoIndex[cliente?.mercadoId.orEmpty()]?.name.orEmpty(),
                    type = MovimientoType.PEDIDO,
                    amount = p.total,
                    timestamp = p.createdAt,
                )
            }

        return ReporteUiState(
            mode = ReporteMode.DIARIO,
            diarioPreset = input.diarioPreset,
            diarioFromMs = from,
            diarioToMs = to,
            cobradoTotal = cobros.sumOf { it.paid },
            cobroCount = cobros.size,
            pedidosCreadosCount = createdInRange.size,
            pendienteDelDia = createdInRange.sumOf { it.pending },
            movimientos = (movimientosCobros + movimientosPedidos).sortedByDescending { it.timestamp },
            customDiarioFrom = input.customDiarioFrom,
            customDiarioTo = input.customDiarioTo,
            allClientes = allClienteOptions,
            isLoading = false,
        )
    }

    private fun buildClienteState(
        input: ReporteInput,
        allPedidos: List<Pedido>,
        clienteIndex: Map<String, Cliente>,
        mercadoIndex: Map<String, Mercado>,
        allClienteOptions: List<ClienteOption>,
    ): ReporteUiState {
        val selectedId = input.selectedClienteId ?: allClienteOptions.firstOrNull()?.id
        val (from, to) = clienteRange(input.clientePreset, input.customClienteFrom, input.customClienteTo)

        val clientePedidos = if (selectedId != null) {
            allPedidos.filter { it.clienteId == selectedId && it.createdAt in from..to }
        } else emptyList()

        val billable = clientePedidos.filter { !it.isSaldoExtra }
        val historial = clientePedidos.sortedByDescending { it.createdAt }.map { p ->
            HistorialItem(
                pedidoId = p.id,
                title = if (p.isSaldoExtra) "Saldo extra" else dateFormat.format(Date(p.createdAt)),
                date = p.createdAt,
                total = p.total,
                paid = p.paid,
                pending = p.pending,
                isSaldoExtra = p.isSaldoExtra,
            )
        }

        val selectedCliente = clienteIndex[selectedId]

        return ReporteUiState(
            mode = ReporteMode.POR_CLIENTE,
            clientePreset = input.clientePreset,
            clienteFromMs = from,
            clienteToMs = to,
            selectedClienteId = selectedId,
            selectedClienteName = selectedCliente?.name.orEmpty(),
            selectedClientePhotoUrl = selectedCliente?.photoUrl,
            selectedMercadoName = mercadoIndex[selectedCliente?.mercadoId.orEmpty()]?.name.orEmpty(),
            facturado = billable.sumOf { it.total },
            pagado = billable.sumOf { it.paid },
            saldo = billable.sumOf { it.pending },
            historial = historial,
            customClienteFrom = input.customClienteFrom,
            customClienteTo = input.customClienteTo,
            allClientes = allClienteOptions,
            isLoading = false,
        )
    }

    // ── Date range helpers ────────────────────────────────────────────────────

    private fun diarioRange(preset: DiarioPreset, customFrom: Long?, customTo: Long?): Pair<Long, Long> = when (preset) {
        DiarioPreset.HOY -> Pair(startOfDay(0), endOfDay(0))
        DiarioPreset.AYER -> Pair(startOfDay(-1), endOfDay(-1))
        DiarioPreset.SEMANA -> Pair(startOfDay(-6), endOfDay(0))
        DiarioPreset.PERSONALIZADO -> Pair(customFrom ?: startOfDay(0), customTo ?: endOfDay(0))
    }

    private fun clienteRange(preset: ClientePreset, customFrom: Long?, customTo: Long?): Pair<Long, Long> {
        val to = endOfDay(0)
        return when (preset) {
            ClientePreset.MES -> Pair(startOfMonth(), to)
            ClientePreset.TRIMESTRE -> Pair(startOfQuarter(), to)
            ClientePreset.ANIO -> Pair(startOfYear(), to)
            ClientePreset.PERSONALIZADO -> Pair(customFrom ?: startOfMonth(), customTo ?: to)
        }
    }

    private fun startOfDay(offset: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offset)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun endOfDay(offset: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offset)
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    private fun startOfMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun startOfQuarter(): Long = Calendar.getInstance().apply {
        val quarterStartMonth = (get(Calendar.MONTH) / 3) * 3
        set(Calendar.MONTH, quarterStartMonth); set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun startOfYear(): Long = Calendar.getInstance().apply {
        set(Calendar.MONTH, 0); set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // ── Actions ───────────────────────────────────────────────────────────────

    fun setMode(mode: ReporteMode) { _input.value = _input.value.copy(mode = mode) }

    fun setDiarioPreset(preset: DiarioPreset) {
        _input.value = _input.value.copy(diarioPreset = preset, customDiarioFrom = null, customDiarioTo = null)
    }

    fun setClientePreset(preset: ClientePreset) {
        _input.value = _input.value.copy(clientePreset = preset, customClienteFrom = null, customClienteTo = null)
    }

    fun setCustomDiarioFrom(ms: Long) {
        _input.value = _input.value.copy(customDiarioFrom = ms, diarioPreset = DiarioPreset.PERSONALIZADO)
    }

    fun setCustomDiarioTo(ms: Long) {
        _input.value = _input.value.copy(customDiarioTo = ms, diarioPreset = DiarioPreset.PERSONALIZADO)
    }

    fun setCustomClienteFrom(ms: Long) {
        _input.value = _input.value.copy(customClienteFrom = ms, clientePreset = ClientePreset.PERSONALIZADO)
    }

    fun setCustomClienteTo(ms: Long) {
        _input.value = _input.value.copy(customClienteTo = ms, clientePreset = ClientePreset.PERSONALIZADO)
    }

    fun selectCliente(clienteId: String) {
        _input.value = _input.value.copy(selectedClienteId = clienteId)
    }
}
