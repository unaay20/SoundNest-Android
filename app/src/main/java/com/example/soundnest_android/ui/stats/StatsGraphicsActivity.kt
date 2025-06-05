// /app/src/main/java/com/example/soundnest_android/ui/stats/StatsGraphicsActivity.kt

package com.example.soundnest_android.ui.stats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.visits.GenrePlayCount
import com.example.soundnest_android.restful.models.visits.SongPlayCount
import com.example.soundnest_android.restful.services.VisitService
import com.example.soundnest_android.restful.utils.ApiResult

class StatsGraphicsActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CHART_TYPE = "chart_type"
        // 1 -> Top canciones de usuario
        // 2 -> Top canciones globales
        // 3 -> Top géneros globales
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val chartType = intent.getIntExtra(EXTRA_CHART_TYPE, 1)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StatsScreen(chartType = chartType)
                }
            }
        }
    }
}


@Composable
fun StatsScreen(chartType: Int) {
    // --- Estados de Compose ---
    var labels by remember { mutableStateOf<List<String>>(emptyList()) }
    var values by remember { mutableStateOf<List<Float>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val tokenProvider = remember { SharedPrefsTokenProvider(context) }

    val visitService = remember {
        VisitService(RestfulRoutes.getBaseUrl(), tokenProvider)
    }

    val userId: Int = tokenProvider.getUserId()

    LaunchedEffect(key1 = chartType) {
        isLoading = true
        errorMsg = null

        when (chartType) {
            1 -> {
                when (val result: ApiResult<List<SongPlayCount>?> =
                    visitService.getTopSongsByUser(userId, limit = 10)) {
                    is ApiResult.Success -> {
                        val dataList = result.data.orEmpty()
                        labels = dataList.map { it.songName }
                        values = dataList.map { it.totalPlayCount.toFloat() }
                    }

                    is ApiResult.HttpError -> TODO()
                    is ApiResult.NetworkError -> TODO()
                    is ApiResult.UnknownError -> TODO()
                }
            }

            2 -> {
                when (val result: ApiResult<List<SongPlayCount>?> =
                    visitService.getTopSongsGlobal(limit = 10)) {
                    is ApiResult.Success -> {
                        val dataList = result.data.orEmpty()
                        labels = dataList.map { it.songName }
                        values = dataList.map { it.totalPlayCount.toFloat() }
                    }

                    is ApiResult.HttpError -> TODO()
                    is ApiResult.NetworkError -> TODO()
                    is ApiResult.UnknownError -> TODO()
                }
            }

            3 -> {
                when (val result: ApiResult<List<GenrePlayCount>?> =
                    visitService.getTopGenresGlobal(limit = 10)) {
                    is ApiResult.Success -> {
                        val dataList = result.data.orEmpty()
                        labels = dataList.map { it.genreName }
                        values = dataList.map { it.totalPlayCount.toFloat() }
                    }

                    is ApiResult.HttpError -> TODO()
                    is ApiResult.NetworkError -> TODO()
                    is ApiResult.UnknownError -> TODO()
                }
            }

            else -> {
                errorMsg = "Tipo de gráfica no soportado"
            }
        }

        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Cargando datos...", fontSize = 18.sp)
                }
            }

            errorMsg != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Error: $errorMsg", fontSize = 16.sp, color = Color.Red)
                }
            }

            else -> {
                BarChartCompose(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp),
                    labels = labels,
                    values = values
                )
            }
        }
    }
}

@Composable
fun BarChartCompose(
    modifier: Modifier = Modifier,
    labels: List<String>,
    values: List<Float>
) {
    if (labels.isEmpty() || values.isEmpty() || labels.size != values.size) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No hay datos para mostrar", fontSize = 16.sp)
        }
        return
    }

    val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val labelMargin = 30f

        val count = values.size

        val barSlotWidth = canvasWidth / (count * 2f)
        val barWidth = barSlotWidth

        values.forEachIndexed { index, value ->
            val left = index * (barSlotWidth * 2) + (barSlotWidth / 2)
            val normalizedHeight = (value / maxValue) * (canvasHeight - labelMargin)
            val top = canvasHeight - labelMargin - normalizedHeight
            val right = left + barWidth
            val bottom = canvasHeight - labelMargin

            drawRect(
                color = Color(0xFF4CAF50), // verde
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, normalizedHeight)
            )

            val label = labels[index]
            drawContext.canvas.nativeCanvas.apply {
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val xPos = left + barWidth / 2
                val yPos = canvasHeight - 4f
                drawText(label, xPos, yPos, textPaint)
            }
        }

        drawLine(
            color = Color.Black,
            strokeWidth = 2f,
            start = Offset(0f, canvasHeight - labelMargin),
            end = Offset(canvasWidth, canvasHeight - labelMargin)
        )
    }
}
