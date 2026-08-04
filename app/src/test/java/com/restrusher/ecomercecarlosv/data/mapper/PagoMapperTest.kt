package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.fixtures.pagoDto
import com.restrusher.ecomercecarlosv.fixtures.pagoEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class PagoMapperTest {

    @Test
    fun `toDomain then toEntity — round-trips every field`() {
        val entity = pagoEntity()

        assertEquals(entity, PagoMapper.toEntity(PagoMapper.toDomain(entity)))
    }

    @Test
    fun `toDto then fromDto — round-trips every field`() {
        val entity = pagoEntity()

        assertEquals(entity, PagoMapper.fromDto(PagoMapper.toDto(entity)))
    }

    @Test
    fun `fromDto — carries the amount and the payment timestamp`() {
        val entity = PagoMapper.fromDto(pagoDto())

        assertEquals(45.0, entity.amount, 0.001)
        assertEquals(1_700_000_000_000L, entity.paidAt)
    }
}
