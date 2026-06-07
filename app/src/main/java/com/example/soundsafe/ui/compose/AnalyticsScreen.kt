package com.example.soundsafe.ui.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
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

    var showInfoDialog by remember { mutableStateOf(false) }
    var showGraphInfoDialog by remember { mutableStateOf(false) }
    var isLogExpanded by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Log Aggregation") },
            text = {
                Text("Detailed logs are averaged to make them easier to read:\n\n" +
                        "• Daily: 15-minute averages\n" +
                        "• Weekly: 1-hour averages\n" +
                        "• Monthly: 1-day averages\n\n" +
                        "Note: All statistics (Avg/Max) are still calculated using 100% of the raw 1-minute measurements.")
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    if (showGraphInfoDialog) {
        AlertDialog(
            onDismissRequest = { showGraphInfoDialog = false },
            title = { Text("About the Graph") },
            text = {
                Text("This graph shows your sound exposure trend using averaged data points to make the history easier to read.\n\n" +
                        "Note: The 'MAX LEVEL' card below shows the absolute highest peak measured, which may be higher than any single point on this averaged graph line.")
            },
            confirmButton = {
                TextButton(onClick = { showGraphInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                PillTimeFrameSelector(
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
                AnalyticsHeaderCard(
                    title = title,
                    soundLog = soundLog,
                    selectedTimeFrame = selectedTimeFrame,
                    onInfoClick = { showGraphInfoDialog = true }
                )
            }

            item {
                StatsRow(avgDb = avgDb, maxDb = maxDb)
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    onClick = { isLogExpanded = !isLogExpanded }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Detailed Log",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = if (isLogExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isLogExpanded) "Collapse" else "Expand",
                                modifier = Modifier.padding(start = 8.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        InfoIconButton(onClick = { showInfoDialog = true })
                    }
                }
            }

            if (isLogExpanded) {
                items(soundLog.reversed()) { record ->
                    SoundLogItem(record, selectedTimeFrame)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PillTimeFrameSelector(
    selectedTimeFrame: String,
    onTimeFrameSelected: (String) -> Unit
) {
    val options = listOf("Daily", "Weekly", "Monthly")
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedTimeFrame
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = tween(durationMillis = 300),
                    label = "TabBackground"
                )
                val defaultTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                val targetTextColor = if (isSelected && isDark) LocalAccentColor.current.darkerPrimary else defaultTextColor

                val textColor by animateColorAsState(
                    targetValue = targetTextColor,
                    animationSpec = tween(durationMillis = 300),
                    label = "TabText"
                )

                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onTimeFrameSelected(option) },
                    color = backgroundColor,
                    shape = CircleShape
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsHeaderCard(
    title: String,
    soundLog: List<SoundRecord>,
    selectedTimeFrame: String,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedTimeFrame != "Daily") {
                        Text(
                            text = "Showing daily averages",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                InfoIconButton(onClick = onInfoClick)
            }
            Spacer(modifier = Modifier.height(24.dp))
            SoundLineGraph(
                soundLog = soundLog,
                selectedTimeFrame = selectedTimeFrame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
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
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        fontSize = 10.sp
    )
    val graphColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outline

    val maxRecord = soundLog.maxOfOrNull { it.dbLevel } ?: 0.0
    val maxYLimit = maxOf(100.0, maxRecord + 10.0).toFloat()

    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val warningLineColor = Color(0xFFF44336).copy(alpha = 0.6f)
    val warningTextColor = Color(0xFFF44336)

    Canvas(modifier = modifier) {
        val widthPx = size.width
        val heightPx = size.height

        // Define Safe Drawing Zone to prevent label overlap
        val leftPadding = 50f
        val bottomPadding = 60f
        val rightPadding = 50f
        val topPadding = 20f

        val chartWidth = widthPx - leftPadding - rightPadding
        val chartHeight = heightPx - bottomPadding - topPadding

        // 1. Draw 10-Step Y-Axis Labels and Horizontal Grid Lines
        val yAxisIncrements = (0..100 step 10).toList()
        yAxisIncrements.forEach { db ->
            val yPos = topPadding + chartHeight - ((db / maxYLimit) * chartHeight)
            val textLayout = textMeasurer.measure(db.toString(), style = labelStyle)

            // Draw Label
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(leftPadding - textLayout.size.width - 15f, yPos - textLayout.size.height / 2f)
            )

            // Draw Subtle Horizontal Grid Line
            drawLine(
                color = gridLineColor,
                start = Offset(leftPadding, yPos),
                end = Offset(leftPadding + chartWidth, yPos),
                strokeWidth = 1f
            )
        }

        // 2. Draw X-Axis Labels
        val calendar = Calendar.getInstance()
        when (selectedTimeFrame) {
            "Daily" -> {
                val timeLabels = listOf("12 AM", "6 AM", "12 PM", "6 PM", "12 AM")
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
                    val xPos = leftPadding + (index * (chartWidth / 6f))
                    val textLayout = textMeasurer.measure(label, style = labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(xPos - textLayout.size.width / 2f, topPadding + chartHeight + 15f)
                    )
                }
            }
            "Monthly" -> {
                val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val monthLabels = listOf(1, 10, 20, totalDays)
                monthLabels.forEach { day ->
                    val xPos = leftPadding + ((day - 1) / (totalDays - 1).toFloat()) * chartWidth
                    val textLayout = textMeasurer.measure(day.toString(), style = labelStyle)
                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(xPos - textLayout.size.width / 2f, topPadding + chartHeight + 15f)
                    )
                }
            }
        }

        // 3. Draw Warning Line (85dB) - Fixed Red
        val warningY = topPadding + chartHeight - ((85f / maxYLimit) * chartHeight)
        drawLine(
            color = warningLineColor,
            start = Offset(leftPadding, warningY),
            end = Offset(leftPadding + chartWidth, warningY),
            strokeWidth = 2.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
        )
        val warningLabelLayout = textMeasurer.measure("Warning (85 dB)", style = TextStyle(color = warningTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold))
        drawText(
            textLayoutResult = warningLabelLayout,
            topLeft = Offset(leftPadding + chartWidth - warningLabelLayout.size.width - 5f, warningY - warningLabelLayout.size.height - 2f)
        )

        // 4. Plot Data
        if (soundLog.isNotEmpty()) {
            val points = if (selectedTimeFrame == "Daily") {
                soundLog.map { record ->
                    val cal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
                    val fractionOfDay = (cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)) / 1440f
                    val x = leftPadding + fractionOfDay * chartWidth
                    val y = topPadding + chartHeight - ((record.dbLevel.toFloat() / maxYLimit) * chartHeight)
                    Offset(x, y)
                }.sortedBy { it.x }
            } else {
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
                    val xFraction = when (selectedTimeFrame) {
                        "Weekly" -> (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 / 6f
                        "Monthly" -> (cal.get(Calendar.DAY_OF_MONTH) - 1) / (cal.getActualMaximum(Calendar.DAY_OF_MONTH) - 1).toFloat()
                        else -> 0f
                    }
                    val x = leftPadding + xFraction * chartWidth
                    val y = topPadding + chartHeight - ((avgDbForDay / maxYLimit) * chartHeight)
                    Offset(x, y)
                }.sortedBy { it.x }
            }

            if (points.isNotEmpty()) {
                val path = Path()
                path.moveTo(points[0].x, points[0].y)

                if (points.size > 1) {
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                        val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                        path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                    }
                }

                // Gradient Fill
                val fillPath = android.graphics.Path(path.asAndroidPath())
                fillPath.lineTo(points.last().x, topPadding + chartHeight)
                fillPath.lineTo(points.first().x, topPadding + chartHeight)
                fillPath.close()

                drawPath(
                    path = fillPath.asComposePath(),
                    brush = Brush.verticalGradient(
                        colors = listOf(graphColor.copy(alpha = 0.5f), Color.Transparent),
                        startY = topPadding,
                        endY = topPadding + chartHeight
                    )
                )

                // Main Line
                drawPath(
                    path = path,
                    color = graphColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // 5. Main Axes
        drawLine(
            color = axisColor.copy(alpha = 0.5f),
            start = Offset(leftPadding, topPadding),
            end = Offset(leftPadding, topPadding + chartHeight),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = axisColor.copy(alpha = 0.5f),
            start = Offset(leftPadding, topPadding + chartHeight),
            end = Offset(leftPadding + chartWidth, topPadding + chartHeight),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
fun StatsRow(avgDb: Double, maxDb: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$value dB",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SoundLogItem(record: SoundRecord, selectedTimeFrame: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val displayTime = when (selectedTimeFrame) {
            "Daily" -> record.time
            "Monthly" -> record.dateOnly
            else -> record.dateTime
        }
        Text(
            text = displayTime,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "${"%.1f".format(record.dbLevel)} dB SPL",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (record.dbLevel >= 85) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
        )
    }
}
