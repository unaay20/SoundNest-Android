package com.example.soundnest_android.ui.stats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import com.example.soundnest_android.R


class StatsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        findViewById<ComposeView>(R.id.composeStatsView).setContent {
            MaterialTheme {
                Surface {
                    StatsScreenAll()
                }
            }
        }
    }
}

