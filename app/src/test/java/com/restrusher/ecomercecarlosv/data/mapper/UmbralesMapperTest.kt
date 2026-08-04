package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.data.local.entity.UmbralesEntity
import com.restrusher.ecomercecarlosv.fixtures.umbralesDto
import com.restrusher.ecomercecarlosv.fixtures.umbralesEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class UmbralesMapperTest {

    @Test
    fun `toDomain — carries both thresholds`() {
        val domain = UmbralesMapper.toDomain(umbralesEntity(montoMaximo = 500.0, diasMaximos = 45))

        assertEquals(500.0, domain.montoMaximo, 0.001)
        assertEquals(45, domain.diasMaximos)
    }

    @Test
    fun `toEntity — always targets the singleton row`() {
        val domain = UmbralesMapper.toDomain(umbralesEntity())

        assertEquals(UmbralesEntity.SINGLETON_ID, UmbralesMapper.toEntity(domain).id)
    }

    @Test
    fun `toDomain then toEntity — drops updatedAt because the domain model has no such field`() {
        val entity = umbralesEntity()

        assertEquals(entity.copy(updatedAt = 0L), UmbralesMapper.toEntity(UmbralesMapper.toDomain(entity)))
    }

    @Test
    fun `fromDto — keeps the id and the sync timestamp`() {
        val entity = UmbralesMapper.fromDto(umbralesDto())

        assertEquals(UmbralesEntity.SINGLETON_ID, entity.id)
        assertEquals(1_700_000_000_000L, entity.updatedAt)
    }

    @Test
    fun `toDto then fromDto — drops updatedAt because the dto default wins`() {
        val entity = umbralesEntity()

        assertEquals(entity.copy(updatedAt = 0L), UmbralesMapper.fromDto(UmbralesMapper.toDto(entity)))
    }
}
