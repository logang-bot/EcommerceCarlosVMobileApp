package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.fixtures.clienteDto
import com.restrusher.ecomercecarlosv.fixtures.clienteEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClienteMapperTest {

    @Test
    fun `toDomain then toEntity — round-trips every shared field`() {
        val entity = clienteEntity()

        val result = ClienteMapper.toEntity(ClienteMapper.toDomain(entity))

        // updatedAt and isDeleted are sync bookkeeping, absent from the domain model.
        assertEquals(entity.copy(updatedAt = 0L), result)
    }

    @Test
    fun `toDomain — pipe-separated phones — splits into a list`() {
        val domain = ClienteMapper.toDomain(clienteEntity(phones = "70000001|70000002|70000003"))

        assertEquals(listOf("70000001", "70000002", "70000003"), domain.phones)
    }

    @Test
    fun `toDomain — a single phone without a pipe — yields one entry`() {
        assertEquals(listOf("70000001"), ClienteMapper.toDomain(clienteEntity(phones = "70000001")).phones)
    }

    @Test
    fun `toDomain — blank phones column — yields an empty list rather than one blank entry`() {
        assertEquals(emptyList<String>(), ClienteMapper.toDomain(clienteEntity(phones = "")).phones)
    }

    @Test
    fun `toEntity — empty phone list — joins to an empty string`() {
        val domain = ClienteMapper.toDomain(clienteEntity(phones = ""))

        assertEquals("", ClienteMapper.toEntity(domain).phones)
    }

    @Test
    fun `fromDto — no existing row — local-only fields fall back to their defaults`() {
        val entity = ClienteMapper.fromDto(clienteDto(), existing = null)

        assertEquals(0, entity.primaryPhoneIndex)
        assertEquals(0.0, entity.blacklistBalance, 0.001)
        assertFalse(entity.blacklistIsManualAmount)
    }

    @Test
    fun `fromDto — existing row supplied — local-only fields are preserved`() {
        val existing = clienteEntity(
            primaryPhoneIndex = 2,
            blacklistBalance = 250.0,
            blacklistIsManualAmount = true,
        )

        val entity = ClienteMapper.fromDto(clienteDto(), existing = existing)

        assertEquals(2, entity.primaryPhoneIndex)
        assertEquals(250.0, entity.blacklistBalance, 0.001)
        assertTrue(entity.blacklistIsManualAmount)
    }

    @Test
    fun `fromDto — remote fields always win over the existing row`() {
        val existing = clienteEntity().copy(name = "Nombre viejo", isDeleted = true)

        val entity = ClienteMapper.fromDto(clienteDto(isDeleted = false), existing = existing)

        assertEquals("Doña Ana", entity.name)
        assertFalse(entity.isDeleted)
    }

    @Test
    fun `fromDto — null phones from the server — becomes an empty string`() {
        assertEquals("", ClienteMapper.fromDto(clienteDto(phones = null)).phones)
    }

    @Test
    fun `toDto then fromDto — drops the local-only fields when no existing row is merged`() {
        val entity = clienteEntity(primaryPhoneIndex = 2, blacklistBalance = 250.0)

        val result = ClienteMapper.fromDto(ClienteMapper.toDto(entity), existing = null)

        assertEquals(0, result.primaryPhoneIndex)
        assertEquals(0.0, result.blacklistBalance, 0.001)
    }

    @Test
    fun `toDto — carries the soft-delete flag to the server`() {
        assertTrue(ClienteMapper.toDto(clienteEntity(isDeleted = true)).isDeleted)
    }
}
