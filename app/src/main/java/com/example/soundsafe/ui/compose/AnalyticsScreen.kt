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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soundsafe.ui.viewmodel.AnalyticsViewModel
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = viewModel()
) {
    val soundLog by viewModel.soundLog.collectAsState()
    val selectedTimeFrame by viewModel.selectedTimeFrame.collectAsState()
    val avgDb by viewModel.avgDb.collectAsState()
    val maxDb by viewModel.maxDb.collectAsState()
    val minDb by viewModel.minDb.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            TimeFrameSelector(
                selectedTimeFrame = selectedTimeFrame,
                onTimeFrameSelected = { viewModel.onTimeFrameSelected(it) }
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
            AnalyticsHeader(
                title = title,
                soundLog = soundLog,
                selectedTimeFrame = selectedTimeFrame,
                avgDb = avgDb,
                maxDb = maxDb,
                minDb = minDb
            )
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

        items(soundLog.reversed()) { record ->
            SoundLogItem(record, selectedTimeFrame)
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
        if (selectedTimeFrame != "Daily") {
            Text(
                text = "Showing daily averages",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        SoundLineGraph(
            soundLog = soundLog,
            selectedTimeFrame = selectedTimeFrame,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

@Composable
fun SoundLineGraph(
    soundLog: List<SoundRecord>,
    selectedTimeFrame: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
    )
    val axisLabelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )

    val maxRecord = soundLog.maxOfOrNull { it.dbLevel } ?: 0.0
    // Y-axis must always include 85dB and some padding
    val maxYLimit = maxOf(100.0, maxRecord + 10.0).toFloat()

    val outlineColor = MaterialTheme.colorScheme.outline
    Canvas(modifier = modifier) {
        val widthPx = size.width
        val heightPx = size.height

        // Proper padding to avoid clipping
        val leftPadding = 120f
        val bottomPadding = 100f
        val rightPadding = 40f
        val topPadding = 40f

        val chartWidth = widthPx - leftPadding - rightPadding
        val chartHeight = heightPx - bottomPadding - topPadding

        // 1. Draw Axis Labels
        // Y-Axis Label: "Decibels (dB SPL)"
        val yAxisLabelLayout = textMeasurer.measure("Decibels (dB SPL)", style = axisLabelStyle)
        drawText(
            textLayoutResult = yAxisLabelLayout,
            topLeft = Offset(10f, topPadding - yAxisLabelLayout.size.height - 10f)
        )

        // X-Axis Label
        val xAxisLabel = when (selectedTimeFrame) {
            "Daily" -> "Time"
            "Weekly" -> "Day of Week"
            "Monthly" -> "Day of Month"
            else -> ""
        }
        val xAxisLabelLayout = textMeasurer.measure(xAxisLabel, style = axisLabelStyle)
        drawText(
            textLayoutResult = xAxisLabelLayout,
            topLeft = Offset(leftPadding + chartWidth / 2f - xAxisLabelLayout.size.width / 2f, heightPx - 40f)
        )

        // 2. Draw Y-Axis and Labels (5 and 10 dB increments)
        val yIncrements = (0..maxYLimit.toInt() step 5).toList()
        yIncrements.forEach { db ->
            if (db % 10 == 0 || db == 85) { // Show labels for 10s and the warning 85
                val yPos = topPadding + chartHeight - ((db / maxYLimit) * chartHeight)
                val labelText = db.toString()
                val textLayout = textMeasurer.measure(labelText, style = labelStyle)
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(leftPadding - textLayout.size.width - 20f, yPos - textLayout.size.height / 2f)
                )

                // Draw grid line
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    start = Offset(leftPadding, yPos),
                    end = Offset(leftPadding + chartWidth, yPos),
                    strokeWidth = 1f
                )
            }
        }

        // 3. Draw X-Axis Labels
        val calendar = Calendar.getInstance()
        when (selectedTimeFrame) {
            "Daily" -> {
                val timeLabels = listOf("12 AM", "3 AM", "6 AM", "9 AM", "12 PM", "3 PM", "6 PM", "9 PM", "12 AM")
                timeLabels.forEachIndexed { index, label ->
                    val xPos = leftPadding + (index * (chartWidth / (timeLabels.size - 1)))
                    val textLayout = textMeasurer.measure(label, style = labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(xPos - textLayout.size.width / 2f, topPadding + chartHeight + 15f)
                    )
                }
            }
            "Weekly" -> {
                val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                dayLabels.forEachIndexed { index, label ->
                    val xPos = leftPadding + (index * (chartWidth / 7f)) // Spaced for 7 days
                    val textLayout = textMeasurer.measure(label, style = labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(xPos - textLayout.size.width / 2f, topPadding + chartHeight + 15f)
                    )
                }
            }
            "Monthly" -> {
                val totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val labelsToShow = (1..totalDaysInMonth step 5).toMutableList()
                if (labelsToShow.last() != totalDaysInMonth) labelsToShow.add(totalDaysInMonth)

                labelsToShow.forEach { day ->
                    val xPos = leftPadding + ((day - 1) / totalDaysInMonth.toFloat()) * chartWidth
                    val textLayout = textMeasurer.measure(day.toString(), style = labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(xPos - textLayout.size.width / 2f, topPadding + chartHeight + 15f)
                    )
                }
            }
        }

        // 4. Draw Warning Line (85dB)
        val warningY = topPadding + chartHeight - ((85f / maxYLimit) * chartHeight)
        drawLine(
            color = Color.Red.copy(alpha = 0.6f),
            start = Offset(leftPadding, warningY),
            end = Offset(leftPadding + chartWidth, warningY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
        )
        val warningLabelLayout = textMeasurer.measure("Warning (85 dB)", style = TextStyle(color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold))
        drawText(
            textLayoutResult = warningLabelLayout,
            topLeft = Offset(leftPadding + chartWidth - warningLabelLayout.size.width - 5f, warningY - warningLabelLayout.size.height - 2f)
        )

        // 5. Plot Data
        if (soundLog.isNotEmpty()) {
            val points = if (selectedTimeFrame == "Daily") {
                soundLog.map { record ->
                    val cal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    val fractionOfDay = (hour * 60 + minute) / 1440f
                    val x = leftPadding + fractionOfDay * chartWidth
                    val y = topPadding + chartHeight - ((record.dbLevel.toFloat() / maxYLimit) * chartHeight)
                    Offset(x, y)
                }.sortedBy { it.x }
            } else {
                // Average by day for Weekly and Monthly
                soundLog.groupBy { record ->
                    val cal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }.map { (dayTimestamp, records) ->
                    val avgDbForDay = records.map { it.dbLevel }.average().toFloat()
                    val cal = Calendar.getInstance().apply { timeInMillis = dayTimestamp }
                    val x = when (selectedTimeFrame) {
                        "Weekly" -> {
                            val dayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Mon=0...Sun=6
                            leftPadding + (dayOfWeek / 6f) * chartWidth
                        }
                        "Monthly" -> {
                            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                            val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                            leftPadding + ((dayOfMonth - 1) / (totalDays - 1).toFloat()) * chartWidth
                        }
                        else -> leftPadding
                    }
                    val y = topPadding + chartHeight - ((avgDbForDay / maxYLimit) * chartHeight)
                    Offset(x, y)
                }.sortedBy { it.x }
            }

            if (points.isNotEmpty()) {
                val path = Path()
                path.moveTo(points[0].x, points[0].y)

                // Use lineTo for accuracy
                for (i in 1 until points.size) {
                    path.lineTo(points[i].x, points[i].y)
                }

                // Draw Gradient Fill
                val fillPath = android.graphics.Path(path.asAndroidPath())
                fillPath.lineTo(points.last().x, topPadding + chartHeight)
                fillPath.lineTo(points.first().x, topPadding + chartHeight)
                fillPath.close()

                drawPath(
                    path = fillPath.asComposePath(),
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Blue.copy(alpha = 0.3f), Color.Transparent),
                        startY = topPadding,
                        endY = topPadding + chartHeight
                    )
                )

                // Draw Line
                drawPath(
                    path = path,
                    color = Color.Blue,
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            }
        }

        // 6. Draw Main Axes
        drawLine(
            color = outlineColor,
            start = Offset(leftPadding, topPadding),
            end = Offset(leftPadding, topPadding + chartHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = outlineColor,
            start = Offset(leftPadding, topPadding + chartHeight),
            end = Offset(leftPadding + chartWidth, topPadding + chartHeight),
            strokeWidth = 2f
        )
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
fun SoundLogItem(record: SoundRecord, selectedTimeFrame: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val displayTime = if (selectedTimeFrame == "Daily") record.time else record.dateTime
        Text(text = displayTime)
        Text(text = "${"%.1f".format(record.dbLevel)} dB SPL")
    }
}
