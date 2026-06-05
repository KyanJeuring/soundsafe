package com.example.soundsafe.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AnalyticsScreen(
    soundLog: List<SoundRecord>
) {
    val avgDb = if (soundLog.isNotEmpty()) {
        soundLog.mapNotNull { it.dbLevel.toDoubleOrNull() }.average()
    } else 0.0

    val maxDb = if (soundLog.isNotEmpty()) {
        soundLog.mapNotNull { it.dbLevel.toDoubleOrNull() }.maxOrNull() ?: 0.0
    } else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            AnalyticsHeader(soundLog = soundLog)
        }

        item {
            StatsRow(avgDb = avgDb, maxDb = maxDb)
        }

        item {
            Text(
                text = "Detailed Log",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(soundLog) { record ->
            SoundLogItem(record)
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun AnalyticsHeader(soundLog: List<SoundRecord>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Today's Exposure", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        SoundLineGraph(
            soundLog = soundLog,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
fun SoundLineGraph(
    soundLog: List<SoundRecord>,
    modifier: Modifier = Modifier
) {
    val data = soundLog.reversed().mapNotNull { it.dbLevel.toFloatOrNull() }

    // Y-Axis Scaling: max(100f, maxRecord + 10f)
    val maxRecord = data.maxOrNull() ?: 0f
    val maxYThreshold = maxOf(100f, maxRecord + 10f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40f
        val chartWidth = width - padding
        val chartHeight = height - padding

        // Draw Axes
        drawLine(
            color = Color.Black,
            start = Offset(padding, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Black,
            start = Offset(padding, 0f),
            end = Offset(padding, chartHeight),
            strokeWidth = 2f
        )

        // Draw Warning Line (85dB)
        val warningY = chartHeight - ((85f / maxYThreshold) * chartHeight)
        drawLine(
            color = Color.Red,
            start = Offset(padding, warningY),
            end = Offset(width, warningY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )

        drawContext.canvas.nativeCanvas.drawText(
            "Warning (85dB)",
            padding + 10f,
            warningY - 10f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.RED
                textSize = 30f
            }
        )

        // Plot Data
        if (data.isNotEmpty()) {
            val xStep = if (data.size > 1) chartWidth / (data.size - 1) else 0f
            val path = Path()

            data.forEachIndexed { index, db ->
                val x = padding + (index * xStep)
                val y = chartHeight - ((db / maxYThreshold) * chartHeight)

                // 1. Path Connection
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // 2. Data Nodes (Dots)
                drawCircle(
                    color = Color.Blue,
                    radius = 8f,
                    center = Offset(x, y)
                )
            }

            if (data.size > 1) {
                drawPath(
                    path = path,
                    color = Color.Blue.copy(alpha = 0.7f),
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}

@Composable
fun StatsRow(avgDb: Double, maxDb: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(label = "AVG VOLUME", value = "%.1f".format(avgDb), modifier = Modifier.weight(1f))
        StatCard(label = "MAX LEVEL", value = "%.1f".format(maxDb), modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = "$value dB", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SoundLogItem(record: SoundRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = record.time)
        Text(text = "${record.dbLevel} dB SPL")
    }
}
