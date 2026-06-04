package com.restrusher.ecomercecarlosv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.restrusher.ecomercecarlosv.presentation.navigation.AppNavigation
import com.restrusher.ecomercecarlosv.ui.theme.EcomerceCarlosVTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcomerceCarlosVTheme {
                AppNavigation()
            }
        }
    }
}
