package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View.NO_ID
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private var downloadID: Long = 0
    private lateinit var binding: ActivityMainBinding
    private lateinit var downloadManager: DownloadManager
    private var downloadStatus = ""
    private var downloadFileName = ""
    private lateinit var notificationManager: NotificationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.apply {
            setSupportActionBar(toolbar)
            this.lyContent.btnDownload.setOnClickListener {
                checkDownloadSource()
            }
        }
        setContentView(binding.root)
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }


    private fun checkDownloadSource() {
        when (binding.lyContent.rgSource.checkedRadioButtonId) {
            NO_ID -> {
                Toast.makeText(
                    this,
                    getString(R.string.choose_file_to_download),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                downloadFileName =
                    findViewById<RadioButton>(binding.lyContent.rgSource.checkedRadioButtonId).text.toString()
                download()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT).show()
                checkDownloadStatus()
                notificationManager.sendCompleteDownload(
                    downloadFileName,
                    downloadStatus,
                    this@MainActivity
                )
            }
        }
    }

    private fun checkDownloadStatus() {
        val cursor: Cursor =
            downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
        if (cursor.moveToNext()) {
            val status: Int = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            cursor.close()
            when (status) {
                DownloadManager.STATUS_FAILED -> {
                    binding.lyContent.btnDownload.updateButtonState(ButtonState.Completed)
                    downloadStatus = "Failed"
                    // do something when failed
                }
                DownloadManager.STATUS_PENDING, DownloadManager.STATUS_PAUSED -> {
                    // do something pending or paused
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    binding.lyContent.btnDownload.updateButtonState(ButtonState.Completed)
                    downloadStatus = "Success"
                    // do something when successful
                }
                DownloadManager.STATUS_RUNNING -> {
                    // do something when running
                }
            }
        }
    }

    private fun download() {
        binding.lyContent.btnDownload.updateButtonState(ButtonState.Loading)
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
    }

}
