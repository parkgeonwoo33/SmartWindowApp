package com.example.smartwindowapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.smartwindowapp.ui.SmartWindowScreen
import com.example.smartwindowapp.ui.theme.SmartWindowAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartWindowAppTheme {
                Surface(modifier = Modifier) {
                    SmartWindowScreen()
                }
            }
        }
    }
}
