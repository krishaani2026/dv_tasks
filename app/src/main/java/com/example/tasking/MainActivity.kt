package com.example.tasking

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tasking.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val taskReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.tasking.TIMER_UPDATE") {
                val seconds = intent.getIntExtra("time_left", 0)

                if (seconds > 0) {
                    binding.tvStatus.text = "Sprint Active: $seconds s remaining"
                } else {
                    binding.tvStatus.text = "Sprint Complete! 🎯"
                    binding.btnAction.text = "START NEW SPRINT"
                    val primaryColor = ContextCompat.getColor(this@MainActivity, R.color.tasking_primary)
                    binding.btnAction.backgroundTintList = ColorStateList.valueOf(primaryColor)

                    // Visual pop-up when timer hits 0
                    Toast.makeText(this@MainActivity, "🎉 Sprint Completed!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()

        binding.btnAction.setOnClickListener {
            val serviceIntent = Intent(this, TaskService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)

            binding.btnAction.text = "SPRINT IN PROGRESS"
            val greenColor = ContextCompat.getColor(this, R.color.green_flag)
            binding.btnAction.backgroundTintList = ColorStateList.valueOf(greenColor)

            // Visual pop-up on start
            Toast.makeText(this, "🚀 30-Second Sprint Started!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("com.example.tasking.TIMER_UPDATE")
        ContextCompat.registerReceiver(
            this,
            taskReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(taskReceiver)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }
}