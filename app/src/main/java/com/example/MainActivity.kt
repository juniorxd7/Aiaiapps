package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.SettingsRepository
import com.example.ui.SorteiaScreen
import com.example.ui.SorteiaViewModel
import com.example.ui.theme.SorteiaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SorteiaViewModel by viewModels {
        SorteiaViewModel.provideFactory(
            owner = this,
            settingsRepository = SettingsRepository(applicationContext),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SorteiaTheme {
                SorteiaScreen(viewModel = viewModel)
            }
        }
    }
}
