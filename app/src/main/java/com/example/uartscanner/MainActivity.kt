package com.example.uartscanner

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var tvLog: TextView
    private lateinit var btnScan: Button
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog = findViewById(R.id.tvLog)
        btnScan = findViewById(R.id.btnScan)
        progress = findViewById(R.id.progress)

        btnScan.setOnClickListener {
            tvLog.text = "Start scan...\n"
            progress.isIndeterminate = true
            thread {
                val scanner = SerialPortScanner()
                val results = scanner.scanAllPorts { line ->
                    runOnUiThread { appendLog(line) }
                }
                runOnUiThread {
                    progress.isIndeterminate = false
                    appendLog("\nScan finished. Found: ${'$'}{results.size} ports accessible")
                    // Save log
                    val out = getExternalFilesDir(null)?.let { File(it, "uart_log.txt") }
                    out?.writeText(tvLog.text.toString())
                    appendLog("Log saved: ${'$'}{out?.absolutePath}")
                }
            }
        }

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
    }

    private fun appendLog(text: String) {
        tvLog.append(text + "\n")
        val sv = findViewById<ScrollView>(R.id.scroll)
        sv.post { sv.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
