package com.restrusher.partypuzz.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.restrusher.partypuzz.navigation.HomeNavigation
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PartyPuzzTheme {
                HomeNavigation()
            }
        }
    }
}