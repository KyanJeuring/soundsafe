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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsScreen(
    soundLog: List<SoundRecord>,
    selectedTimeFrame: String,
    onTimeFrameSelected: (String) -> Unit
) {
    val avgDb = if (soundLog.isNotEmpty()) {
        soundLog.mapNotNull { it.dbLevel.toDoubleOrNull() }.average()
    } else 0.0

    val maxDb = if (soundLog.isNotEmpty()) {
        soundLog.mapNotNull { it.dbLevel.toDoubleOrNull() }.maxOrNull() ?: 0.0
    } else 0.0

    val minDb = if (soundLog.isNotEmpty()) {
        soundLog.mapNotNull { it.dbLevel.toDoubleOrNull() }.minOrNull() ?: 0.0
    } else 0.0

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            TimeFrameSelector(
                selectedTimeFrame = selectedTimeFrame,
                onTimeFrameSelected = onTimeFrameSelected
            )
        }

        item {
            val title = when (selectedTimeFrame) {
                "Daily" -> "Today's Exposure"
                "Weekly" -> "This Week's Exposure"
                "Monthly" -> {
                    val calendar = Calendar.getInstance()
                    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                    "$monthName's Exposure"
                }
                else -> "Exposure Analysis"
            }
            AnalyticsHeader(title = title, soundLog = soundLog, selectedTimeFrame = selectedTimeFrame, avgDb = avgDb, maxDb = maxDb, minDb = minDb)
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
fun TimeFrameSelector(
    selectedTimeFrame: String,
    onTimeFrameSelected: (String) -> Unit
) {
    val options = listOf("Daily", "Weekly", "Monthly")
    val selectedIndex = options.indexOf(selectedTimeFrame).coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        containerColor = Color.Transparent,
        divider = {}
    ) {
        options.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTimeFrameSelected(title) },
                text = { Text(text = title, style = MaterialTheme.typography.labelLarge) }
            )
        }
    }
}

@Composable
fun AnalyticsHeader(
    title: String,
    soundLog: List<SoundRecord>,
    selectedTimeFrame: String,
    avgDb: Double,
    maxDb: Double,
    minDb: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        SoundLineGraph(
            soundLog = soundLog,
            selectedTimeFrame = selectedTimeFrame,
            avgDb = avgDb,
            maxDb = maxDb,
            minDb = minDb,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
    }
}

