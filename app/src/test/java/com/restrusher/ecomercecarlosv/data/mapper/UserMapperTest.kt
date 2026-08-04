package com.restrusher.ecomercecarlosv.data.mapper

import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.fixtures.userDto
import com.restrusher.ecomercecarlosv.fixtures.userEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserMapperTest {

    @Test
    fun `toDomain then toEntity — round-trips every field`() {
        val entity = userEntity()

        assertEquals(entity, UserMapper.toEntity(UserMapper.toDomain(entity)))
    }

    @Test
    fun `toDomain — role column — is parsed into the enum`() {
        assertEquals(UserRole.USUARIO, UserMapper.toDomain(userEntity(role = "USUARIO")).role)
    }

    @Test
    fun `toEntity — enum role — is stored as its name`() {
        val domain = UserMapper.toDomain(userEntity(role = "USUARIO"))

        assertEquals("USUARIO", UserMapper.toEntity(domain).role)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toDomain — unknown role column — throws rather than defaulting`() {
        UserMapper.toDomain(userEntity(role = "ADMIN"))
    }

    @Test
    fun `toDomain from a dto — biometric enrolment is device-local so it is never set`() {
        assertNull(UserMapper.toDomain(userDto()).biometricEnabledAt)
    }

    @Test
    fun `toDto — does not expose the biometric enrolment timestamp`() {
        val domain = UserMapper.toDomain(userEntity(biometricEnabledAt = 1_700_000_000_000L))

        // UserDto has no such field; the round-trip back through it must lose the value.
        assertNull(UserMapper.toDomain(UserMapper.toDto(domain)).biometricEnabledAt)
    }

    @Test
    fun `toDto then toDomain — round-trips every remote field`() {
        val domain = UserMapper.toDomain(userEntity(biometricEnabledAt = null))

        assertEquals(domain, UserMapper.toDomain(UserMapper.toDto(domain)))
    }
}
