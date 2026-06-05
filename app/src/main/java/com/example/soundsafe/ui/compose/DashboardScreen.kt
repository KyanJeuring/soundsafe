package com.example.soundsafe.ui.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

@Composable
fun DashboardScreen(
    currentDbLevel: String,
    currentMediaVolume: Float,
    onVolumeChange: (Float) -> Unit,
    isRecording: Boolean,
    onToggleRecording: () -> Unit
) {
    val dbValue = currentDbLevel.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SoundGauge(
                dbValue = dbValue.toFloat(),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1.8f)
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "$currentDbLevel dB",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        VolumeController(
            volume = currentMediaVolume,
            onVolumeChange = onVolumeChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onToggleRecording,
            modifier = Modifier.fillMaxWidth(),
            colors = if (isRecording) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            } else {
                ButtonDefaults.buttonColors()
            }
        ) {
            Text(if (isRecording) "Stop Monitoring" else "Start Monitoring")
        }
    }
}

@Composable
fun VolumeController(
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Low Volume")
                Slider(
                    value = volume,
                    onValueChange = {
                        onVolumeChange(it)
                        // TODO: Triggering this should disable the Automatic Volume feature in the backend logic.
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "High Volume")
            }
            Text(
                text = "System Media Volume",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SoundGauge(
    dbValue: Float,
    modifier: Modifier = Modifier,
    maxDb: Float = 100f
) {
    val coercedValue = dbValue.coerceIn(0f, maxDb)
    val sweepAngle by animateFloatAsState(
        targetValue = (coercedValue / maxDb) * 180f,
        animationSpec = tween(durationMillis = 500),
        label = "SweepAngleAnimation"
    )

    val color by animateColorAsState(
        targetValue = when {
            coercedValue <= 40f -> Color(0xFF4CAF50) // Green
            coercedValue <= 65f -> Color(0xFFFF9800) // Orange
            else -> Color(0xFFF44336) // Red
        },
        animationSpec = tween(durationMillis = 500),
        label = "ColorAnimation"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = size.width * 0.1f
        // Ensure the arc is centered and fits the width
        val arcSize = size.copy(height = size.width)

        // Track Layer (Subtle Light Grey)
        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = arcSize
        )

        // Progress Layer (Dynamic Color)
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = arcSize
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    DashboardScreen(
        currentDbLevel = "65.2",
        currentMediaVolume = 0.5f,
        onVolumeChange = {},
        isRecording = true,
        onToggleRecording = {}
    )
}
