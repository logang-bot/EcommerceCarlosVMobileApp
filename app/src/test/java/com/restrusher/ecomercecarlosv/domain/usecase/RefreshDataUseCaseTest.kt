package com.restrusher.ecomercecarlosv.domain.usecase

import com.restrusher.ecomercecarlosv.fakes.FakeClienteRepository
import com.restrusher.ecomercecarlosv.fakes.FakeMercadoRepository
import com.restrusher.ecomercecarlosv.fakes.FakePedidoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Both use cases fan out with `async`, so a failing refresh must not short-circuit the others —
 * the call counts are what prove that.
 */
class RefreshDataUseCaseTest {

    private val clientes = FakeClienteRepository()
    private val pedidos = FakePedidoRepository()
    private val mercados = FakeMercadoRepository()

    private val refreshCliente = RefreshClienteDataUseCase(clientes, pedidos)
    private val refreshMercado = RefreshMercadoDataUseCase(mercados, clientes, pedidos)

    @Test
    fun `cliente data — both refreshes succeed — reports success`() = runTest {
        assertTrue(refreshCliente())
    }

    @Test
    fun `cliente data — refreshes clientes and pedidos exactly once each`() = runTest {
        refreshCliente()

        assertEquals(1, clientes.refreshCount)
        assertEquals(1, pedidos.refreshCount)
    }

    @Test
    fun `cliente data — clientes fails — reports failure but still refreshes pedidos`() = runTest {
        clientes.refreshResult = false

        assertFalse(refreshCliente())
        assertEquals(1, pedidos.refreshCount)
    }

    @Test
    fun `cliente data — pedidos fails — reports failure but still refreshes clientes`() = runTest {
        pedidos.refreshResult = false

        assertFalse(refreshCliente())
        assertEquals(1, clientes.refreshCount)
    }

    @Test
    fun `mercado data — all three succeed — reports success`() = runTest {
        assertTrue(refreshMercado())
    }

    @Test
    fun `mercado data — refreshes all three entities exactly once each`() = runTest {
        refreshMercado()

        assertEquals(1, mercados.refreshCount)
        assertEquals(1, clientes.refreshCount)
        assertEquals(1, pedidos.refreshCount)
    }

    @Test
    fun `mercado data — the first refresh fails — the other two still run`() = runTest {
        mercados.refreshResult = false

        assertFalse(refreshMercado())
        assertEquals(1, clientes.refreshCount)
        assertEquals(1, pedidos.refreshCount)
    }

    @Test
    fun `mercado data — the last refresh fails — reports failure`() = runTest {
        pedidos.refreshResult = false

        assertFalse(refreshMercado())
    }

    @Test
    fun `mercado data — every refresh fails — reports failure`() = runTest {
        mercados.refreshResult = false
        clientes.refreshResult = false
        pedidos.refreshResult = false

        assertFalse(refreshMercado())
    }
}
