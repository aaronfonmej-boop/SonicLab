package com.soniclab3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.soniclab3d.ui.SonicLabApp
import com.soniclab3d.ui.theme.SonicLabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestFastestDisplayMode()
        setContent {
            SonicLabTheme {
                SonicLabApp()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun requestFastestDisplayMode() {
        val targetDisplay = windowManager.defaultDisplay
        val current = targetDisplay.mode
        val preferred = targetDisplay.supportedModes
            ?.filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
            ?.maxByOrNull { it.refreshRate }
            ?: return
        window.attributes = window.attributes.apply {
            preferredDisplayModeId = preferred.modeId
        }
    }
}
