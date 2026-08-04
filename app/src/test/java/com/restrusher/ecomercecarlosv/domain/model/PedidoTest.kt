package com.restrusher.ecomercecarlosv.domain.model

import com.restrusher.ecomercecarlosv.fixtures.pedido
import org.junit.Assert.assertEquals
import org.junit.Test

class PedidoTest {

    @Test
    fun `pending — partially paid — is the difference`() {
        assertEquals(40.0, pedido(total = 100.0, paid = 60.0).pending, 0.001)
    }

    @Test
    fun `pending — nothing paid — is the full total`() {
        assertEquals(100.0, pedido(total = 100.0, paid = 0.0).pending, 0.001)
    }

    @Test
    fun `pending — paid exactly matches total — is zero`() {
        assertEquals(0.0, pedido(total = 100.0, paid = 100.0).pending, 0.001)
    }

    @Test
    fun `pending — overpaid — clamps to zero instead of going negative`() {
        assertEquals(0.0, pedido(total = 100.0, paid = 150.0).pending, 0.001)
    }

    @Test
    fun `pending — saldo extra with a zero total — is zero`() {
        assertEquals(0.0, pedido(total = 0.0, paid = 0.0, isSaldoExtra = true).pending, 0.001)
    }
}
