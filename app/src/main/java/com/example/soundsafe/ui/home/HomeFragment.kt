package com.example.soundsafe.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.soundsafe.R
import com.example.soundsafe.audio.SoundMeasurementStore
import com.example.soundsafe.audio.SoundMonitoringService
import com.example.soundsafe.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val microphonePermissionRequestCode = 100
    private val notificationPermissionRequestCode = 101

    private val tableRefreshHandler =
        Handler(Looper.getMainLooper())

    private val tableRefreshRunnable =
        object : Runnable {

            override fun run() {

                updateMeasurementTable()

                tableRefreshHandler.postDelayed(
                    this,
                    2000
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(
            inflater,
            container,
            false
        )

        binding.textHome.text =
            getString(R.string.sound_monitoring_running)

        binding.buttonRecordingToggle.setOnClickListener {
            toggleRecording()
        }

        syncRecordingUi()
        checkPermissionsAndStartService()

        updateMeasurementTable()

        return binding.root
    }

    private fun checkPermissionsAndStartService() {

        val microphoneGranted =
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        if (!microphoneGranted) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                microphonePermissionRequestCode
            )

            return
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val notificationGranted =
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!notificationGranted) {

                requestPermissions(
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    notificationPermissionRequestCode
                )

                return
            }
        }

        startSoundService()
    }

    private fun startSoundService() {

        if (SoundMonitoringService.isServiceRunning) {
            syncRecordingUi()
            return
        }

        val intent = Intent(
            requireContext(),
            SoundMonitoringService::class.java
        )

        ContextCompat.startForegroundService(
            requireContext(),
            intent
        )

        showRecordingUi(true)
    }

    private fun toggleRecording() {

        val context = requireContext()
        val intent = Intent(
            context,
            SoundMonitoringService::class.java
        )

        if (SoundMonitoringService.isRecording) {

            intent.action = SoundMonitoringService.ACTION_STOP_RECORDING
            context.startService(intent)
            showRecordingUi(false)

        } else {

            intent.action = SoundMonitoringService.ACTION_RESUME_RECORDING
            context.startService(intent)
            showRecordingUi(true)
        }
    }

    private fun syncRecordingUi() {

        val binding = _binding ?: return

        showRecordingUi(
            SoundMonitoringService.isRecording,
            binding
        )
    }

    private fun showRecordingUi(
        isRecording: Boolean,
        binding: FragmentHomeBinding? = _binding
    ) {

        val targetBinding = binding ?: return

        if (isRecording) {
            targetBinding.textHome.text =
                getString(R.string.sound_monitoring_running)
            targetBinding.buttonRecordingToggle.text =
                getString(R.string.stop_recording)
        } else {
            targetBinding.textHome.text =
                getString(R.string.sound_monitoring_paused)
            targetBinding.buttonRecordingToggle.text =
                getString(R.string.resume_recording)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            grantResults.isNotEmpty() &&
            grantResults[0] ==
            PackageManager.PERMISSION_GRANTED
        ) {

            checkPermissionsAndStartService()

        } else {

            _binding?.textHome?.text =
                getString(R.string.permission_denied)
        }
    }

    private fun updateMeasurementTable() {

        val table =
            _binding?.measurementTable ?: return

        while (table.childCount > 1) {
            table.removeViewAt(1)
        }

        val timeFormat = SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        )

        SoundMeasurementStore
            .getMeasurements()
            .asReversed()
            .forEach { measurement ->

                val row = TableRow(requireContext())

                val timeText =
                    TextView(requireContext())

                timeText.text =
                    timeFormat.format(
                        Date(measurement.timestamp)
                    )

                timeText.setPadding(
                    8,
                    8,
                    8,
                    8
                )

                val dbText =
                    TextView(requireContext())

                dbText.text =
                    "%.1f".format(
                        measurement.decibels
                    )

                dbText.setPadding(
                    8,
                    8,
                    8,
                    8
                )

                row.addView(timeText)
                row.addView(dbText)

                table.addView(row)
            }
    }

    override fun onResume() {

        super.onResume()

        tableRefreshHandler.post(
            tableRefreshRunnable
        )
    }

    override fun onPause() {

        super.onPause()

        tableRefreshHandler.removeCallbacks(
            tableRefreshRunnable
        )
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}
