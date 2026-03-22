
package com.jidedaizhijin.myapplication.data.folder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object FolderRepository {

    private const val PREFS_NAME = "folder_prefs"
    private const val KEY_FOLDERS = "folders"
    private const val KEY_FOLDER_SONGS = "folder_songs"

    fun getFolders(context: Context): List<Folder> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_FOLDERS, "[]") ?: "[]"
        val array = JSONArray(json)

        val result = mutableListOf<Folder>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result += Folder(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
        }
        return result.sortedBy { it.createdAt }
    }

    fun createFolder(context: Context, name: String): Folder {
        val trimmedName = name.trim()
        require(trimmedName.isNotBlank()) { "Folder name cannot be blank." }

        val folders = getFolders(context).toMutableList()

        val newFolder = Folder(
            id = System.currentTimeMillis(),
            name = trimmedName
        )

        folders += newFolder
        saveFolders(context, folders)

        return newFolder
    }

    fun renameFolder(context: Context, folderId: Long, newName: String) {
        val trimmedName = newName.trim()
        require(trimmedName.isNotBlank()) { "Folder name cannot be blank." }

        val updated = getFolders(context).map {
            if (it.id == folderId) it.copy(name = trimmedName) else it
        }
        saveFolders(context, updated)
    }

    fun deleteFolder(context: Context, folderId: Long) {
        val folders = getFolders(context).filterNot { it.id == folderId }
        saveFolders(context, folders)

        val folderSongs = getFolderSongs(context).filterNot { it.folderId == folderId }
        saveFolderSongs(context, folderSongs)
    }

    fun addSongToFolder(context: Context, folderId: Long, songId: Long) {
        val mappings = getFolderSongs(context).toMutableList()

        val exists = mappings.any { it.folderId == folderId && it.songId == songId }
        if (!exists) {
            mappings += FolderSong(folderId = folderId, songId = songId)
            saveFolderSongs(context, mappings)
        }
    }

    fun removeSongFromFolder(context: Context, folderId: Long, songId: Long) {
        val updated = getFolderSongs(context)
            .filterNot { it.folderId == folderId && it.songId == songId }
        saveFolderSongs(context, updated)
    }

    fun getSongIdsInFolder(context: Context, folderId: Long): List<Long> {
        return getFolderSongs(context)
            .filter { it.folderId == folderId }
            .map { it.songId }
    }

    fun isSongInFolder(context: Context, folderId: Long, songId: Long): Boolean {
        return getFolderSongs(context)
            .any { it.folderId == folderId && it.songId == songId }
    }

    private fun getFolderSongs(context: Context): List<FolderSong> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_FOLDER_SONGS, "[]") ?: "[]"
        val array = JSONArray(json)

        val result = mutableListOf<FolderSong>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result += FolderSong(
                folderId = obj.getLong("folderId"),
                songId = obj.getLong("songId")
            )
        }
        return result
    }

    private fun saveFolders(context: Context, folders: List<Folder>) {
        val array = JSONArray()
        folders.forEach { folder ->
            array.put(
                JSONObject().apply {
                    put("id", folder.id)
                    put("name", folder.name)
                    put("createdAt", folder.createdAt)
                }
            )
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FOLDERS, array.toString())
            .apply()
    }

    private fun saveFolderSongs(context: Context, mappings: List<FolderSong>) {
        val array = JSONArray()
        mappings.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("folderId", item.folderId)
                    put("songId", item.songId)
                }
            )
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FOLDER_SONGS, array.toString())
            .apply()
    }
}