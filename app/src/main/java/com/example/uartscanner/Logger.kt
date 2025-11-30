package com.example.uartscanner

import android.content.Context
import java.io.File

object Logger {
    fun save(context: Context, name: String, text: String): String {
        val f = File(context.getExternalFilesDir(null), name)
        f.writeText(text)
        return f.absolutePath
    }
}
