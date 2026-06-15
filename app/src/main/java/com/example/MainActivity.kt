package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.ui.MainLayout
import com.example.ui.MarketplaceViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MarketplaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) } // Dark mode by default as requested
            
            MainLayout(
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = { isDarkTheme = !isDarkTheme }
            )
        }
    }
}
