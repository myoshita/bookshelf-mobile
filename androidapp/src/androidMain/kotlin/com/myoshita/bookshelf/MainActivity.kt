package com.myoshita.bookshelf

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.myoshita.bookshelf.theme.surfaceDark
import com.myoshita.bookshelf.theme.surfaceLight
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.vinceglb.filekit.core.FileKit
import org.koin.android.ext.koin.androidContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val systemBarStyle = when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> SystemBarStyle.light(
                surfaceLight.toArgb(),
                surfaceLight.toArgb()
            )
            Configuration.UI_MODE_NIGHT_YES -> SystemBarStyle.dark(surfaceDark.toArgb())
            else -> error("Illegal State, current mode is $currentNightMode")
        }
        enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle,
        )
        Napier.base(DebugAntilog())
        FileKit.init(this)

        setContent {
            App(
                koinAppDeclaration = {
                    androidContext(this@MainActivity.applicationContext)
                }
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val context = LocalContext.current
    App(
        koinAppDeclaration = {
            androidContext(context)
        }
    )
}
