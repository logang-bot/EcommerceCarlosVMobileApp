package com.restrusher.ecomercecarlosv.domain.model

import com.restrusher.ecomercecarlosv.fixtures.detallePedido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DetallePedidoTest {

    @Test
    fun `subtotal — multiplies unit price by quantity`() {
        assertEquals(37.5, detallePedido(unitPrice = 12.5, quantity = 3).subtotal, 0.001)
    }

    @Test
    fun `subtotal — quantity of zero — is zero`() {
        assertEquals(0.0, detallePedido(unitPrice = 12.5, quantity = 0).subtotal, 0.001)
    }

    @Test
    fun `isPriceModified — unit price matches the catalog — is false`() {
        assertFalse(detallePedido(unitPrice = 10.0, catalogPrice = 10.0).isPriceModified)
    }

    @Test
    fun `isPriceModified — sold above the catalog price — is true`() {
        assertTrue(detallePedido(unitPrice = 12.0, catalogPrice = 10.0).isPriceModified)
    }

    @Test
    fun `isPriceModified — sold at a discount — is true`() {
        assertTrue(detallePedido(unitPrice = 8.0, catalogPrice = 10.0).isPriceModified)
    }
}
