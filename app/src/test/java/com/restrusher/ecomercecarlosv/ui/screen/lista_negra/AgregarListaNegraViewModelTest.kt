package com.restrusher.ecomercecarlosv.ui.screen.lista_negra

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.fakes.FakeClienteRepository
import com.restrusher.ecomercecarlosv.fakes.FakePedidoRepository
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
class AgregarListaNegraViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clientes = FakeClienteRepository()
    private val pedidos = FakePedidoRepository()

    private fun viewModel() = AgregarListaNegraViewModel(
        clienteRepository = clientes,
        pedidoRepository = pedidos,
        savedStateHandle = SavedStateHandle(mapOf("clienteId" to "cliente-1")),
    )

    private fun deuda(id: String, pending: Double) =
        pedido(id = id, clienteId = "cliente-1", status = PedidoStatus.PARTIAL, total = pending)

    @Test
    fun `first load with pending pedidos — defaults to AUTO`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(deuda("p1", pending = 100.0))

        viewModel().uiState.test {
            val state = awaitItem()

            assertEquals(TotalMode.AUTO, state.totalMode)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `first load with nothing pending — defaults to MANUAL`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))

        viewModel().uiState.test {
            assertEquals(TotalMode.MANUAL, awaitItem().totalMode)
        }
    }

    @Test
    fun `loaded state — carries the cliente name and the pending pedidos`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1", name = "Doña Ana"))
        pedidos.givenPedidos(deuda("p1", pending = 100.0), deuda("p2", pending = 50.0))

        viewModel().uiState.test {
            val state = awaitItem()

            assertEquals("Doña Ana", state.clienteName)
            assertEquals(2, state.pendingPedidos.size)
            assertEquals(150.0, state.autoAmount, 0.001)
        }
    }

    @Test
    fun `pending list — excludes pedidos that are already paid`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(
            deuda("p1", pending = 100.0),
            pedido(
                id = "p2",
                clienteId = "cliente-1",
                status = PedidoStatus.PAID,
                total = 50.0,
                paid = 50.0,
            ),
        )

        viewModel().uiState.test {
            assertEquals(listOf("p1"), awaitItem().pendingPedidos.map { it.id })
        }
    }

    @Test
    fun `a user's mode choice survives a later data emission`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(deuda("p1", pending = 100.0))

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()

            vm.onTotalModeChange(TotalMode.MANUAL)
            assertEquals(TotalMode.MANUAL, awaitItem().totalMode)

            pedidos.givenPedidos(deuda("p1", pending = 100.0), deuda("p2", pending = 25.0))

            assertEquals(TotalMode.MANUAL, expectMostRecentItem().totalMode)
        }
    }

    @Test
    fun `onConfirm — is ignored while the form is incomplete`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        var succeeded = false

        val vm = viewModel()
        vm.uiState.test { awaitItem() }

        vm.onConfirm { succeeded = true }

        assertFalse(succeeded)
    }

    @Test
    fun `onConfirm — in AUTO mode blacklists with the computed total`() = runTest {
        clientes.givenClientes(cliente(id = "cliente-1"))
        pedidos.givenPedidos(deuda("p1", pending = 100.0), deuda("p2", pending = 50.0))
        var succeeded = false

        val vm = viewModel()
        vm.uiState.test { awaitItem() }
        vm.onReasonChange("No paga hace meses")

        vm.onConfirm { succeeded = true }

        assertTrue(succeeded)
        val blacklisted = clientes.blacklisted.single()
        assertEquals(150.0, blacklisted.balance, 0.001)
        assertFalse(blacklisted.isManualAmount)
    }

    @Test
    fun `onConfirm — in MANUAL mode blacklists with the typed amount and flags it as manual`() =
        runTest {
            clientes.givenClientes(cliente(id = "cliente-1"))
            pedidos.givenPedidos(deuda("p1", pending = 100.0))

            val vm = viewModel()
            vm.uiState.test { awaitItem() }
            vm.onTotalModeChange(TotalMode.MANUAL)
            vm.onAmountChange("75.5")
            vm.onReasonChange("Acuerdo verbal")

            vm.onConfirm { }

            val blacklisted = clientes.blacklisted.single()
            assertEquals(75.5, blacklisted.balance, 0.001)
            assertEquals("Acuerdo verbal", blacklisted.reason)
            assertTrue(blacklisted.isManualAmount)
        }
}
