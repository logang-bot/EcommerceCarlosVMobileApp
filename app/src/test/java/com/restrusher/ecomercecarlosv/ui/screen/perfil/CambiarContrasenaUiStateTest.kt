package com.restrusher.ecomercecarlosv.ui.screen.perfil

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CambiarContrasenaUiStateTest {

    private fun state(
        newPassword: String = "",
        confirmPassword: String = "",
        currentPassword: String = "",
        isSelf: Boolean = false,
        targetName: String = "",
    ) = CambiarContrasenaUiState(
        targetName = targetName,
        isSelf = isSelf,
        currentPassword = currentPassword,
        newPassword = newPassword,
        confirmPassword = confirmPassword,
    )

    @Test
    fun `meetsLength — exactly eight characters — is satisfied`() {
        assertTrue(state(newPassword = "Abcdef12").meetsLength)
    }

    @Test
    fun `meetsLength — seven characters — is not satisfied`() {
        assertFalse(state(newPassword = "Abcde12").meetsLength)
    }

    @Test
    fun `meetsNumber — no digit present — is not satisfied`() {
        assertFalse(state(newPassword = "Abcdefgh").meetsNumber)
    }

    @Test
    fun `meetsCasing — only lowercase — is not satisfied`() {
        assertFalse(state(newPassword = "abcdef12").meetsCasing)
    }

    @Test
    fun `meetsCasing — only uppercase — is not satisfied`() {
        assertFalse(state(newPassword = "ABCDEF12").meetsCasing)
    }

    @Test
    fun `meetsCasing — both cases present — is satisfied`() {
        assertTrue(state(newPassword = "Abcdef12").meetsCasing)
    }

    @Test
    fun `passwordMismatch — confirmation still empty — stays false while typing`() {
        assertFalse(state(newPassword = "Abcdef12", confirmPassword = "").passwordMismatch)
    }

    @Test
    fun `passwordMismatch — confirmation differs — is true`() {
        assertTrue(state(newPassword = "Abcdef12", confirmPassword = "Abcdef13").passwordMismatch)
    }

    @Test
    fun `isValid — all rules met and confirmation matches — is true`() {
        assertTrue(state(newPassword = "Abcdef12", confirmPassword = "Abcdef12").isValid)
    }

    @Test
    fun `isValid — a single rule unmet — is false`() {
        assertFalse(state(newPassword = "Abcdefgh", confirmPassword = "Abcdefgh").isValid)
    }

    @Test
    fun `isValid — confirmation does not match — is false`() {
        assertFalse(state(newPassword = "Abcdef12", confirmPassword = "Abcdef13").isValid)
    }

    @Test
    fun `isValid — changing your own password without the current one — is false`() {
        val subject = state(newPassword = "Abcdef12", confirmPassword = "Abcdef12", isSelf = true)

        assertFalse(subject.isValid)
    }

    @Test
    fun `isValid — changing your own password with the current one — is true`() {
        val subject = state(
            newPassword = "Abcdef12",
            confirmPassword = "Abcdef12",
            currentPassword = "anterior",
            isSelf = true,
        )

        assertTrue(subject.isValid)
    }

    @Test
    fun `isValid — changing somebody else's password — does not require the current one`() {
        val subject = state(newPassword = "Abcdef12", confirmPassword = "Abcdef12", isSelf = false)

        assertTrue(subject.isValid)
    }

    @Test
    fun `targetFirstName — full name — returns the first token`() {
        assertEquals("Carlos", state(targetName = "Carlos Vargas Rojas").targetFirstName)
    }

    @Test
    fun `targetFirstName — leading whitespace — returns the first non-blank token`() {
        assertEquals("Carlos", state(targetName = "  Carlos Vargas").targetFirstName)
    }

    @Test
    fun `targetFirstName — blank name — falls back to the original string`() {
        assertEquals("   ", state(targetName = "   ").targetFirstName)
    }
}
