package com.restrusher.ecomercecarlosv.ui.screen.perfil

import androidx.lifecycle.SavedStateHandle
import com.restrusher.ecomercecarlosv.data.remote.AdminUserService
import com.restrusher.ecomercecarlosv.domain.model.UserRole
import com.restrusher.ecomercecarlosv.fakes.FakeSessionManager
import com.restrusher.ecomercecarlosv.fakes.FakeUserRepository
import com.restrusher.ecomercecarlosv.fixtures.appUser
import com.restrusher.ecomercecarlosv.support.MainDispatcherRule
import io.github.jan.supabase.SupabaseClient
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Covers the profile load and the save gate. The two save paths themselves are not exercised —
 * they reach Supabase and the admin Edge Function, which belongs in an integration test rather
 * than behind a pile of mocks. The password rules are covered by `CambiarContrasenaUiStateTest`.
 */
@RunWith(RobolectricTestRunner::class)
class CambiarContrasenaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = FakeUserRepository()
    private val session = FakeSessionManager()
    private val supabase = mockk<SupabaseClient>(relaxed = true)
    private val adminUserService = mockk<AdminUserService>(relaxed = true)

    private fun viewModel(isSelf: Boolean, userId: String = "user-1") = CambiarContrasenaViewModel(
        savedStateHandle = SavedStateHandle(mapOf("userId" to userId, "isSelf" to isSelf)),
        userRepository = userRepository,
        sessionManager = session,
        supabase = supabase,
        adminUserService = adminUserService,
    )

    @Test
    fun `changing your own password — the profile comes from the active session`() = runTest {
        session.setCurrentUser(appUser(id = "user-1", name = "Carlos Vargas", role = UserRole.SUPERUSUARIO))

        val state = viewModel(isSelf = true).state.value

        assertEquals("Carlos Vargas", state.targetName)
        assertEquals("user-1@example.test", state.targetEmail)
        assertEquals(UserRole.SUPERUSUARIO, state.targetRole)
        assertTrue(state.isSelf)
    }

    @Test
    fun `changing somebody else's password — the profile is read from the repository`() = runTest {
        userRepository.givenUsers(appUser(id = "user-9", name = "Ana Quispe", role = UserRole.USUARIO))

        val state = viewModel(isSelf = false, userId = "user-9").state.value

        assertEquals("Ana Quispe", state.targetName)
        assertEquals(UserRole.USUARIO, state.targetRole)
        assertFalse(state.isSelf)
    }

    @Test
    fun `an unknown user leaves the profile fields blank rather than crashing`() = runTest {
        val state = viewModel(isSelf = false, userId = "user-desconocido").state.value

        assertEquals("", state.targetName)
        assertEquals("", state.targetEmail)
    }

    @Test
    fun `onSave — an invalid form never reaches the admin service`() = runTest {
        userRepository.givenUsers(appUser(id = "user-9"))
        val vm = viewModel(isSelf = false, userId = "user-9")

        vm.onNewPasswordChange("corta")
        vm.onConfirmPasswordChange("corta")
        vm.onSave()

        coVerify(exactly = 0) { adminUserService.resetPassword(any(), any()) }
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `onSave — a mismatched confirmation never reaches the admin service`() = runTest {
        userRepository.givenUsers(appUser(id = "user-9"))
        val vm = viewModel(isSelf = false, userId = "user-9")

        vm.onNewPasswordChange("Abcdef12")
        vm.onConfirmPasswordChange("Abcdef13")
        vm.onSave()

        coVerify(exactly = 0) { adminUserService.resetPassword(any(), any()) }
    }

    @Test
    fun `onSave — changing your own password requires the current one first`() = runTest {
        session.setCurrentUser(appUser(id = "user-1"))
        val vm = viewModel(isSelf = true)

        vm.onNewPasswordChange("Abcdef12")
        vm.onConfirmPasswordChange("Abcdef12")
        vm.onSave()

        assertFalse(vm.state.value.isValid)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `typing in any field clears a previous error`() = runTest {
        userRepository.givenUsers(appUser(id = "user-9"))
        val vm = viewModel(isSelf = false, userId = "user-9")

        vm.onNewPasswordChange("Abcdef12")

        assertNull(vm.state.value.errorMessage)
        assertNull(vm.state.value.errorRes)
    }

    @Test
    fun `clearError — wipes both the server message and the fallback resource`() = runTest {
        userRepository.givenUsers(appUser(id = "user-9"))
        val vm = viewModel(isSelf = false, userId = "user-9")

        vm.clearError()

        assertNull(vm.state.value.errorMessage)
        assertNull(vm.state.value.errorRes)
    }
}
