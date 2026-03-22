package com.jidedaizhijin.myapplication

import android.content.Context

object FavoritesStore {

    private const val PREFS_NAME = "pulse_favorites"
    private const val KEY_FAVORITES = "favorite_song_ids"

    fun getFavorites(context: Context): Set<String> {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(context: Context, songId: Long): Boolean {
        return getFavorites(context).contains(songId.toString())
    }

    fun toggleFavorite(context: Context, songId: Long): Boolean {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getFavorites(context).toMutableSet()
        val id = songId.toString()

        val nowFavorite = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }

        sp.edit().putStringSet(KEY_FAVORITES, current).apply()
        return nowFavorite
    }
}