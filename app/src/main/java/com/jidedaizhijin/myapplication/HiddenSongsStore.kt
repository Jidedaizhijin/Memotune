package com.jidedaizhijin.myapplication

import android.content.Context

object HiddenSongsStore {

    private const val PREFS_NAME = "hidden_songs_prefs"
    private const val KEY_HIDDEN_IDS = "hidden_ids"

    fun getHiddenSongs(context: Context): Set<String> {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_HIDDEN_IDS, emptySet())
            ?.toSet()
            ?: emptySet()
    }

    fun hideSong(context: Context, songId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_HIDDEN_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(songId.toString())
        prefs.edit().putStringSet(KEY_HIDDEN_IDS, current).apply()
    }

    fun unhideSong(context: Context, songId: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_HIDDEN_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(songId.toString())
        prefs.edit().putStringSet(KEY_HIDDEN_IDS, current).apply()
    }

    fun clearHiddenSongs(context: Context) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HIDDEN_IDS)
            .apply()
    }
}