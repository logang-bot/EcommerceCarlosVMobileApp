package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.usecase.CalcularEstadoClienteUseCase
import com.restrusher.ecomercecarlosv.domain.usecase.CreateSaldoExtraUseCase
import com.restrusher.ecomercecarlosv.fakes.FakeClienteRepository
import com.restrusher.ecomercecarlosv.fakes.FakePedidoRepository
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import com.restrusher.ecomercecarlosv.fakes.FakeUmbralesRepository
import com.restrusher.ecomercecarlosv.fixtures.cliente
import com.restrusher.ecomercecarlosv.fixtures.pedido
import com.restrusher.ecomercecarlosv.support.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DetalleClienteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clientes = FakeClienteRepository()
    private val pedidos = FakePedidoRepository()
    private val umbrales = FakeUmbralesRepository()
    private val session = FakeSessionManager()

    private fun viewModel() = DetalleClienteViewModel(
        clienteRepository = clientes,
        pedidoRepository = pedidos,
        createSaldoExtraUseCase = CreateSaldoExtraUseCase(pedidos),
        calcularEstadoCliente = CalcularEstadoClienteUseCase(),
        umbralesRepository = umbrales,
        sessionManager = session,
        savedStateHandle = SavedStateHandle(mapOf("clienteId" to "cliente-1")),
    )

    private fun pedidoDe(
        id: String,
        status: PedidoStatus,
        total: Double,
        paid: Double = 0.0,
        isSaldoExtra: Boolean = false,
    ) = pedido(
        id = id,
        clienteId = "cliente-1",
        status = status,
        total = total,
        paid = paid,
        isSaldoExtra = isSaldoExtra,
    )

    @Test
    fun `balance — splits regular pedidos from saldos extra and totals both`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(
            pedidoDe("p1", PedidoStatus.PARTIAL, total = 100.0, paid = 40.0),
            pedidoDe("p2", PedidoStatus.PENDING, total = 50.0, isSaldoExtra = true),
            pedidoDe("p3", PedidoStatus.PAID, total = 999.0, paid = 999.0),
        )

        viewModel().uiState.test {
            val state = awaitItem()

            assertEquals(60.0, state.pedidosBalance, 0.001)
            assertEquals(50.0, state.extraBalance, 0.001)
            assertEquals(110.0, state.balance, 0.001)
        }
    }

    @Test
    fun `counts — a paid pedido is neither an unpaid pedido nor an unpaid extra`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(
            pedidoDe("p1", PedidoStatus.PARTIAL, total = 100.0),
            pedidoDe("p2", PedidoStatus.PENDING, total = 50.0, isSaldoExtra = true),
            pedidoDe("p3", PedidoStatus.PAID, total = 999.0, paid = 999.0),
        )

        viewModel().uiState.test {
            val state = awaitItem()

            assertEquals(1, state.unpaidPedidosCount)
            assertEquals(1, state.unpaidExtraCount)
            assertEquals(3, state.allPedidosCount)
        }
    }

    @Test
    fun `filters — none selected shows every pedido`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(
            pedidoDe("p1", PedidoStatus.PARTIAL, total = 100.0),
            pedidoDe("p2", PedidoStatus.PAID, total = 50.0, paid = 50.0),
        )

        viewModel().uiState.test {
            assertEquals(2, awaitItem().pedidos.size)
        }
    }

    @Test
    fun `filters — selecting a status narrows the list without changing the total count`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(
            pedidoDe("p1", PedidoStatus.PARTIAL, total = 100.0),
            pedidoDe("p2", PedidoStatus.PAID, total = 50.0, paid = 50.0),
        )

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()

            vm.onTogglePedidoFilter(PedidoStatus.PAID)

            val state = awaitItem()
            assertEquals(listOf("p2"), state.pedidos.map { it.id })
            assertEquals(2, state.allPedidosCount)
        }
    }

    @Test
    fun `filters — toggling the same status twice restores the full list`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(
            pedidoDe("p1", PedidoStatus.PARTIAL, total = 100.0),
            pedidoDe("p2", PedidoStatus.PAID, total = 50.0, paid = 50.0),
        )

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()
            vm.onTogglePedidoFilter(PedidoStatus.PAID)
            awaitItem()

            vm.onTogglePedidoFilter(PedidoStatus.PAID)

            assertEquals(2, awaitItem().pedidos.size)
        }
    }

    @Test
    fun `onClearPedidoFilters — drops every selected filter`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(pedidoDe("p1", PedidoStatus.PARTIAL, total = 100.0))

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()
            vm.onTogglePedidoFilter(PedidoStatus.PAID)
            awaitItem()

            vm.onClearPedidoFilters()

            assertEquals(emptySet<PedidoStatus>(), awaitItem().pedidoFilters)
        }
    }

    // The state is `SharingStarted.Eagerly`, so these side-effect tests can read `.value` directly
    // instead of holding a collector open.

    @Test
    fun `quitar lista negra — an automatic amount unblacklists straight away`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1", isBlacklisted = true, blacklistIsManualAmount = false))

        val vm = viewModel()
        vm.onQuitarListaNegraClick()

        assertFalse(vm.uiState.value.showUnblacklistSheet)
        assertEquals(listOf("cliente-1"), clientes.unblacklisted)
    }

    @Test
    fun `quitar lista negra — a manual amount asks first, because the debt was hand-entered`() =
        runTest {
            clientes.givenClientes(
                cliente(id = "cliente-1", isBlacklisted = true, blacklistIsManualAmount = true),
            )

            val vm = viewModel()
            vm.uiState.test {
                awaitItem()

                vm.onQuitarListaNegraClick()

                assertTrue(awaitItem().showUnblacklistSheet)
            }
        }

    @Test
    fun `dismissUnblacklistSheet — closes the sheet without unblacklisting`() = runTest {
        clientes.givenClientes(
            cliente(id = "cliente-1", isBlacklisted = true, blacklistIsManualAmount = true),
        )

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()
            vm.onQuitarListaNegraClick()
            awaitItem()

            vm.dismissUnblacklistSheet()

            assertFalse(awaitItem().showUnblacklistSheet)
        }
    }

    @Test
    fun `marcar todo pagado — a blacklist balance above what is owed becomes a saldo extra`() =
        runTest {
            clientes.givenClientes(
                cliente(id = "cliente-1", isBlacklisted = true, blacklistBalance = 500.0),
            )
            pedidos.givenPedidos(pedidoDe("p1", PedidoStatus.PARTIAL, total = 200.0))

            val vm = viewModel()
            vm.unblacklistMarkAllPaid()

            val saldoExtra = pedidos.created.single().pedido
            assertTrue(saldoExtra.isSaldoExtra)
            assertEquals(300.0, saldoExtra.total, 0.001)
            assertEquals(listOf("cliente-1"), clientes.unblacklisted)
        }

    @Test
    fun `marcar todo pagado — no excess means no saldo extra is created`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1", isBlacklisted = true, blacklistBalance = 200.0))
        pedidos.givenPedidos(pedidoDe("p1", PedidoStatus.PARTIAL, total = 200.0))

        val vm = viewModel()
        vm.unblacklistMarkAllPaid()

        assertTrue(pedidos.created.isEmpty())
        assertEquals(listOf("cliente-1"), clientes.unblacklisted)
    }
}
