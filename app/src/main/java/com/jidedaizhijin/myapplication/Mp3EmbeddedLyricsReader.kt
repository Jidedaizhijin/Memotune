package com.jidedaizhijin.myapplication

import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.Charset
import kotlin.math.min

object Mp3EmbeddedLyricsReader {

    fun readLyrics(audioFilePath: String?): String? {
        if (audioFilePath.isNullOrBlank()) return null

        val file = File(audioFilePath)
        if (!file.exists()) return null
        if (!file.extension.equals("mp3", ignoreCase = true)) return null

        return try {
            RandomAccessFile(file, "r").use { raf ->
                val header = ByteArray(10)
                raf.readFully(header)

                if (String(header, 0, 3, Charsets.ISO_8859_1) != "ID3") {
                    return null
                }

                val majorVersion = header[3].toInt() and 0xFF
                if (majorVersion !in 3..4) {
                    return null
                }

                val tagSize = synchsafeToInt(header, 6)
                val tagData = ByteArray(tagSize)
                raf.readFully(tagData)

                // 优先 SYLT（同步歌词）
                val sylt = readSyltFrame(tagData, majorVersion)
                if (!sylt.isNullOrBlank()) return sylt

                // 再退回 USLT（普通歌词）
                readUsltFrame(tagData, majorVersion)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readUsltFrame(tagData: ByteArray, majorVersion: Int): String? {
        var offset = 0

        while (offset + 10 <= tagData.size) {
            val frameId = String(tagData, offset, 4, Charsets.ISO_8859_1)
            if (frameId.all { it == '\u0000' }) break

            val frameSize = when (majorVersion) {
                4 -> synchsafeToInt(tagData, offset + 4)
                else -> bigEndianInt(tagData, offset + 4)
            }

            if (frameSize <= 0) break

            val frameDataStart = offset + 10
            val frameDataEnd = frameDataStart + frameSize

            if (frameDataEnd > tagData.size) break

            if (frameId == "USLT") {
                val frameData = tagData.copyOfRange(frameDataStart, frameDataEnd)
                return parseUslt(frameData)
            }

            offset = frameDataEnd
        }

        return null
    }

    private fun readSyltFrame(tagData: ByteArray, majorVersion: Int): String? {
        var offset = 0

        while (offset + 10 <= tagData.size) {
            val frameId = String(tagData, offset, 4, Charsets.ISO_8859_1)
            if (frameId.all { it == '\u0000' }) break

            val frameSize = when (majorVersion) {
                4 -> synchsafeToInt(tagData, offset + 4)
                else -> bigEndianInt(tagData, offset + 4)
            }

            if (frameSize <= 0) break

            val frameDataStart = offset + 10
            val frameDataEnd = frameDataStart + frameSize

            if (frameDataEnd > tagData.size) break

            if (frameId == "SYLT") {
                val frameData = tagData.copyOfRange(frameDataStart, frameDataEnd)
                val parsed = parseSylt(frameData)
                if (!parsed.isNullOrBlank()) return parsed
            }

            offset = frameDataEnd
        }

        return null
    }

    private fun parseUslt(frameData: ByteArray): String? {
        if (frameData.size < 4) return null

        val encoding = frameData[0].toInt() and 0xFF
        val charset = when (encoding) {
            0 -> Charsets.ISO_8859_1
            1 -> Charsets.UTF_16
            2 -> Charset.forName("UTF-16BE")
            3 -> Charsets.UTF_8
            else -> Charsets.UTF_8
        }

        var offset = 1

        // language, 3 bytes
        offset += 3
        if (offset >= frameData.size) return null

        // skip content descriptor
        offset = skipDescriptor(frameData, offset, encoding)
        if (offset >= frameData.size) return null

        val lyricsBytes = frameData.copyOfRange(offset, frameData.size)
        val lyrics = try {
            String(lyricsBytes, charset)
        } catch (_: Exception) {
            return null
        }

        return lyrics
            .replace("\u0000", "")
            .trim()
            .ifBlank { null }
    }

    private fun parseSylt(frameData: ByteArray): String? {
        // SYLT:
        // [0] text encoding
        // [1..3] language
        // [4] timestamp format (1 = MPEG frames, 2 = milliseconds)
        // [5] content type
        // [descriptor]
        // repeated: text + 0-term + 4-byte timestamp

        if (frameData.size < 6) return null

        val encoding = frameData[0].toInt() and 0xFF
        val charset = when (encoding) {
            0 -> Charsets.ISO_8859_1
            1 -> Charsets.UTF_16
            2 -> Charset.forName("UTF-16BE")
            3 -> Charsets.UTF_8
            else -> Charsets.UTF_8
        }

        val timestampFormat = frameData[4].toInt() and 0xFF

        // 这里只稳定支持 milliseconds
        if (timestampFormat != 2) {
            return null
        }

        var offset = 6

        // skip content descriptor
        offset = skipDescriptor(frameData, offset, encoding)
        if (offset >= frameData.size) return null

        val result = mutableListOf<String>()

        while (offset < frameData.size) {
            val read = readTerminatedText(frameData, offset, encoding, charset) ?: break
            val text = read.first.trim()
            offset = read.second

            if (offset + 4 > frameData.size) break

            val timestamp = bigEndianInt(frameData, offset)
            offset += 4

            if (text.isNotBlank()) {
                result += "${formatLrcTime(timestamp.toLong())}$text"
            }
        }

        return result
            .joinToString("\n")
            .trim()
            .ifBlank { null }
    }

    private fun readTerminatedText(
        data: ByteArray,
        start: Int,
        encoding: Int,
        charset: Charset
    ): Pair<String, Int>? {
        var i = start

        return when (encoding) {
            0, 3 -> {
                while (i < data.size && data[i].toInt() != 0) {
                    i++
                }
                val textBytes = data.copyOfRange(start, min(i, data.size))
                val text = try {
                    String(textBytes, charset)
                } catch (_: Exception) {
                    return null
                }
                Pair(text, min(i + 1, data.size))
            }

            1, 2 -> {
                while (i + 1 < data.size) {
                    if (data[i].toInt() == 0 && data[i + 1].toInt() == 0) {
                        val textBytes = data.copyOfRange(start, i)
                        val text = try {
                            String(textBytes, charset)
                        } catch (_: Exception) {
                            return null
                        }
                        return Pair(text, min(i + 2, data.size))
                    }
                    i += 2
                }

                val textBytes = data.copyOfRange(start, data.size)
                val text = try {
                    String(textBytes, charset)
                } catch (_: Exception) {
                    return null
                }
                Pair(text, data.size)
            }

            else -> null
        }
    }

    private fun skipDescriptor(data: ByteArray, start: Int, encoding: Int): Int {
        var i = start

        return when (encoding) {
            0, 3 -> {
                while (i < data.size && data[i].toInt() != 0) {
                    i++
                }
                min(i + 1, data.size)
            }

            1, 2 -> {
                while (i + 1 < data.size) {
                    if (data[i].toInt() == 0 && data[i + 1].toInt() == 0) {
                        return min(i + 2, data.size)
                    }
                    i += 2
                }
                data.size
            }

            else -> data.size
        }
    }

    private fun formatLrcTime(ms: Long): String {
        val totalCentis = ms / 10
        val minutes = totalCentis / 6000
        val seconds = (totalCentis % 6000) / 100
        val centis = totalCentis % 100
        return "[%02d:%02d.%02d]".format(minutes, seconds, centis)
    }

    private fun synchsafeToInt(bytes: ByteArray, start: Int): Int {
        return ((bytes[start].toInt() and 0x7F) shl 21) or
                ((bytes[start + 1].toInt() and 0x7F) shl 14) or
                ((bytes[start + 2].toInt() and 0x7F) shl 7) or
                (bytes[start + 3].toInt() and 0x7F)
    }

    private fun bigEndianInt(bytes: ByteArray, start: Int): Int {
        return ((bytes[start].toInt() and 0xFF) shl 24) or
                ((bytes[start + 1].toInt() and 0xFF) shl 16) or
                ((bytes[start + 2].toInt() and 0xFF) shl 8) or
                (bytes[start + 3].toInt() and 0xFF)
    }
}