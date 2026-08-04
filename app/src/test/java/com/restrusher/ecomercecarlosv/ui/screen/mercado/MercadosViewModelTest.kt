package com.restrusher.ecomercecarlosv.ui.screen.mercado

import app.cash.turbine.test
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import com.restrusher.ecomercecarlosv.domain.usecase.CalcularEstadoClienteUseCase
import com.restrusher.ecomercecarlosv.domain.usecase.RefreshMercadoDataUseCase
import com.restrusher.ecomercecarlosv.fakes.FakeClienteRepository
import com.restrusher.ecomercecarlosv.fakes.FakeMercadoRepository
import com.restrusher.ecomercecarlosv.fakes.FakePedidoRepository
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import com.restrusher.ecomercecarlosv.fakes.FakeSyncOperationDao
import com.restrusher.ecomercecarlosv.fakes.FakeUmbralesRepository
import com.restrusher.ecomercecarlosv.fixtures.cliente
import com.restrusher.ecomercecarlosv.fixtures.mercado
import com.restrusher.ecomercecarlosv.fixtures.pedido
import com.restrusher.ecomercecarlosv.support.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * No Robolectric — this ViewModel takes no route, so it constructs on a plain JVM test.
 *
 * The dashboard dot used to have its own status rule: it counted every unpaid pedido and hardcoded
 * 200,0 / 30 días, so a mercado could show red while every client inside it showed AL_DIA. It now
 * delegates to `CalcularEstadoClienteUseCase`, and most of these tests pin that difference.
 *
 * ⚠️ `pedido()` defaults to `createdAt = 0L`, which is always older than `diasMaximos` and would
 * make every client CRITICO by accident — every debt here sets [RECENT] explicitly.
 */
class MercadosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mercadoRepository = FakeMercadoRepository()
    private val clienteRepository = FakeClienteRepository()
    private val pedidoRepository = FakePedidoRepository()
    private val umbralesRepository = FakeUmbralesRepository()
    private val sessionManager = FakeSessionManager()
    private val syncOperationDao = FakeSyncOperationDao()

    private fun viewModel() = MercadosViewModel(
        mercadoRepository = mercadoRepository,
        clienteRepository = clienteRepository,
        pedidoRepository = pedidoRepository,
        umbralesRepository = umbralesRepository,
        calcularEstadoCliente = CalcularEstadoClienteUseCase(),
        sessionManager = sessionManager,
        syncOperationDao = syncOperationDao,
        refreshMercadoData = RefreshMercadoDataUseCase(
            mercadoRepository,
            clienteRepository,
            pedidoRepository,
        ),
    )

    /** `uiState` is `WhileSubscribed`, so it needs a live collector; the initial value has no stats yet. */
    private suspend fun statOfMercado1(): MercadoStat {
        lateinit var stat: MercadoStat
        viewModel().uiState.test {
            var state = awaitItem()
            while (state.stats.isEmpty()) state = awaitItem()
            stat = state.stats.getValue("mercado-1")
        }
        return stat
    }

    @Test
    fun `stats — partial debt over montoMaximo — the mercado is critical`() = runTest {
        mercadoRepository.givenMercados(mercado())
        clienteRepository.givenClientes(cliente())
        pedidoRepository.givenPedidos(
            pedido(status = PedidoStatus.PARTIAL, total = 300.0, paid = 50.0, createdAt = RECENT),
        )

        val stat = statOfMercado1()

        assertTrue(stat.hasCritical)
    }

    @Test
    fun `stats — recent partial debt under montoMaximo — the mercado is a warning, not critical`() = runTest {
        mercadoRepository.givenMercados(mercado())
        clienteRepository.givenClientes(cliente())
        pedidoRepository.givenPedidos(
            pedido(status = PedidoStatus.PARTIAL, total = 100.0, paid = 50.0, createdAt = RECENT),
        )

        val stat = statOfMercado1()

        assertTrue(stat.hasWarning)
        assertFalse(stat.hasCritical)
    }

    @Test
    fun `stats — an old untouched PENDING pedido — leaves the mercado clean`() = runTest {
        mercadoRepository.givenMercados(mercado())
        clienteRepository.givenClientes(cliente())
        // The old rule scored this critical. A normal open order is not mora.
        pedidoRepository.givenPedidos(
            pedido(status = PedidoStatus.PENDING, total = 500.0, paid = 0.0, createdAt = ANCIENT),
        )

        val stat = statOfMercado1()

        assertFalse(stat.hasWarning)
        assertFalse(stat.hasCritical)
    }

    @Test
    fun `stats — an old saldo extra — leaves the mercado clean`() = runTest {
        mercadoRepository.givenMercados(mercado())
        clienteRepository.givenClientes(cliente())
        // The old rule scored this critical too. Manually recorded debt is not an unpaid delivery.
        pedidoRepository.givenPedidos(
            pedido(
                status = PedidoStatus.PENDING,
                total = 500.0,
                paid = 0.0,
                createdAt = ANCIENT,
                isSaldoExtra = true,
            ),
        )

        val stat = statOfMercado1()

        assertFalse(stat.hasWarning)
        assertFalse(stat.hasCritical)
    }

    @Test
    fun `stats — raising montoMaximo — downgrades the mercado from critical to warning`() = runTest {
        mercadoRepository.givenMercados(mercado())
        clienteRepository.givenClientes(cliente())
        pedidoRepository.givenPedidos(
            pedido(status = PedidoStatus.PARTIAL, total = 300.0, paid = 50.0, createdAt = RECENT),
        )
        // The dashboard used to hardcode its thresholds, so Ajustes had no effect on it.
        umbralesRepository.given(Umbrales(montoMaximo = 1_000.0, diasMaximos = 30))

        val stat = statOfMercado1()

        assertTrue(stat.hasWarning)
        assertFalse(stat.hasCritical)
    }

    @Test
    fun `stats — lowering diasMaximos — promotes a warning to critical`() = runTest {
        mercadoRepository.givenMercados(mercado())
        clienteRepository.givenClientes(cliente())
        // Two days old and well under montoMaximo: ADVERTENCIA at the default 30 días, CRITICO at 1.
        pedidoRepository.givenPedidos(
            pedido(status = PedidoStatus.PARTIAL, total = 100.0, paid = 50.0, createdAt = TWO_DAYS_AGO),
        )
        umbralesRepository.given(Umbrales(montoMaximo = 200.0, diasMaximos = 1))

        val stat = statOfMercado1()

        assertTrue(stat.hasCritical)
    }

    @Test
    fun `stats — one client in mora among several — colours the whole mercado`() = runTest {
        mercadoRepository.givenMercados(mercado())
        clienteRepository.givenClientes(
            cliente(id = "cliente-1"),
            cliente(id = "cliente-2", name = "Don Beto"),
        )
        pedidoRepository.givenPedidos(
            pedido(id = "p1", clienteId = "cliente-2", status = PedidoStatus.PARTIAL, total = 300.0, paid = 10.0, createdAt = RECENT),
        )

        val stat = statOfMercado1()

        assertTrue(stat.hasCritical)
        assertEquals(2, stat.activeClientCount)
    }

    @Test
    fun `stats — blacklisted clients — are excluded from the count and the dot`() = runTest {
        mercadoRepository.givenMercados(mercado())
        clienteRepository.givenClientes(
            cliente(id = "cliente-1"),
            cliente(id = "cliente-2", name = "Don Beto", isBlacklisted = true),
        )
        pedidoRepository.givenPedidos(
            pedido(id = "p1", clienteId = "cliente-2", status = PedidoStatus.PARTIAL, total = 900.0, paid = 10.0, createdAt = RECENT),
        )

        val stat = statOfMercado1()

        assertEquals(1, stat.activeClientCount)
        assertFalse(stat.hasCritical)
    }

    @Test
    fun `stats — a mercado with no clients — is clean and counts zero`() = runTest {
        mercadoRepository.givenMercados(mercado())

        val stat = statOfMercado1()

        assertEquals(MercadoStat(), stat)
    }
}

/** Recent enough to sit inside any sane `diasMaximos`; the fixture default of 0L never is. */
private val RECENT = System.currentTimeMillis()

/** Inside the default 30 días but outside a `diasMaximos` of 1. */
private val TWO_DAYS_AGO = System.currentTimeMillis() - 2L * 24 * 60 * 60 * 1000

/** Comfortably past `diasMaximos`, to prove age alone no longer colours a mercado. */
private val ANCIENT = System.currentTimeMillis() - 400L * 24 * 60 * 60 * 1000
