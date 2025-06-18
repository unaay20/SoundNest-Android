package com.example.soundnest_android.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
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
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.services.VisitService
import com.example.soundnest_android.restful.utils.ApiResult

@Composable
fun StatsScreenAll() {
    val context = LocalContext.current

    val tokenProvider = remember { SharedPrefsTokenProvider(context) }
    val visitService = remember { VisitService(RestfulRoutes.getBaseUrl(), tokenProvider) }
    val userId: Int = tokenProvider.getUserId()

    var labelsUser by remember { mutableStateOf<List<String>>(emptyList()) }
    var valuesUser by remember { mutableStateOf<List<Float>>(emptyList()) }
    var isLoadingUser by remember { mutableStateOf(true) }
    var errorUser by remember { mutableStateOf<String?>(null) }

    var labelsGlobal by remember { mutableStateOf<List<String>>(emptyList()) }
    var valuesGlobal by remember { mutableStateOf<List<Float>>(emptyList()) }
    var isLoadingGlobal by remember { mutableStateOf(true) }
    var errorGlobal by remember { mutableStateOf<String?>(null) }

    var labelsGenres by remember { mutableStateOf<List<String>>(emptyList()) }
    var valuesGenres by remember { mutableStateOf<List<Float>>(emptyList()) }
    var isLoadingGenres by remember { mutableStateOf(true) }
    var errorGenres by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (userId >= 0) {
            when (val result = visitService.getTopSongsByUser(userId, limit = 5)) {
                is ApiResult.Success -> {
                    val dataList = result.data.orEmpty()
                    labelsUser = dataList.map { it.songName }
                    valuesUser = dataList.map { it.totalPlayCount.toFloat() }
                }

                is ApiResult.HttpError -> {
                    errorUser = context.getString(R.string.msg_http_error_user_songs, result.code)
                }

                is ApiResult.NetworkError -> {
                    errorUser = context.getString(R.string.msg_network_error)
                }

                is ApiResult.UnknownError -> {
                    errorUser = context.getString(
                        R.string.msg_unknown_error,
                        result.exception?.localizedMessage ?: context.getString(R.string.msg_unexpected_error)
                    )
                }
            }
        } else {
            errorUser = context.getString(R.string.msg_user_not_authenticated)
        }
        isLoadingUser = false

        when (val result = visitService.getTopSongsGlobal(limit = 5)) {
            is ApiResult.Success -> {
                val dataList = result.data.orEmpty()
                labelsGlobal = dataList.map { it.songName }
                valuesGlobal = dataList.map { it.totalPlayCount.toFloat() }
            }

            is ApiResult.HttpError -> {
                errorGlobal = context.getString(
                    R.string.msg_http_error_global_songs,
                    result.code
                )
            }

            is ApiResult.NetworkError -> {
                errorGlobal = context.getString(R.string.msg_network_error_check_connection)
            }

            is ApiResult.UnknownError -> {
                errorGlobal = context.getString(
                    R.string.msg_unknown_error_generic,
                    result.exception?.localizedMessage
                        ?: context.getString(R.string.msg_unexpected_error_generic)
                )
            }
        }
        isLoadingGlobal = false

        when (val result = visitService.getTopGenresGlobal(limit = 5)) {
            is ApiResult.Success -> {
                val dataList = result.data.orEmpty()
                labelsGenres = dataList.map { it.genreName }
                valuesGenres = dataList.map { it.totalPlayCount.toFloat() }
            }

            is ApiResult.HttpError -> {
                errorGenres = context.getString(
                    R.string.msg_http_error_global_genres,
                    result.code
                )
            }

            is ApiResult.NetworkError -> {
                errorGenres = context.getString(R.string.msg_network_error_check_connection)
            }

            is ApiResult.UnknownError -> {
                errorGenres = context.getString(
                    R.string.msg_unknown_error_generic,
                    result.exception?.localizedMessage
                        ?: context.getString(R.string.msg_unexpected_error_generic)
                )
            }
        }
        isLoadingGenres = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = context.getString(R.string.title_top_user_songs),
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            color = Color.Unspecified
        )
        when {
            isLoadingUser -> LoadingPlaceholder()
            errorUser != null -> ErrorPlaceholder(message = errorUser!!)
            else -> BarChartCompose(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                labels = labelsUser,
                values = valuesUser
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = context.getString(R.string.title_top_global_songs),
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        when {
            isLoadingGlobal -> LoadingPlaceholder()
            errorGlobal != null -> ErrorPlaceholder(message = errorGlobal!!)
            else -> BarChartCompose(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                labels = labelsGlobal,
                values = valuesGlobal
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = context.getString(R.string.title_top_global_genres),
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        when {
            isLoadingGenres -> LoadingPlaceholder()
            errorGenres != null -> ErrorPlaceholder(message = errorGenres!!)
            else -> BarChartCompose(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                labels = labelsGenres,
                values = valuesGenres
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Red)
    }
}

@Composable
fun BarChartCompose(
    modifier: Modifier = Modifier,
    labels: List<String>,
    values: List<Float>
) {
    if (labels.isEmpty() || values.isEmpty() || labels.size != values.size) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = LocalContext.current.getString(R.string.msg_no_data), fontSize = 16.sp)
        }
        return
    }

    val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val yAxisMargin = 40f
        val chartHeight = canvasHeight - 30f
        val numTicks = 5
        val tickSpacing = chartHeight / numTicks

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 20f
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        for (i in 0..numTicks) {
            val y = chartHeight - i * tickSpacing
            val labelValue = (i * maxValue / numTicks).toInt().toString()

            drawContext.canvas.nativeCanvas.drawText(
                labelValue,
                yAxisMargin - 8f,
                y + 6f,
                textPaint
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                strokeWidth = 1f,
                start = Offset(yAxisMargin, y),
                end = Offset(canvasWidth, y)
            )
        }

        val count = values.size
        val usableWidth = canvasWidth - yAxisMargin
        val barSlotWidth = usableWidth / (count * 2f)
        val barWidth = barSlotWidth

        values.forEachIndexed { index, value ->
            val barColor = if (index % 2 == 0) Color(0xFFFF9800) else Color(0xFF9C27B0)
            val normalizedHeight = (value / maxValue) * chartHeight
            val left = yAxisMargin + index * (barSlotWidth * 2) + (barSlotWidth / 2)
            val top = chartHeight - normalizedHeight
            val right = left + barWidth
            val bottom = chartHeight

            drawRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, normalizedHeight)
            )

            val numberPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 22f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            val xPos = left + barWidth / 2
            val yPos = if (normalizedHeight < 20f) bottom - normalizedHeight - 4f else top - 4f
            drawContext.canvas.nativeCanvas.drawText(
                value.toInt().toString(),
                xPos,
                yPos,
                numberPaint
            )

            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val labelY = canvasHeight - 4f
            drawContext.canvas.nativeCanvas.drawText(labels[index], xPos, labelY, labelPaint)
        }

        drawLine(
            color = Color.Black,
            strokeWidth = 2f,
            start = Offset(yAxisMargin, chartHeight),
            end = Offset(canvasWidth, chartHeight)
        )
    }
}
