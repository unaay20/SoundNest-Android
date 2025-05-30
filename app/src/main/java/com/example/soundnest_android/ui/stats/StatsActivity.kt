package com.example.soundnest_android.ui.stats

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.soundnest_android.R

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        findViewById<Button>(R.id.btnChart1).setOnClickListener {
            openChart(1)
        }
        findViewById<Button>(R.id.btnChart2).setOnClickListener {
            openChart(2)
        }
        findViewById<Button>(R.id.btnChart3).setOnClickListener {
            openChart(3)
        }
    }

    private fun openChart(type: Int) {
        Intent(this, StatsGraphicsActivity::class.java).also { intent ->
            intent.putExtra("chart_type", type)
            startActivity(intent)
        }
    }
}
