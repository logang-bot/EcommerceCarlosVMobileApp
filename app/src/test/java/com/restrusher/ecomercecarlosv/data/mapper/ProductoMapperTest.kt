package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.fixtures.productoDto
import com.restrusher.ecomercecarlosv.fixtures.productoEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ProductoMapperTest {

    @Test
    fun `toDomain then toEntity — round-trips every shared field`() {
        val entity = productoEntity()

        assertEquals(entity.copy(updatedAt = 0L), ProductoMapper.toEntity(ProductoMapper.toDomain(entity)))
    }

    @Test
    fun `toDomain — absent description stays null`() {
        assertNull(ProductoMapper.toDomain(productoEntity().copy(description = null)).description)
    }

    @Test
    fun `toDomain — a deactivated producto keeps its flag`() {
        assertFalse(ProductoMapper.toDomain(productoEntity().copy(isActive = false)).isActive)
    }

    @Test
    fun `fromDto — keeps the sync bookkeeping columns`() {
        assertEquals(1_700_000_000_000L, ProductoMapper.fromDto(productoDto()).updatedAt)
    }

    @Test
    fun `toDto then fromDto — round-trips every remote field`() {
        val entity = productoEntity()

        assertEquals(entity.copy(updatedAt = 0L), ProductoMapper.fromDto(ProductoMapper.toDto(entity)))
    }
}
