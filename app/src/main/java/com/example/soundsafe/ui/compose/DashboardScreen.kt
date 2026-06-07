package com.example.soundsafe.ui.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    currentDbLevel: String,
    isRecording: Boolean,
    onToggleRecording: () -> Unit
) {
    // Determine effective dB value based on recording state
    val effectiveDbText = if (isRecording) currentDbLevel else "0"
    val dbValue = if (isRecording) (currentDbLevel.toDoubleOrNull() ?: 0.0) else 0.0

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
                text = "$effectiveDbText dB",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onToggleRecording,
            modifier = Modifier.fillMaxWidth(),
            colors = if (isRecording) {
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
            } else {
                ButtonDefaults.buttonColors()
            }
        ) {
            Text(if (isRecording) "Stop Recording" else "Resume Recording")
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

    // Fixed gradient colors mapped to the 180-degree sweep
    val gradientColors = listOf(
        Color(0xFF2E7D32), // Dark Green
        Color(0xFF4CAF50), // Light Green
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFF9800), // Orange
        Color(0xFFF44336)  // Red
    )

    Canvas(modifier = modifier) {
        val strokeWidth = size.width * 0.1f
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

        // Progress Layer (Revealing Sweep Gradient)
        // CRITICAL MATH: SweepGradient starts at 0 degrees (3 o'clock).
        // Our arch starts at 180 degrees (9 o'clock) and sweeps 180 degrees clockwise.
        // We need to map our colors to the second half of the gradient (0.5 to 1.0).
        drawArc(
            brush = Brush.sweepGradient(
                0.0f to Color.Transparent, // First 180 degrees (3 o'clock to 9 o'clock) are empty
                0.5f to gradientColors.first(), // 180 degrees (9 o'clock)
                0.75f to gradientColors[2], // 270 degrees (12 o'clock)
                1.0f to gradientColors.last(), // 360 degrees (3 o'clock)
                center = Offset(size.width / 2f, size.width / 2f)
            ),
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
        isRecording = true,
        onToggleRecording = {}
    )
}
