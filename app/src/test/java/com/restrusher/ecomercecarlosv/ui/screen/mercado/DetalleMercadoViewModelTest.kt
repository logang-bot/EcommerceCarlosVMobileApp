package com.restrusher.ecomercecarlosv.ui.screen.mercado

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.restrusher.ecomercecarlosv.fakes.FakeClienteRepository
import com.restrusher.ecomercecarlosv.fakes.FakeMercadoRepository
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import com.restrusher.ecomercecarlosv.fixtures.cliente
import com.restrusher.ecomercecarlosv.fixtures.mercado
import com.restrusher.ecomercecarlosv.support.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Robolectric is required only so `SavedStateHandle.toRoute` can decode the route. */
@RunWith(RobolectricTestRunner::class)
class DetalleMercadoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mercados = FakeMercadoRepository()
    private val clientes = FakeClienteRepository()
    private val session = FakeSessionManager()

    private fun viewModel() = DetalleMercadoViewModel(
        mercadoRepository = mercados,
        clienteRepository = clientes,
        sessionManager = session,
        savedStateHandle = SavedStateHandle(mapOf("mercadoId" to "mercado-1")),
    )

    @Test
    fun `onShowDeleteDialog — opens the dialog without deleting anything`() = runTest {
        mercados.givenMercados(mercado(id = "mercado-1"))

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()

            vm.onShowDeleteDialog()

            assertTrue(expectMostRecentItem().showDeleteDialog)
        }
        assertNotNull(mercados.getById("mercado-1"))
    }

    @Test
    fun `onDismissDeleteDialog — closes it and leaves the mercado alone`() = runTest {
        mercados.givenMercados(mercado(id = "mercado-1"))

        val vm = viewModel()
        vm.uiState.test {
            awaitItem()
            vm.onShowDeleteDialog()

            vm.onDismissDeleteDialog()

            assertFalse(expectMostRecentItem().showDeleteDialog)
        }
        assertNotNull(mercados.getById("mercado-1"))
    }

    @Test
    fun `onDelete — deletes and reports success`() = runTest {
        mercados.givenMercados(mercado(id = "mercado-1"))
        var confirmed = false

        viewModel().onDelete { confirmed = true }

        assertTrue(confirmed)
        assertNull(mercados.getById("mercado-1"))
    }

    /** Drives the plural in the confirmation, so it has to count the clientes of this mercado only. */
    @Test
    fun `clienteCount — counts only the clientes of this mercado`() = runTest {
        mercados.givenMercados(mercado(id = "mercado-1"))
        clientes.givenClientes(
            cliente(id = "c1").copy(mercadoId = "mercado-1"),
            cliente(id = "c2").copy(mercadoId = "mercado-1"),
            cliente(id = "c3").copy(mercadoId = "mercado-2"),
        )

        viewModel().uiState.test {
            assertEquals(2, expectMostRecentItem().clienteCount)
        }
    }
}
