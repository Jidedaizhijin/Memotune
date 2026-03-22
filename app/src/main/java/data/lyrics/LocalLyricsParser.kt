package com.jidedaizhijin.myapplication.data.lyrics

import java.io.File

object LocalLyricsParser {

    private val timeTagRegex = Regex("""\[(\d{2}):(\d{2})(?:\.(\d{1,3}))?]""")

    fun parseLrcText(text: String): List<LyricLine> {
        val result = mutableListOf<LyricLine>()

        text.lineSequence().forEach { rawLine ->
            val matches = timeTagRegex.findAll(rawLine).toList()
            if (matches.isEmpty()) return@forEach

            val lyricText = rawLine.replace(timeTagRegex, "").trim()
            if (lyricText.isBlank()) return@forEach

            matches.forEach { match ->
                val min = match.groupValues[1].toIntOrNull() ?: 0
                val sec = match.groupValues[2].toIntOrNull() ?: 0
                val frac = match.groupValues[3]

                val ms = when (frac.length) {
                    1 -> (frac.toIntOrNull() ?: 0) * 100
                    2 -> (frac.toIntOrNull() ?: 0) * 10
                    3 -> frac.toIntOrNull() ?: 0
                    else -> 0
                }

                val timeMs = min * 60_000L + sec * 1_000L + ms
                result.add(LyricLine(timeMs = timeMs, text = lyricText))
            }
        }

        return result.sortedBy { it.timeMs }
    }

    fun readLrcFileIfExists(audioFilePath: String?): String? {
        if (audioFilePath.isNullOrBlank()) return null

        return try {
            val audioFile = File(audioFilePath)
            if (!audioFile.exists()) return null

            val lrcFile = File(audioFile.parentFile, "${audioFile.nameWithoutExtension}.lrc")
            if (!lrcFile.exists()) return null

            lrcFile.readText()
        } catch (_: Exception) {
            null
        }
    }
}