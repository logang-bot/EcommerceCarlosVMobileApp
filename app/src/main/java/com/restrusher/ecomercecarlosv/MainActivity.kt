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
import androidx.compose.ui.Modifier
import com.restrusher.ecomercecarlosv.presentation.navigation.AppNavigation
import com.restrusher.ecomercecarlosv.presentation.navigation.AppViewModel
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        // Keep splash visible until the session-restore check completes so users
        // with an active session never see a flash of the Login screen on re-launch.
        splashScreen.setKeepOnScreenCondition { !appViewModel.isLoaded.value }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcomerceCarlosVTheme {
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
