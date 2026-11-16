package com.restrusher.ecomercecarlosv.screenshots

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import com.restrusher.ecomercecarlosv.MainActivity
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.data.prefs.ThemeManager
import com.restrusher.ecomercecarlosv.domain.model.AppUser
import com.restrusher.ecomercecarlosv.domain.model.ThemeMode
import com.restrusher.ecomercecarlosv.screenshots.data.DemoCatalog
import com.restrusher.ecomercecarlosv.screenshots.data.DemoClientes
import com.restrusher.ecomercecarlosv.screenshots.data.DemoDataSeeder
import com.restrusher.ecomercecarlosv.screenshots.data.DemoIds
import com.restrusher.ecomercecarlosv.screenshots.data.DemoPedidos
import com.restrusher.ecomercecarlosv.screenshots.fakes.ScreenshotSessionManager
import com.restrusher.ecomercecarlosv.screenshots.util.DeviceChrome
import com.restrusher.ecomercecarlosv.screenshots.util.ScreenRobot
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Captures the app's key screens for the portfolio, in light and dark, with no backend.
 *
 * Each test seeds the in-memory database with the demo data, launches the real `MainActivity`
 * once per theme and walks a flow by tapping what is on screen. See
 * `docs/features/testing.md` → "Portfolio screenshots" for how to run it and where the PNGs land.
 */
@HiltAndroidTest
class PortfolioScreenshotTest {

    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    @get:Rule(order = 2)
    val testName = TestName()

    @Inject lateinit var seeder: DemoDataSeeder
    @Inject lateinit var session: ScreenshotSessionManager
    @Inject lateinit var themeManager: ThemeManager

    private val rodriguez = DemoCatalog.mercadoName(DemoIds.MERCADO_RODRIGUEZ)
    private val rosaPedidoDate = SimpleDateFormat("dd MMM yyyy", Locale("es"))
        .format(Date(DemoPedidos.createdAt(DemoIds.PEDIDO_ROSA_PARCIAL)))

    @Before
    fun setUp() {
        hilt.inject()
        runBlocking { seeder.seed() }
    }

    @Test
    fun login() = signedOut {
        waitForText(string(R.string.login_title))
        capture("00_login")
    }

    @Test
    fun home_tabs() = signedIn {
        capture("01_mercados")
        tapText(R.string.nav_productos)
        waitForText(DemoCatalog.productoName(DemoIds.PRODUCTO_CAFE))
        capture("02_catalogo")
        tapText(R.string.nav_reporte)
        capture("03_reporte")
    }

    @Test
    fun detalle_mercado() = signedIn {
        longPressText(rodriguez)
        tapText(R.string.mercados_accion_ver_detalles)
        capture("04_detalle_mercado")
    }

    @Test
    fun cliente_y_pedidos() = signedIn {
        tapText(rodriguez)
        waitForText(DemoClientes.ROSA_NAME)
        capture("05_clientes")
        tapText(DemoClientes.ROSA_NAME)
        waitForText(rosaPedidoDate)
        capture("06_detalle_cliente")
        scrollToAndTapText(rosaPedidoDate)
        capture("07_detalle_pedido")
    }

    @Test
    fun nuevo_pedido() = signedIn {
        tapText(rodriguez)
        tapText(DemoClientes.ROSA_NAME)
        tapDescription(R.string.pedidos_nuevo)
        tapText(DemoCatalog.productoName(DemoIds.PRODUCTO_ARROZ))
        tapText(DemoCatalog.productoName(DemoIds.PRODUCTO_ACEITE))
        capture("08_nuevo_pedido")
    }

    @Test
    fun busqueda() = signedIn {
        tapDescription(R.string.common_buscar)
        typeIntoFirstField("an")
        waitForText("Juan Mamani")
        capture("09_busqueda")
    }

    @Test
    fun lista_negra() = signedIn {
        tapText(R.string.mercados_lista_negra_title)
        waitForText("Pedro Alanoca")
        capture("10_lista_negra")
    }

    @Test
    fun perfil() = signedIn {
        tapText("CV")
        waitForText(DemoCatalog.owner.name)
        capture("11_perfil")
    }

    @Test
    fun sincronizacion() {
        runBlocking { seeder.seedPendingSync() }
        signedIn {
            tapDescription(R.string.sinc_title)
            waitForText("Luis Condori")
            capture("12_sincronizacion")
        }
    }

    private fun signedIn(flow: ScreenRobot.() -> Unit) = inEachTheme(DemoCatalog.owner) {
        waitForText(rodriguez)
        flow()
    }

    private fun signedOut(flow: ScreenRobot.() -> Unit) = inEachTheme(user = null, flow)

    /** Runs [flow] on a fresh activity in light, then again in dark. */
    private fun inEachTheme(user: AppUser?, flow: ScreenRobot.() -> Unit) {
        for (theme in listOf(ThemeMode.LIGHT, ThemeMode.DARK)) {
            DeviceChrome.applyNightMode(dark = theme == ThemeMode.DARK)
            themeManager.setTheme(theme)
            if (user == null) session.clearSession() else session.setCurrentUser(user)
            ActivityScenario.launch(MainActivity::class.java).use {
                ScreenRobot(compose, prefix = theme.name.lowercase()).runCapturingFailure(flow)
            }
        }
    }

    /** On failure, saves `<theme>_FAILED_<test>.png` showing where the flow got stuck, then rethrows. */
    private fun ScreenRobot.runCapturingFailure(flow: ScreenRobot.() -> Unit) {
        try {
            flow()
        } catch (failure: Throwable) {
            runCatching { capture("FAILED_${testName.methodName}") }
            throw failure
        }
    }

    companion object {
        private var originalNightMode = "auto"

        @JvmStatic
        @BeforeClass
        fun stageDevice() {
            originalNightMode = DeviceChrome.currentNightMode()
            DeviceChrome.enterDemoMode()
        }

        @JvmStatic
        @AfterClass
        fun restoreDevice() {
            DeviceChrome.exitDemoMode()
            DeviceChrome.setNightMode(originalNightMode)
        }
    }
}