@Composable
fun SoundLineGraph(
    soundLog: List<SoundRecord>,
    selectedTimeFrame: String,
    avgDb: Double,
    maxDb: Double,
    minDb: Double,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = Color.Gray, fontSize = 10.sp)

    val dataRecords = soundLog.reversed()
    val dataValues = dataRecords.mapNotNull { it.dbLevel.toFloatOrNull() }

    val maxRecord = dataValues.maxOrNull() ?: 0f
    val maxYThreshold = maxOf(100f, maxRecord + 10f)

    Canvas(modifier = modifier) {
        val widthPx = size.width
        val heightPx = size.height
        val leftPadding = 80f
        val bottomPadding = 60f
        val chartWidth = widthPx - leftPadding
        val chartHeight = heightPx - bottomPadding

        // Draw Axes (Subtle Grey)
        drawLine(
            color = Color.LightGray,
            start = Offset(leftPadding, chartHeight),
            end = Offset(widthPx, chartHeight),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.LightGray,
            start = Offset(leftPadding, 0f),
            end = Offset(leftPadding, chartHeight),
            strokeWidth = 1f
        )

        // Y-Axis Labels (Max, Avg, Min)
        val yLabels = listOf(
            maxDb.toFloat() to "%.0f".format(maxDb),
            avgDb.toFloat() to "%.0f".format(avgDb),
            minDb.toFloat() to "%.0f".format(minDb)
        )
        yLabels.forEach { (value, label) ->
            val yPos = chartHeight - ((value / maxYThreshold) * chartHeight)
            val textLayout = textMeasurer.measure(label, style = labelStyle)
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(leftPadding - textLayout.size.width - 10f, yPos - textLayout.size.height / 2f)
            )
            drawLine(Color.LightGray.copy(alpha = 0.2f), Offset(leftPadding, yPos), Offset(widthPx, yPos), 1f)
        }

        // X-Axis Labels
        when (selectedTimeFrame) {
            "Daily" -> {
                val timeLabels = listOf("12 AM", "3 AM", "6 AM", "9 AM", "12 PM", "3 PM", "6 PM", "9 PM", "12 AM")
                timeLabels.forEachIndexed { index, label ->
                    val xPos = leftPadding + (index * (chartWidth / (timeLabels.size - 1)))
                    val textLayout = textMeasurer.measure(label, style = labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(xPos - textLayout.size.width / 2f, chartHeight + 10f)
                    )
                }
            }
            "Weekly" -> {
                val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                dayLabels.forEachIndexed { index, label ->
                    val xPos = leftPadding + (index * (chartWidth / 6f))
                    val textLayout = textMeasurer.measure(label, style = labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(xPos - textLayout.size.width / 2f, chartHeight + 10f)
                    )
                }
            }
            "Monthly" -> {
                val calendar = Calendar.getInstance()
                val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                // Anti-clutter: Show labels every 5 days + last day
                val labelsToShow = (1..totalDaysInMonth step 5).toMutableList()
                if (labelsToShow.last() != totalDaysInMonth) {
                    labelsToShow.add(totalDaysInMonth)
                }

                labelsToShow.forEach { day ->
                    val xPos = leftPadding + ((day - 1) / (totalDaysInMonth - 1).toFloat()) * chartWidth
                    val textLayout = textMeasurer.measure(day.toString(), style = labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(xPos - textLayout.size.width / 2f, chartHeight + 10f)
                    )
                }
            }
        }

        // Draw Warning Line (85dB)
        val warningY = chartHeight - ((85f / maxYThreshold) * chartHeight)
        drawLine(
            color = Color.Red.copy(alpha = 0.3f),
            start = Offset(leftPadding, warningY),
            end = Offset(widthPx, warningY),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )

        // Plot Data
        if (dataValues.isNotEmpty()) {
            val path = Path()
            val points = mutableListOf<Offset>()

            dataRecords.forEach { record ->
                val db = record.dbLevel.toFloatOrNull() ?: 0f
                val y = chartHeight - ((db / maxYThreshold) * chartHeight)

                val x = when (selectedTimeFrame) {
                    "Daily" -> {
                        val timeParts = record.time.split(":")
                        if (timeParts.size >= 2) {
                            val hours = timeParts[0].toInt()
                            val minutes = timeParts[1].toInt()
                            val totalMinutes = hours * 60 + minutes
                            leftPadding + (totalMinutes.toFloat() / 1440f) * chartWidth
                        } else leftPadding
                    }
                    "Weekly" -> {
                        // For Weekly, we mock day mapping since SoundRecord only has time.
                        // In a real app, SoundRecord would have a Date.
                        val dayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
                        leftPadding + (dayIndex / 6f) * chartWidth
                    }
                    "Monthly" -> {
                        val calendar = Calendar.getInstance()
                        val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                        leftPadding + ((dayOfMonth - 1) / (totalDaysInMonth - 1).toFloat()) * chartWidth
                    }
                    else -> leftPadding + (dataRecords.indexOf(record).toFloat() / (dataValues.size - 1).coerceAtLeast(1) * chartWidth)
                }
                points.add(Offset(x, y))
            }

            if (points.size > 1) {
                points.sortBy { it.x }

                path.moveTo(points[0].x, points[0].y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                    val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                    path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                }

                val fillPath = android.graphics.Path(path.asAndroidPath())
                fillPath.lineTo(points.last().x, chartHeight)
                fillPath.lineTo(points.first().x, chartHeight)
                fillPath.close()

                drawPath(
                    path = fillPath.asComposePath(),
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Blue.copy(alpha = 0.2f), Color.Transparent),
                        startY = 0f,
                        endY = chartHeight
                    )
                )

                drawPath(
                    path = path,
                    color = Color.Blue.copy(alpha = 0.8f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            } else if (points.size == 1) {
                drawCircle(color = Color.Blue, radius = 4f, center = points[0])
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
