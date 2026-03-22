package com.jidedaizhijin.myapplication.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jidedaizhijin.myapplication.Song
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.unit.dp
import com.jidedaizhijin.myapplication.ui.player.SongRow

@Composable
fun SongListSection(
    songs: List<Song>,
    filteredSongs: List<Song>,
    currentTab: String,
    currentMediaId: String?,
    isPlaying: Boolean,
    innerPadding: PaddingValues,
    onSongClick: (index: Int, song: Song) -> Unit,
    onSongLongClick: (song: Song) -> Unit
) {
    when {
        songs.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No Music")
            }
        }

        currentTab == "FAVORITES" && filteredSongs.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Favorites Yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    top = 6.dp,
                    bottom = 132.dp
                )
            ) {
                itemsIndexed(
                    items = filteredSongs,
                    key = { _, item -> item.id }
                ) { index, song ->
                    val isCurrentSong = currentMediaId == song.id.toString()
                    val isCurrentPlayingSong = isCurrentSong && isPlaying

                    SongRow(
                        song = song,
                        isHighlighted = isCurrentSong,
                        isActuallyPlaying = isCurrentPlayingSong,
                        onClick = { onSongClick(index, song) },
                        onLongClick = { onSongLongClick(song) }
                    )
                }
            }
        }
    }
}