package com.example.uartscanner

import java.io.File
import java.io.RandomAccessFile

class SerialPortScanner {
    private val commonPaths = listOf("/dev/ttyS0","/dev/ttyS1","/dev/ttyS2","/dev/ttyS3","/dev/ttyMT0","/dev/ttyMT1","/dev/ttyUSB0","/dev/ttyUSB1")

    fun scanAllPorts(onLine: (String) -> Unit): List<String> {
        val found = mutableListOf<String>()
        val dev = File("/dev")
        val devPorts = dev.listFiles()?.filter { it.name.startsWith("tty") }?.map { it.absolutePath } ?: emptyList()
        val ports = (commonPaths + devPorts).distinct()
        onLine("Candidate ports: ${'$'}{ports.joinToString(", ")}")

        for (p in ports) {
            try {
                onLine("-> Opening $p")
                val raf = RandomAccessFile(File(p), "r")
                val buffer = ByteArray(1024)
                val len = try { raf.read(buffer) } catch (e: Exception) { -1 }
                if (len > 0) {
                    val hex = buffer.copyOf(len).joinToString(" ") { String.format("%02X", it) }
                    onLine("RX from $p: $hex")
                    found.add(p)
                } else {
                    onLine("No immediate data on $p (len=$len). Starting listener")
                    val listenResult = listenShort(p, 2000)
                    if (listenResult.isNotEmpty()) {
                        onLine("Listener got: $listenResult")
                        found.add(p)
                    } else {
                        onLine("No data during short listen on $p")
                    }
                }
                raf.close()
            } catch (e: Exception) {
                onLine("Cannot open $p : ${'$'}{e.message}")
            }
        }
        return found
    }

    private fun listenShort(path: String, ms: Int): String {
        try {
            val f = RandomAccessFile(File(path), "r")
            val end = System.currentTimeMillis() + ms
            val acc = mutableListOf<Byte>()
            while (System.currentTimeMillis() < end) {
                val b = try { f.read() } catch (e: Exception) { -1 }
                if (b == -1) {
                    Thread.sleep(50)
                    continue
                }
                acc.add(b.toByte())
                if (acc.size >= 1) break
            }
            f.close()
            if (acc.isNotEmpty()) {
                return acc.joinToString(" ") { String.format("%02X", it) }
            }
        } catch (e: Exception) {
        }
        return ""
    }
}
