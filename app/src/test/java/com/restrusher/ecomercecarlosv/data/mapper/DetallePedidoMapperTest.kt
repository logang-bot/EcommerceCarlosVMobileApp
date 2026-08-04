package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.fixtures.detallePedidoDto
import com.restrusher.ecomercecarlosv.fixtures.detallePedidoEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetallePedidoMapperTest {

    @Test
    fun `toDomain then toEntity — round-trips every field`() {
        val entity = detallePedidoEntity()

        assertEquals(entity, DetallePedidoMapper.toEntity(DetallePedidoMapper.toDomain(entity)))
    }

    @Test
    fun `toDto then fromDto — round-trips every field`() {
        val entity = detallePedidoEntity()

        assertEquals(entity, DetallePedidoMapper.fromDto(DetallePedidoMapper.toDto(entity)))
    }

    @Test
    fun `toDomain — keeps the catalog price alongside the sold price`() {
        val domain = DetallePedidoMapper.toDomain(detallePedidoEntity())

        assertEquals(30.0, domain.unitPrice, 0.001)
        assertEquals(32.5, domain.catalogPrice, 0.001)
    }

    @Test
    fun `fromDto — absent notes stay null`() {
        assertNull(DetallePedidoMapper.fromDto(detallePedidoDto().copy(notes = null)).notes)
    }
}
