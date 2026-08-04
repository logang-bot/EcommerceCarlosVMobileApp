package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.fixtures.mercadoDto
import com.restrusher.ecomercecarlosv.fixtures.mercadoEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MercadoMapperTest {

    @Test
    fun `toDomain then toEntity — round-trips every shared field`() {
        val entity = mercadoEntity()

        assertEquals(entity.copy(updatedAt = 0L), MercadoMapper.toEntity(MercadoMapper.toDomain(entity)))
    }

    @Test
    fun `toDomain — absent coordinates stay null`() {
        val domain = MercadoMapper.toDomain(mercadoEntity().copy(latitude = null, longitude = null))

        assertNull(domain.latitude)
        assertNull(domain.longitude)
    }

    @Test
    fun `fromDto — keeps the sync bookkeeping columns`() {
        val entity = MercadoMapper.fromDto(mercadoDto())

        assertEquals(1_700_000_000_000L, entity.updatedAt)
    }

    @Test
    fun `toDto then fromDto — round-trips every remote field`() {
        val entity = mercadoEntity()

        assertEquals(entity.copy(updatedAt = 0L), MercadoMapper.fromDto(MercadoMapper.toDto(entity)))
    }
}
