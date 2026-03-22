package com.jidedaizhijin.myapplication

import android.content.Context
import androidx.media3.session.MediaController
import org.json.JSONArray

object PlaybackStateStore {

    private const val PREFS_NAME = "playback_state"
    private const val KEY_QUEUE_IDS = "queue_ids"
    private const val KEY_CURRENT_INDEX = "current_index"
    private const val KEY_CURRENT_POSITION = "current_position"
    private const val KEY_CURRENT_ID = "current_id"

    data class PlaybackSnapshot(
        val queueIds: List<Long>,
        val currentIndex: Int,
        val currentPosition: Long,
        val currentSongId: Long?
    )

    fun saveFromController(
        context: Context,
        controller: MediaController
    ) {
        try {
            val queueArray = JSONArray()

            for (i in 0 until controller.mediaItemCount) {
                val item = controller.getMediaItemAt(i)
                val id = item.mediaId.toLongOrNull()
                if (id != null) {
                    queueArray.put(id)
                }
            }

            val currentId = controller.currentMediaItem?.mediaId?.toLongOrNull()

            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_QUEUE_IDS, queueArray.toString())
                .putInt(KEY_CURRENT_INDEX, controller.currentMediaItemIndex.coerceAtLeast(0))
                .putLong(KEY_CURRENT_POSITION, controller.currentPosition.coerceAtLeast(0L))
                .putLong(KEY_CURRENT_ID, currentId ?: -1L)
                .apply()
        } catch (_: Exception) {
        }
    }

    fun load(context: Context): PlaybackSnapshot? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val queueRaw = prefs.getString(KEY_QUEUE_IDS, null) ?: return null

            val jsonArray = JSONArray(queueRaw)
            val queueIds = mutableListOf<Long>()

            for (i in 0 until jsonArray.length()) {
                queueIds.add(jsonArray.getLong(i))
            }

            if (queueIds.isEmpty()) return null

            val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
            val currentPosition = prefs.getLong(KEY_CURRENT_POSITION, 0L)
            val savedId = prefs.getLong(KEY_CURRENT_ID, -1L)

            PlaybackSnapshot(
                queueIds = queueIds,
                currentIndex = currentIndex,
                currentPosition = currentPosition,
                currentSongId = if (savedId == -1L) null else savedId
            )
        } catch (_: Exception) {
            null
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}