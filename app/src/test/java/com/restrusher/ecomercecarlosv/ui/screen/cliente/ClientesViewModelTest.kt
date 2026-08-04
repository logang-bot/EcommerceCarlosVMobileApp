package com.restrusher.ecomercecarlosv.ui.screen.cliente

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.restrusher.ecomercecarlosv.domain.model.ClientStatus
import com.restrusher.ecomercecarlosv.domain.model.PedidoStatus
import com.restrusher.ecomercecarlosv.domain.model.Umbrales
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.domain.usecase.CalcularEstadoClienteUseCase
import com.restrusher.ecomercecarlosv.domain.usecase.RefreshClienteDataUseCase
import com.restrusher.ecomercecarlosv.fakes.FakeClienteRepository
import com.restrusher.ecomercecarlosv.fakes.FakeMercadoRepository
import com.restrusher.ecomercecarlosv.fakes.FakePedidoRepository
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import com.restrusher.ecomercecarlosv.fakes.FakeUmbralesRepository
import com.restrusher.ecomercecarlosv.fixtures.appUser
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Robolectric is required only so `SavedStateHandle.toRoute` can decode the route. */
@RunWith(RobolectricTestRunner::class)
class ClientesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clientes = FakeClienteRepository()
    private val mercados = FakeMercadoRepository()
    private val pedidos = FakePedidoRepository()
    private val umbrales = FakeUmbralesRepository()
    private val session = FakeSessionManager()

    private fun viewModel() = ClientesViewModel(
        clienteRepository = clientes,
        mercadoRepository = mercados,
        pedidoRepository = pedidos,
        umbralesRepository = umbrales,
        sessionManager = session,
        refreshClienteData = RefreshClienteDataUseCase(clientes, pedidos),
        calcularEstadoCliente = CalcularEstadoClienteUseCase(),
        savedStateHandle = SavedStateHandle(mapOf("mercadoId" to "mercado-1")),
    )

    /**
     * A recent partial debt. `createdAt` must be near-now: the ViewModel calls the status use case
     * without overriding its clock, so a fixture left at epoch 0 would be "older than diasMaximos"
     * and turn every client CRITICO regardless of amount.
     */
    private fun deuda(clienteId: String, pending: Double) = pedido(
        id = "p-$clienteId",
        clienteId = clienteId,
        status = PedidoStatus.PARTIAL,
        total = pending,
        createdAt = System.currentTimeMillis(),
    )

    @Test
    fun `default sort is alphabetical`() = runTest {
        clientes.givenClientes(
            cliente(id = "c1", name = "Zulema"),
            cliente(id = "c2", name = "Ana"),
        )

        viewModel().uiState.test {
            assertEquals(listOf("Ana", "Zulema"), awaitItem().clientes.map { it.cliente.name })
        }
    }

    @Test
    fun `only clientes of the routed mercado are listed`() = runTest {
        clientes.givenClientes(
            cliente(id = "c1", name = "Ana", mercadoId = "mercado-1"),
            cliente(id = "c2", name = "Beto", mercadoId = "mercado-2"),
        )

        viewModel().uiState.test {
            assertEquals(listOf("Ana"), awaitItem().clientes.map { it.cliente.name })
        }
    }

    @Test
    fun `mercadoName is loaded from the repository on init`() = runTest {
        mercados.save(mercado(id = "mercado-1", name = "Mercado Rodríguez"))

        viewModel().uiState.test {
            assertEquals("Mercado Rodríguez", awaitItem().mercadoName)
        }
    }

    @Test
    fun `balance — sums partial pedidos and pending saldos extra, but not plain pending pedidos`() =
        runTest {
            clientes.givenClientes(cliente(id = "c1"))
            pedidos.givenPedidos(
                pedido(id = "p1", clienteId = "c1", status = PedidoStatus.PARTIAL, total = 100.0, paid = 40.0),
                pedido(id = "p2", clienteId = "c1", status = PedidoStatus.PENDING, total = 50.0, isSaldoExtra = true),
                pedido(id = "p3", clienteId = "c1", status = PedidoStatus.PENDING, total = 999.0),
            )

            viewModel().uiState.test {
                assertEquals(110.0, awaitItem().clientes.single().balance, 0.001)
            }
        }

    @Test
    fun `status — comes from the extracted use case`() = runTest {
        umbrales.given(Umbrales(montoMaximo = 100.0, diasMaximos = 30))
        clientes.givenClientes(cliente(id = "c1"))
        pedidos.givenPedidos(deuda("c1", pending = 500.0))

        viewModel().uiState.test {
            assertEquals(ClientStatus.CRITICO, awaitItem().clientes.single().status)
        }
    }

    @Test
    fun `search — matches on name, ignoring case`() = runTest {
        clientes.givenClientes(cliente(id = "c1", name = "Ana"), cliente(id = "c2", name = "Beto"))

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()

            vm.onSearchChange("an")

            assertEquals(listOf("Ana"), awaitItem().clientes.map { it.cliente.name })
        }
    }

    @Test
    fun `search — a blank query restores the whole list`() = runTest {
        clientes.givenClientes(cliente(id = "c1", name = "Ana"), cliente(id = "c2", name = "Beto"))

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()
            vm.onSearchChange("an")
            awaitItem()

            vm.onSearchChange("")

            assertEquals(2, awaitItem().clientes.size)
        }
    }

    @Test
    fun `sort MAYOR_SALDO — orders by balance descending`() = runTest {
        clientes.givenClientes(cliente(id = "c1", name = "Ana"), cliente(id = "c2", name = "Beto"))
        pedidos.givenPedidos(deuda("c1", pending = 10.0), deuda("c2", pending = 90.0))

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()

            vm.onSortChange(ClienteSortMode.MAYOR_SALDO)

            assertEquals(listOf("Beto", "Ana"), awaitItem().clientes.map { it.cliente.name })
        }
    }

    @Test
    fun `sort SOLO_CON_DEUDA — drops clientes that owe nothing`() = runTest {
        clientes.givenClientes(cliente(id = "c1", name = "Ana"), cliente(id = "c2", name = "Beto"))
        pedidos.givenPedidos(deuda("c2", pending = 90.0))

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()

            vm.onSortChange(ClienteSortMode.SOLO_CON_DEUDA)

            assertEquals(listOf("Beto"), awaitItem().clientes.map { it.cliente.name })
        }
    }

    @Test
    fun `sort CRITICOS_FIRST — worst status first, then biggest balance`() = runTest {
        umbrales.given(Umbrales(montoMaximo = 100.0, diasMaximos = 30))
        clientes.givenClientes(
            cliente(id = "c1", name = "AlDia"),
            cliente(id = "c2", name = "Advertencia"),
            cliente(id = "c3", name = "Critico"),
        )
        pedidos.givenPedidos(deuda("c2", pending = 50.0), deuda("c3", pending = 500.0))

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()

            vm.onSortChange(ClienteSortMode.CRITICOS_FIRST)

            assertEquals(
                listOf("AlDia", "Advertencia", "Critico"),
                awaitItem().clientes.map { it.cliente.name },
            )
        }
    }

    @Test
    fun `canWrite — an invitado may not write`() = runTest {
        session.setCurrentUser(appUser(role = UserRole.INVITADO))

        viewModel().uiState.test {
            assertFalse(awaitItem().canWrite)
        }
    }

    @Test
    fun `canWrite — a regular usuario may write`() = runTest {
        session.setCurrentUser(appUser(role = UserRole.USUARIO))

        viewModel().uiState.test {
            assertTrue(awaitItem().canWrite)
        }
    }

    // onRefresh toggles isRefreshing around the call, so these read the settled state rather than
    // the intermediate emissions.

    @Test
    fun `onRefresh — a failed refresh raises refreshFailed and then clears on dismiss`() = runTest {
        pedidos.refreshResult = false

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()

            vm.onRefresh()

            assertTrue(expectMostRecentItem().refreshFailed)

            vm.onRefreshErrorDismissed()

            assertFalse(expectMostRecentItem().refreshFailed)
        }
    }

    @Test
    fun `onRefresh — a successful refresh leaves no error and refreshes both entities`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            awaitItem()

            vm.onRefresh()

            val settled = expectMostRecentItem()
            assertFalse(settled.refreshFailed)
            assertFalse(settled.isRefreshing)
        }

        assertEquals(1, clientes.refreshCount)
        assertEquals(1, pedidos.refreshCount)
    }
}
