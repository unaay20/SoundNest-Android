package com.example.soundnest_android.ui.stats


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
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

@Composable
fun StatsScreenAll() {
    val context = LocalContext.current

    val tokenProvider = remember { SharedPrefsTokenProvider(context) }
    val visitService = remember {
        VisitService(RestfulRoutes.getBaseUrl(), tokenProvider)
    }
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
            when (val result: ApiResult<List<SongPlayCount>?> =
                visitService.getTopSongsByUser(userId, limit = 10)
            ) {
                is ApiResult.Success -> {
                    val dataList = result.data.orEmpty()
                    labelsUser = dataList.map { it.songName }
                    valuesUser = dataList.map { it.totalPlayCount.toFloat() }
                }

                is ApiResult.HttpError -> TODO()
                is ApiResult.NetworkError -> TODO()
                is ApiResult.UnknownError -> TODO()
            }
        } else {
            errorUser = "Usuario no autenticado"
        }
        isLoadingUser = false

        when (val result: ApiResult<List<SongPlayCount>?> =
            visitService.getTopSongsGlobal(limit = 10)
        ) {
            is ApiResult.Success -> {
                val dataList = result.data.orEmpty()
                labelsGlobal = dataList.map { it.songName }
                valuesGlobal = dataList.map { it.totalPlayCount.toFloat() }
            }

            is ApiResult.HttpError -> TODO()
            is ApiResult.NetworkError -> TODO()
            is ApiResult.UnknownError -> TODO()
        }
        isLoadingGlobal = false

        when (val result: ApiResult<List<GenrePlayCount>?> =
            visitService.getTopGenresGlobal(limit = 10)
        ) {
            is ApiResult.Success -> {
                val dataList = result.data.orEmpty()
                labelsGenres = dataList.map { it.genreName }
                valuesGenres = dataList.map { it.totalPlayCount.toFloat() }
            }

            is ApiResult.HttpError -> TODO()
            is ApiResult.NetworkError -> TODO()
            is ApiResult.UnknownError -> TODO()
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
            text = "Top Canciones (Usuario)",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        when {
            isLoadingUser -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorUser != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorUser!!, color = Color.Red)
                }
            }

            else -> {
                BarChartCompose(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    labels = labelsUser,
                    values = valuesUser
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Top Canciones (Global)",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        when {
            isLoadingGlobal -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorGlobal != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorGlobal!!, color = Color.Red)
                }
            }

            else -> {
                BarChartCompose(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    labels = labelsGlobal,
                    values = valuesGlobal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Top Géneros (Global)",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        when {
            isLoadingGenres -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorGenres != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorGenres!!, color = Color.Red)
                }
            }

            else -> {
                BarChartCompose(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    labels = labelsGenres,
                    values = valuesGenres
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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

            drawRect(
                color = Color(0xFF4CAF50),
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
