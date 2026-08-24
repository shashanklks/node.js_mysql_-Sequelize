package com.khatabook.clone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.khatabook.clone.navigation.KhatabookNavGraph
import com.khatabook.clone.ui.theme.KhatabookTheme
import com.khatabook.clone.ui.theme.KhataTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KhatabookTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = KhataTheme.colors.screen) {
                    KhatabookNavGraph()
                }
            }
        }
    }
}
