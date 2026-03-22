package com.jidedaizhijin.myapplication

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object AiLyricsProvider {

    private const val DEFAULT_BASE_URL = "https://api.deepseek.com/chat/completions"
    private const val DEFAULT_MODEL = "deepseek-chat"

    private fun realBaseUrl(baseUrl: String): String {
        return baseUrl.trim().ifBlank { DEFAULT_BASE_URL }
    }

    private fun realModel(model: String): String {
        return model.trim().ifBlank { DEFAULT_MODEL }
    }

    suspend fun fetchLyrics(
        apiKey: String,
        baseUrl: String,
        model: String,
        title: String,
        artist: String
    ): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) return@withContext ""

            val originalTitle = title.trim()
            val originalArtist = artist.trim()

            val firstTry = requestLyrics(
                apiKey = apiKey,
                baseUrl = baseUrl,
                model = model,
                title = originalTitle,
                artist = originalArtist
            )

            if (firstTry.isNotBlank()) {
                return@withContext formatLyrics(
                    apiKey = apiKey,
                    baseUrl = baseUrl,
                    model = model,
                    rawLyrics = firstTry
                )
            }

            val cleanTitle = normalizeTitle(originalTitle)
            val cleanArtist = normalizeArtist(originalArtist)

            if (cleanTitle != originalTitle || cleanArtist != originalArtist) {
                val secondTry = requestLyrics(
                    apiKey = apiKey,
                    baseUrl = baseUrl,
                    model = model,
                    title = cleanTitle,
                    artist = cleanArtist
                )

                if (secondTry.isNotBlank()) {
                    return@withContext formatLyrics(
                        apiKey = apiKey,
                        baseUrl = baseUrl,
                        model = model,
                        rawLyrics = secondTry
                    )
                }
            }

            ""
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun generateHighlight(
        apiKey: String,
        baseUrl: String,
        model: String,
        title: String,
        artist: String,
        lyrics: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext buildFallbackHighlight(title, artist)
            }

            val url = URL(realBaseUrl(baseUrl))
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            val systemPrompt = """
你是一个二次元音乐迷。

请根据歌曲名、歌手，必要时结合歌词和作品背景，生成一句很短的中文金句。

要求：
1. 这句话要像这首歌的代表性 slogan、作品口号、海报标语。
2. 要短，最好 6 到 14 个字，最多不超过 16 个字。
3. 要一下子让人知道这首歌的感觉。
4. 不要写成听众评论。
5. 不要写“这首歌”“让人”“仿佛”“像是”这种句式。
6. 可以带一点二次元、热血、恋爱、青春、夜晚、飞行、远方、宿命感。
7. 如果拿不准，也要尽量给出一句像歌的短句。
8. 只返回一句。
""".trimIndent()

            val userPrompt = buildString {
                append("歌曲名：$title\n")
                append("歌手：$artist\n")
                if (lyrics.isNotBlank()) {
                    append("歌词参考：\n")
                    append(lyrics.take(800))
                }
            }.trim()

            val body = JSONObject().apply {
                put("model", realModel(model))
                put("temperature", 0.7)
                put("stream", false)
                put(
                    "messages",
                    JSONArray()
                        .put(JSONObject().put("role", "system").put("content", systemPrompt))
                        .put(JSONObject().put("role", "user").put("content", userPrompt))
                )
            }

            OutputStreamWriter(connection.outputStream).use {
                it.write(body.toString())
                it.flush()
            }

            val code = connection.responseCode
            if (code !in 200..299) {
                return@withContext buildFallbackHighlight(title, artist)
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(responseText)

            val content = root
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .optString("content", "")
                .trim()

            val cleaned = sanitizeLyricsResult(content)
                .lineSequence()
                .firstOrNull { it.trim().isNotBlank() }
                ?.trim()
                .orEmpty()

            cleaned.ifBlank { buildFallbackHighlight(title, artist) }
        } catch (_: Exception) {
            buildFallbackHighlight(title, artist)
        }
    }

    suspend fun formatLyrics(
        apiKey: String,
        baseUrl: String,
        model: String,
        rawLyrics: String
    ): String = withContext(Dispatchers.IO) {
        try {
            if (rawLyrics.isBlank()) return@withContext ""

            val cleanedInput = preprocessRawLyrics(rawLyrics)
            if (cleanedInput.isBlank()) return@withContext rawLyrics

            val normalizedInput = normalizeMixedLyricsLines(cleanedInput)
            val formatted = requestFormat(
                apiKey = apiKey,
                baseUrl = baseUrl,
                model = model,
                rawLyrics = normalizedInput
            )
            val base = if (formatted.isBlank()) normalizedInput else formatted

            normalizeMixedLyricsLines(base)
        } catch (_: Exception) {
            normalizeMixedLyricsLines(rawLyrics)
        }
    }

    fun buildFallbackHighlight(title: String, artist: String): String {
        return "这一句留给这首歌"
    }

    private suspend fun requestLyrics(
        apiKey: String,
        baseUrl: String,
        model: String,
        title: String,
        artist: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val url = URL(realBaseUrl(baseUrl))

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            val systemPrompt = """
你是一名歌词助手。

请尽量准确返回歌曲歌词正文。

规则：
1. 只返回歌词正文。
2. 不要包含作词、作曲、编曲、专辑、制作人、演唱、录音、混音、监制等信息。
3. 不要输出解释、说明、道歉、版权提示。
4. 如果无法较有把握地返回歌词，就返回空字符串。
5. 只返回 JSON。

JSON 格式：
{
"lyrics": ""
}
""".trimIndent()

            val userPrompt = """
歌曲名：$title
歌手：$artist

请返回：
{
"lyrics": "完整歌词正文"
}
""".trimIndent()

            val body = JSONObject().apply {
                put("model", realModel(model))
                put("temperature", 0.2)
                put("stream", false)
                put("response_format", JSONObject().put("type", "json_object"))
                put(
                    "messages",
                    JSONArray()
                        .put(JSONObject().put("role", "system").put("content", systemPrompt))
                        .put(JSONObject().put("role", "user").put("content", userPrompt))
                )
            }

            OutputStreamWriter(connection.outputStream).use {
                it.write(body.toString())
                it.flush()
            }

            val code = connection.responseCode
            val responseText = if (code in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            if (code !in 200..299) return@withContext ""

            val root = JSONObject(responseText)
            val content = root
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .optString("content", "")

            if (content.isBlank()) return@withContext ""

            val json = try {
                JSONObject(content)
            } catch (_: Exception) {
                return@withContext ""
            }

            val lyrics = json.optString("lyrics", "").trim()
            val cleanedLyrics = sanitizeLyricsResult(lyrics)

            if (cleanedLyrics.isBlank()) return@withContext ""

            cleanedLyrics
        } catch (_: Exception) {
            ""
        }
    }

    private suspend fun requestFormat(
        apiKey: String,
        baseUrl: String,
        model: String,
        rawLyrics: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val url = URL(realBaseUrl(baseUrl))

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10000
                readTimeout = 20000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            val systemPrompt = """
You are a lyrics formatting assistant for a mobile music player.

Task:
Convert messy lyrics into clean, readable, mobile-friendly lyric lines.

Hard rules:
1. Do not rewrite the meaning of the lyrics.
2. Remove timestamps like [00:12.34].
3. Remove metadata lines such as:
composer, lyricist, arranger, producer, album, credits, songwriter,
作词, 作曲, 编曲, 制作人, 演唱, 录音, 混音, 监制.
4. Remove duplicated empty lines, extra spaces, and obvious formatting noise.
5. Keep the original lyric order unchanged.
6. Do not add titles, labels, markdown, bullets, numbering, or explanations.
7. Output only the final formatted lyrics text.

Formatting rules:
8. If the lyrics are Chinese, strongly split them into short natural phrases, one phrase per line.
9. If the lyrics are non-Chinese (English, Japanese, Korean, etc.), you MUST:
- split the original lyrics into short natural phrases,
- keep one original phrase per line,
- add ONE Chinese translation line immediately below each original line,
- preserve the original order.
10. For non-Chinese lyrics, the final output should look like:
Original line
中文翻译
Original line
中文翻译
11. If the input is already well formatted, still enforce the above display style.
12. Never return one huge paragraph.
""".trimIndent()

            val body = JSONObject().apply {
                put("model", realModel(model))
                put("temperature", 0.1)
                put("stream", false)
                put(
                    "messages",
                    JSONArray()
                        .put(JSONObject().put("role", "system").put("content", systemPrompt))
                        .put(JSONObject().put("role", "user").put("content", rawLyrics))
                )
            }

            OutputStreamWriter(connection.outputStream).use {
                it.write(body.toString())
                it.flush()
            }

            val code = connection.responseCode
            if (code !in 200..299) return@withContext rawLyrics

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(response)

            root
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .optString("content", "")
                .trim()
        } catch (_: Exception) {
            rawLyrics
        }
    }

    private fun preprocessRawLyrics(raw: String): String {
        if (raw.isBlank()) return ""

        val normalized = raw
            .replace('\u2009', ' ')
            .replace('\u200A', ' ')
            .replace('\u2006', ' ')
            .replace('\u2007', ' ')
            .replace('\u202F', ' ')
            .replace(Regex("""\[\d{2}:\d{2}(?:\.\d{2,3})?]"""), "")

        val blockedKeywords = listOf(
            "作词", "作曲", "编曲", "制作人", "录音", "混音", "监制", "演唱",
            "词", "曲", "编", "制作", "录制", "翻唱", "原唱", "作詞", "作曲家",
            "composer", "lyricist", "arranger", "producer", "songwriter",
            "music by", "lyrics by", "arranged by", "album", "credits", "vocal"
        )

        return normalized.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { line ->
                val lower = line.lowercase()
                val hasBlockedKeyword = blockedKeywords.any { lower.contains(it) }
                val looksLikeCreditLine =
                    Regex("""^[\p{L}\p{N}\u4e00-\u9fff\u3040-\u30ff\s/&+.\-]{1,20}\s*[:：]\s*.+$""")
                        .matches(line)
                line.length <= 60 && (hasBlockedKeyword || looksLikeCreditLine)
            }
            .joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun normalizeMixedLyricsLines(text: String): String {
        if (text.isBlank()) return ""

        val output = mutableListOf<String>()

        text.lines().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) return@forEach

            val safeLine = line.replace(Regex("[\\u2000-\\u200F]"), " ")

            val isMetaLine =
                safeLine == "Lyrics formatted by AI" ||
                        safeLine == "Lyrics generated by AI" ||
                        safeLine == "AI failed" ||
                        safeLine.startsWith("✨")

            if (isMetaLine) {
                output += safeLine
                return@forEach
            }

            val lower = safeLine.lowercase()
            val blocked = listOf(
                "作词", "作曲", "编曲", "制作人", "录音", "混音", "监制", "演唱",
                "作詞", "作曲家", "翻唱", "原唱",
                "composer", "lyricist", "arranger", "producer", "songwriter",
                "music by", "lyrics by", "arranged by", "credits", "album", "vocal"
            )

            val looksLikeCreditLine =
                Regex("""^[\p{L}\p{N}\u4e00-\u9fff\u3040-\u30ff\s/&+.\-]{1,20}\s*[:：]\s*.+$""")
                    .matches(safeLine)

            if ((blocked.any { lower.contains(it) } || looksLikeCreditLine) && safeLine.length <= 60) {
                return@forEach
            }

            val splitMatch = Regex(
                """^([\u3040-\u30ff\u3400-\u9fffA-Za-z0-9\s　、。！？…「」『』ー～\-]+?)[\s]+([\u4e00-\u9fff].*)$"""
            ).find(safeLine)

            if (splitMatch != null) {
                val original = splitMatch.groupValues[1].trim()
                val translation = splitMatch.groupValues[2].trim()
                if (original.isNotBlank()) output += original
                if (translation.isNotBlank()) output += translation
            } else {
                output += safeLine
            }
        }

        return output.joinToString("\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    private fun sanitizeLyricsResult(text: String): String {
        if (text.isBlank()) return ""

        val lower = text.lowercase()
        val blockedHints = listOf(
            "i can't provide",
            "i cannot provide",
            "sorry",
            "i’m sorry",
            "cannot assist",
            "copyright",
            "版权",
            "不能提供",
            "无法提供",
            "抱歉",
            "对不起"
        )

        if (blockedHints.any { lower.contains(it) }) {
            return ""
        }

        return text
            .replace(Regex("^```json", RegexOption.IGNORE_CASE), "")
            .replace("```", "")
            .trim()
    }

    private fun normalizeTitle(raw: String): String {
        return raw
            .replace(Regex("\\(.*?\\)"), "")
            .replace(Regex("（.*?）"), "")
            .replace(Regex("\\[.*?]"), "")
            .replace(Regex("【.*?】"), "")
            .replace(Regex("feat\\.?\\s.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("ft\\.?\\s.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("-\\s*live.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("-\\s*remaster.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("-\\s*version.*", RegexOption.IGNORE_CASE), "")
            .replace("伴奏", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun normalizeArtist(raw: String): String {
        return raw
            .replace(Regex("feat\\.?\\s.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("ft\\.?\\s.*", RegexOption.IGNORE_CASE), "")
            .replace("&", ",")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}