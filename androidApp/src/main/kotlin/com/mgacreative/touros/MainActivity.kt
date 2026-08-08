package com.mgacreative.touros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            enableEdgeToEdge()
        } catch (_: Exception) {}
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}