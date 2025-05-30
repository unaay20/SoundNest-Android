package com.example.soundnest_android.ui.stats

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

class StatsGraphicsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHART_TYPE = "chart_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val chartType = intent.getIntExtra(EXTRA_CHART_TYPE, 1)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when (chartType) {
                        1 -> PieChartDemo()
                        2 -> LineChartDemo()
                        3 -> BarChartDemo()
                        else -> Text("Tipo desconocido", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PieChartDemo() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text("Gráfica de Pastel", modifier = Modifier.padding(bottom = 16.dp))
        Canvas(modifier = Modifier.size(200.dp)) {
            val total = 100f
            val slice1 = 40f / total * 360f   // 40%
            drawArc(
                color = Color(0xFF2196F3),
                startAngle = 0f,
                sweepAngle = slice1,
                useCenter = true
            )
            drawArc(
                color = Color(0xFFBBDEFB),
                startAngle = slice1,
                sweepAngle = 360f - slice1,
                useCenter = true
            )
        }
    }
}

@Composable
fun LineChartDemo() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text("Gráfica de Líneas", modifier = Modifier.padding(bottom = 16.dp))
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
            // Datos hardcodeados
            val pts = listOf(10f, 25f, 15f)
            val w = size.width
            val h = size.height
            val dx = w / (pts.size - 1)
            pts.mapIndexed { i, y ->
                Offset(x = i * dx, y = h - (y / 30f * h))
            }.zipWithNext { (x1,y1), (x2,y2) ->
                drawLine(
                    color = Color(0xFFF44336),
                    start = Offset(x1, y1),
                    end   = Offset(x2, y2),
                    strokeWidth = 4f
                )
            }
        }
    }
}

@Composable
fun BarChartDemo() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text("Gráfica de Barras", modifier = Modifier.padding(bottom = 16.dp))
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
            val vals = listOf(5f, 12f, 8f)
            val w = size.width
            val h = size.height
            val barWidth = w / (vals.size * 2f)
            vals.forEachIndexed { i, v ->
                val left = i * 2 * barWidth + barWidth / 2
                val top  = h - (v / 15f * h)
                drawRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(barWidth, h - top)
                )
            }
        }
    }
}