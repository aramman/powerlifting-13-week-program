package com.aram.benchpress13week

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aram.benchpress13week.ui.navigation.BenchApp
import com.aram.benchpress13week.ui.theme.BenchPressTheme
import com.aram.benchpress13week.viewmodel.BenchViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BenchPressTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val vm: BenchViewModel = viewModel()
                    BenchApp(vm)
                }
            }
        }
    }
}
