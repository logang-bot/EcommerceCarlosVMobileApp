package com.restrusher.ecomercecarlosv

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.ecomercecarlosv.data.prefs.ThemeManager
import com.restrusher.ecomercecarlosv.presentation.navigation.AppNavigation
import com.restrusher.ecomercecarlosv.presentation.navigation.AppViewModel
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val appViewModel: AppViewModel by viewModels()

    @Inject lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !appViewModel.isLoaded.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeManager.themeMode.collectAsStateWithLifecycle()
            EcomerceCarlosVTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Box(modifier = Modifier.systemBarsPadding()) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}
