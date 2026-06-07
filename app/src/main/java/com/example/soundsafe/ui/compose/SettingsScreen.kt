package com.example.soundsafe.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun SettingsScreen(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
    selectedAccentName: String,
    onAccentSelected: (String) -> Unit,
    isAutoMediaEnabled: Boolean,
    onAutoMediaToggle: (Boolean) -> Unit,
    isAutoRingtoneEnabled: Boolean,
    onAutoRingtoneToggle: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()
    var showTransparencyDialog by remember { mutableStateOf(false) }
    var showVolumeLogicDialog by remember { mutableStateOf(false) }
    var showBatteryInfoDialog by remember { mutableStateOf(false) }

    if (showTransparencyDialog) {
        AlertDialog(
            onDismissRequest = { showTransparencyDialog = false },
            title = { Text("Measurement Transparency") },
            text = {
                Text("SoundSafe uses your microphone to calculate Sound Pressure Level (SPL) in decibels.\n\n" +
                        "• Privacy: We do not record, listen to, or store any actual audio or conversations. Only the loudness level is captured.\n\n" +
                        "• Phone Calls: To protect your privacy and call quality, monitoring pauses during calls. These periods appear as 0 dB in your logs.\n\n" +
                        "• Audio Playback: When playing music or videos on your device's speakers, the microphone may pick up this sound, resulting in higher readings.")
            },
            confirmButton = {
                TextButton(onClick = { showTransparencyDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    if (showVolumeLogicDialog) {
        AlertDialog(
            onDismissRequest = { showVolumeLogicDialog = false },
            title = { Text("How auto-volume works") },
            text = {
                Text("SoundSafe intelligently scales your device volume based on your surroundings:\n\n" +
                        "• Smart Mapping: We map ambient noise from 40dB (quiet) to 90dB (loud) into a volume range of 20% to 100%.\n\n" +
                        "• Sound Smoothing: The app compares new readings with your recent history. Sudden, short peaks (like a door slamming) are recognized as 'spikes' and ignored.\n\n" +
                        "• Gradual Adjustment: To prevent jarring jumps, volume shifts smoothly. It only reaches a new level once it confirms your environment has truly changed.\n\n" +
                        "• Manual Priority: If you manually adjust your volume, auto-adjustment pauses for that stream to give you full control.")
            },
            confirmButton = {
                TextButton(onClick = { showVolumeLogicDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    if (showBatteryInfoDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryInfoDialog = false },
            title = { Text("Battery & Performance") },
            text = {
                Text("SoundSafe is designed to be extremely lightweight:\n\n" +
                        "• Burst Sampling: We only activate the microphone for 2 seconds every minute.\n\n" +
                        "• Efficient Processing: We process simple volume levels rather than complex audio, ensuring minimal impact on your battery life and CPU.")
            },
            confirmButton = {
                TextButton(onClick = { showBatteryInfoDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ThemeSelectionCard(
            selectedTheme = selectedTheme,
            onThemeSelected = onThemeSelected
        )

        AccentSelectionCard(
            selectedAccentName = selectedAccentName,
            onAccentSelected = onAccentSelected
        )

        SettingToggle(
            label = "Automatic Media Volume",
            checked = isAutoMediaEnabled,
            onCheckedChange = onAutoMediaToggle
        )
        SettingToggle(
            label = "Automatic Ringtone Volume",
            checked = isAutoRingtoneEnabled,
            onCheckedChange = onAutoRingtoneToggle
        )

        HorizontalDivider()

        SettingsInfoCard(
            title = "Measurement Transparency",
            onClick = { showTransparencyDialog = true }
        )

        SettingsInfoCard(
            title = "Automatic Volume Logic",
            onClick = { showVolumeLogicDialog = true }
        )

        SettingsInfoCard(
            title = "Battery & Performance",
            onClick = { showBatteryInfoDialog = true }
        )
    }
}

@Composable
fun InfoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val accentColor = LocalAccentColor.current

    // Inverted logic to handle the cutout:
    // The background box fills the 'i' hole, the icon tint covers the surrounding circle.
    val circleColor = if (isDark) accentColor.darkerPrimary else MaterialTheme.colorScheme.onPrimary
    val iconColor = MaterialTheme.colorScheme.primary

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp)
        ) {
            // Background circle to fill the "i" cutout (this will be the 'i' color)
            Box(
                modifier = Modifier
                    .size(12.dp) // Sized to fill the 'i' specifically
                    .clip(CircleShape)
                    .background(circleColor)
            )
            // The Info Icon (this will be the surrounding circle color)
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingsInfoCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            InfoIconButton(onClick = onClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionCard(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("System Default", "Light", "Dark")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Theme Preference",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedTheme,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onThemeSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccentSelectionCard(
    selectedAccentName: String,
    onAccentSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentAccent = accents.find { it.name == selectedAccentName } ?: accents.first()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Accent Color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedAccentName,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isDark) currentAccent.darkPrimary else currentAccent.lightPrimary)
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    accents.forEach { accent ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) accent.darkPrimary else accent.lightPrimary)
                                    )
                                    Spacer(modifier = Modifier.size(12.dp))
                                    Text(accent.name)
                                }
                            },
                            onClick = {
                                onAccentSelected(accent.name)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val thumbColor = if (isDark) LocalAccentColor.current.darkerPrimary else MaterialTheme.colorScheme.onPrimary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = thumbColor
                )
            )
        }
    }
}
