package com.example.soundsafe.ui.compose

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
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

        Button(
            onClick = onToggleRecording,
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(0.7f),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = if (isRecording) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isRecording) "STOP MONITORING" else "RESUME RECORDING",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
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

        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = arcSize
        )

        drawArc(
            brush = Brush.sweepGradient(
                0.0f to Color.Transparent,
                0.5f to gradientColors.first(),
                0.75f to gradientColors[2],
                1.0f to gradientColors.last(),
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
