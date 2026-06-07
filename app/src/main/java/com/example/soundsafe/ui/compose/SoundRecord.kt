package com.example.soundsafe.ui.compose

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SoundRecord(
    val timestamp: Long,
    val dbLevel: Double
) {
    val time: String
        get() = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

    val dateTime: String
        get() = SimpleDateFormat("dd-MM-yy h:mm a", Locale.getDefault()).format(Date(timestamp))

    val dateOnly: String
        get() = SimpleDateFormat("dd-MM-yy", Locale.getDefault()).format(Date(timestamp))
}
