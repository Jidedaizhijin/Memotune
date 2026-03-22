@file:OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.jidedaizhijin.myapplication

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.jidedaizhijin.myapplication.ui.home.FolderSection
import com.jidedaizhijin.myapplication.ui.home.FolderUiModel
import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.jidedaizhijin.myapplication.ui.home.MusicTopBar
import com.jidedaizhijin.myapplication.ui.home.SongListSection
import com.jidedaizhijin.myapplication.ui.player.FullPlayerScreen
import com.jidedaizhijin.myapplication.ui.player.MiniPlayerBar
import com.jidedaizhijin.myapplication.ui.player.QueueSheet
import com.jidedaizhijin.myapplication.ui.settings.FullScreenSettingsPage
import com.jidedaizhijin.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri,
    val artworkUri: Uri?,
    val filePath: String?
)

@Composable
fun pulseAccentBg(): Color {
    return if (isSystemInDarkTheme()) {
        Color(0xFF1D2633)
    } else {
        Color(0xFFEAF2FF)
    }
}

@Composable
fun pulseAccentText(): Color {
    return if (isSystemInDarkTheme()) {
        Color(0xFF8BB8FF)
    } else {
        Color(0xFF4E8DFF)
    }
}

@Composable
fun pulseActionIconTint(): Color {
    return if (isSystemInDarkTheme()) {
        Color(0xFFB8C2D1)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@UnstableApi
class MainActivity : ComponentActivity() {

    private var controller: MediaController? = null
    private var launchIntentState by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        launchIntentState = intent

        setContent {
            MyApplicationTheme {
                var ready by remember { mutableStateOf(false) }
                var splashMinDone by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(450)
                    splashMinDone = true
                }

                LaunchedEffect(Unit) {
                    val sessionToken = SessionToken(
                        this@MainActivity,
                        ComponentName(this@MainActivity, PlaybackService::class.java)
                    )

                    val future =
                        MediaController.Builder(this@MainActivity, sessionToken).buildAsync()

                    future.addListener({
                        try {
                            controller = future.get()
                            ready = true
                        } catch (_: Exception) {
                        }
                    }, MoreExecutors.directExecutor())
                }

                AnimatedVisibility(
                    visible = ready && controller != null && splashMinDone,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    MusicScreen(
                        controller = controller!!,
                        launchIntent = launchIntentState
                    )
                }

                AnimatedVisibility(
                    visible = !(ready && controller != null && splashMinDone),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchIntentState = intent
    }

    override fun onDestroy() {
        controller?.let {
            PlaybackStateStore.saveFromController(this, it)
            it.release()
        }
        controller = null
        super.onDestroy()
    }
}

@Composable
private fun MusicScreen(
    controller: MediaController,
    launchIntent: Intent?
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val darkTheme = isSystemInDarkTheme()
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasRestoredPlayback by rememberSaveable { mutableStateOf(false) }
    val statusBarBg = MaterialTheme.colorScheme.background.toArgb()

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = statusBarBg
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var folders by remember {
        mutableStateOf(emptyList<com.jidedaizhijin.myapplication.data.folder.Folder>())
    }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var createFolderText by remember { mutableStateOf("") }
    var selectedFolderId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedFolder by remember {
        mutableStateOf<com.jidedaizhijin.myapplication.data.folder.Folder?>(null)
    }
    var showFolderMenu by remember { mutableStateOf(false) }
    var showRenameFolderDialog by remember { mutableStateOf(false) }
    var renameFolderText by remember { mutableStateOf("") }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showPlayerSheet by rememberSaveable { mutableStateOf(false) }
    var showLyrics by rememberSaveable { mutableStateOf(false) }
    var showQueueSheet by rememberSaveable { mutableStateOf(false) }
    var currentTab by rememberSaveable { mutableStateOf("ALL") }
    var sortMode by rememberSaveable { mutableStateOf("date") }

    var currentMediaId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableIntStateOf(controller.repeatMode) }
    var shuffleEnabled by remember { mutableStateOf(controller.shuffleModeEnabled) }
    var progress by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var favoriteIds by remember { mutableStateOf(emptySet<String>()) }
    var hiddenIds by remember { mutableStateOf(emptySet<String>()) }
    var lyricsState by remember { mutableStateOf("No lyrics\nTap AI to generate") }
    var lyricsLoadToken by remember { mutableIntStateOf(0) }
    val apiKey = ApiSettingsStore.getApiKey(context)
    val aiEnabled = apiKey.isNotBlank()

    var longPressedSong by remember { mutableStateOf<Song?>(null) }
    var showSongMenu by remember { mutableStateOf(false) }
    var showAddToFolderSheet by remember { mutableStateOf(false) }
    var folderPickerSong by remember { mutableStateOf<Song?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val audioGranted = result[requiredPermission()] == true
        if (audioGranted) {
            scope.launch {
                songs = scanLocalMusic(context)
                favoriteIds = FavoritesStore.getFavorites(context)
                hiddenIds = HiddenSongsStore.getHiddenSongs(context)
                folders = com.jidedaizhijin.myapplication.data.folder.FolderRepository.getFolders(context)
            }
        }
    }

    LaunchedEffect(Unit) {
        val need = mutableListOf<String>()

        if (
            ContextCompat.checkSelfPermission(
                context,
                requiredPermission()
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            need += requiredPermission()
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            need += Manifest.permission.POST_NOTIFICATIONS
        }

        if (need.isNotEmpty()) {
            permissionLauncher.launch(need.toTypedArray())
        } else {
            songs = scanLocalMusic(context)
            favoriteIds = FavoritesStore.getFavorites(context)
            hiddenIds = HiddenSongsStore.getHiddenSongs(context)
            folders = com.jidedaizhijin.myapplication.data.folder.FolderRepository.getFolders(context)
        }
    }

    DisposableEffect(controller) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentMediaId = mediaItem?.mediaId
                progress = if (controller.currentPosition > 0) controller.currentPosition else 0L
                duration = if (controller.duration > 0) controller.duration else 0L
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                duration = if (controller.duration > 0) controller.duration else 0L
            }

            override fun onRepeatModeChanged(repeatModeValue: Int) {
                repeatMode = repeatModeValue
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabledValue: Boolean) {
                shuffleEnabled = shuffleModeEnabledValue
            }
        }

        controller.addListener(listener)
        isPlaying = controller.isPlaying
        currentMediaId = controller.currentMediaItem?.mediaId
        progress = if (controller.currentPosition > 0) controller.currentPosition else 0L
        duration = if (controller.duration > 0) controller.duration else 0L
        shuffleEnabled = controller.shuffleModeEnabled
        repeatMode = controller.repeatMode

        onDispose {
            controller.removeListener(listener)
        }
    }

    LaunchedEffect(isPlaying, currentMediaId) {
        while (isPlaying && currentMediaId != null) {
            progress = if (controller.currentPosition > 0) controller.currentPosition else 0L
            duration = if (controller.duration > 0) controller.duration else 0L
            delay(500)
        }
    }

    LaunchedEffect(launchIntent, currentMediaId) {
        if (
            launchIntent?.getBooleanExtra("open_player", false) == true &&
            currentMediaId != null
        ) {
            showPlayerSheet = true
        }
    }

    val filteredSongs = remember(
        songs,
        searchText,
        currentTab,
        favoriteIds,
        hiddenIds,
        sortMode
    ) {
        val visibleSongs = songs.filterNot { hiddenIds.contains(it.id.toString()) }

        val baseList = if (currentTab == "FAVORITES") {
            visibleSongs.filter { favoriteIds.contains(it.id.toString()) }
        } else {
            visibleSongs
        }

        val searched = if (searchText.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchText, ignoreCase = true) ||
                        it.artist.contains(searchText, ignoreCase = true)
            }
        }

        when (sortMode) {
            "title" -> searched.sortedBy { it.title.lowercase(Locale.getDefault()) }
            "artist" -> searched.sortedBy { it.artist.lowercase(Locale.getDefault()) }
            else -> searched
        }
    }

    val currentSong = songs.firstOrNull { it.id.toString() == currentMediaId }
    val isCurrentFavorite = currentSong?.id?.toString() in favoriteIds

    LaunchedEffect(currentSong?.id, showPlayerSheet) {
        val song = currentSong ?: return@LaunchedEffect
        if (!showPlayerSheet) return@LaunchedEffect

        lyricsLoadToken += 1
        val currentToken = lyricsLoadToken

        lyricsState = "Loading lyrics..."

        val result = LyricsRepository.getLyrics(
            context = context,
            songId = song.id,
            title = song.title,
            artist = song.artist
        )

        if (currentToken == lyricsLoadToken && currentMediaId == song.id.toString()) {
            lyricsState = result
        }
    }

    fun buildMediaItems(list: List<Song>): List<MediaItem> {
        return list.map { song ->
            MediaItem.Builder()
                .setUri(song.uri)
                .setMediaId(song.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .build()
                )
                .build()
        }
    }

    fun playSongAt(songList: List<Song>, index: Int) {
        val items = buildMediaItems(songList)
        controller.setMediaItems(items, index, 0L)
        controller.prepare()
        controller.play()
        showLyrics = false
    }

    fun restorePlaybackStateIfNeeded() {
        if (hasRestoredPlayback) return
        if (songs.isEmpty()) return

        val snapshot = PlaybackStateStore.load(context)
        if (snapshot == null) {
            hasRestoredPlayback = true
            return
        }

        val restoredQueue = snapshot.queueIds.mapNotNull { savedId ->
            songs.firstOrNull { it.id == savedId }
        }

        if (restoredQueue.isEmpty()) {
            hasRestoredPlayback = true
            return
        }

        val restoredItems = buildMediaItems(restoredQueue)

        val targetIndex = when {
            snapshot.currentSongId != null -> {
                restoredQueue.indexOfFirst { it.id == snapshot.currentSongId }
                    .takeIf { it >= 0 }
                    ?: snapshot.currentIndex
            }
            else -> snapshot.currentIndex
        }.coerceIn(0, restoredItems.lastIndex)

        val targetPosition = snapshot.currentPosition.coerceAtLeast(0L)

        try {
            controller.setMediaItems(restoredItems, targetIndex, targetPosition)
            controller.prepare()
            controller.pause()

            currentMediaId = restoredItems[targetIndex].mediaId
            progress = targetPosition
            duration = if (controller.duration > 0) controller.duration else 0L
        } catch (_: Exception) {
        }

        hasRestoredPlayback = true
    }

    fun savePlaybackStateNow() {
        PlaybackStateStore.saveFromController(context, controller)
    }

    fun togglePlayMode() {
        when {
            !shuffleEnabled && repeatMode == Player.REPEAT_MODE_OFF -> {
                controller.repeatMode = Player.REPEAT_MODE_ONE
                controller.shuffleModeEnabled = false
                repeatMode = Player.REPEAT_MODE_ONE
                shuffleEnabled = false
            }

            !shuffleEnabled && repeatMode == Player.REPEAT_MODE_ONE -> {
                controller.repeatMode = Player.REPEAT_MODE_OFF
                controller.shuffleModeEnabled = true
                repeatMode = Player.REPEAT_MODE_OFF
                shuffleEnabled = true
            }

            else -> {
                controller.repeatMode = Player.REPEAT_MODE_OFF
                controller.shuffleModeEnabled = false
                repeatMode = Player.REPEAT_MODE_OFF
                shuffleEnabled = false
            }
        }
    }

    fun cycleSortMode() {
        sortMode = when (sortMode) {
            "date" -> "title"
            "title" -> "artist"
            else -> "date"
        }
    }

    LaunchedEffect(songs, controller) {
        if (!hasRestoredPlayback && songs.isNotEmpty()) {
            restorePlaybackStateIfNeeded()
        }
    }

    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                savePlaybackStateNow()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            savePlaybackStateNow()
        }
    }

    BackHandler(enabled = showPlayerSheet && !showSettings) {
        showPlayerSheet = false
    }

    BackHandler(enabled = currentTab == "FOLDER" && selectedFolderId != null) {
        selectedFolderId = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            containerColor = Color.Transparent,
            topBar = {
                MusicTopBar(
                    showSearch = showSearch,
                    searchText = searchText,
                    currentTab = currentTab,
                    sortMode = sortMode,
                    onSearchToggle = {
                        if (showSearch) {
                            showSearch = false
                            searchText = ""
                        } else {
                            showSearch = true
                        }
                    },
                    onSearchTextChange = { searchText = it },
                    onSettingsClick = { showSettings = true },
                    onTabChange = { currentTab = it },
                    onSortClick = { cycleSortMode() }
                )
            },
            bottomBar = {},
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            if (currentTab == "FOLDER") {
                if (selectedFolderId == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "+ New Folder",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.jidedaizhijin.myapplication.data.folder.FolderRepository
                                        .createFolder(context, "New Folder ${folders.size + 1}")

                                    folders = com.jidedaizhijin.myapplication.data.folder.FolderRepository
                                        .getFolders(context)
                                }
                                .padding(vertical = 14.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = pulseAccentText()
                        )

                        folders.forEach { folder ->
                            Text(
                                text = folder.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            selectedFolderId = folder.id
                                        },
                                        onLongClick = {
                                            selectedFolder = folder
                                            renameFolderText = folder.name
                                            showFolderMenu = true
                                        }
                                    )
                                    .padding(vertical = 14.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                } else {
                    val folderSongIds = com.jidedaizhijin.myapplication.data.folder.FolderRepository
                        .getSongIdsInFolder(context, selectedFolderId!!)

                    val folderSongs = songs.filter { folderSongIds.contains(it.id) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Text(
                            text = "   Back",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFolderId = null
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = pulseAccentText()
                        )

                        SongListSection(
                            songs = songs,
                            filteredSongs = folderSongs,
                            currentTab = currentTab,
                            currentMediaId = currentMediaId,
                            isPlaying = isPlaying,
                            innerPadding = PaddingValues(0.dp),
                            onSongClick = { index, _ ->
                                playSongAt(folderSongs, index)
                            },
                            onSongLongClick = { song ->
                                longPressedSong = song
                                showSongMenu = true
                            }
                        )
                    }
                }
            } else {
                SongListSection(
                    songs = songs,
                    filteredSongs = filteredSongs,
                    currentTab = currentTab,
                    currentMediaId = currentMediaId,
                    isPlaying = isPlaying,
                    innerPadding = innerPadding,
                    onSongClick = { index, _ ->
                        playSongAt(filteredSongs, index)
                    },
                    onSongLongClick = { song ->
                        longPressedSong = song
                        showSongMenu = true
                    }
                )
            }
        }

        currentSong?.let { song ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                MiniPlayerBar(
                    song = song,
                    isPlaying = isPlaying,
                    onBarClick = { showPlayerSheet = true },
                    onSwipeUp = { showPlayerSheet = true },
                    onPreviousClick = {
                        controller.seekToPreviousMediaItem()
                        controller.play()
                    },
                    onPlayPauseClick = {
                        if (controller.isPlaying) controller.pause() else controller.play()
                    },
                    onNextClick = {
                        controller.seekToNextMediaItem()
                        controller.play()
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showPlayerSheet && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            currentSong?.let { song ->
                FullPlayerScreen(
                    song = song,
                    isPlaying = isPlaying,
                    progress = progress,
                    duration = duration,
                    lyrics = lyricsState,
                    showLyrics = showLyrics,
                    repeatMode = repeatMode,
                    shuffleEnabled = shuffleEnabled,
                    isFavorite = isCurrentFavorite,
                    aiEnabled = aiEnabled,
                    onDismiss = { showPlayerSheet = false },
                    onToggleLyrics = { showLyrics = !showLyrics },
                    onSeek = { newValue: Float ->
                        controller.seekTo(newValue.toLong())
                        progress = newValue.toLong()
                    },
                    onPrevious = {
                        val index = controller.currentMediaItemIndex
                        val count = controller.mediaItemCount

                        if (count > 0) {
                            val newIndex =
                                if (index <= 0) count - 1
                                else index - 1

                            controller.seekToDefaultPosition(newIndex)
                            controller.play()
                        }
                    },
                    onPlayPause = {
                        if (controller.isPlaying) controller.pause() else controller.play()
                    },
                    onNext = {
                        val index = controller.currentMediaItemIndex
                        val count = controller.mediaItemCount

                        if (count > 0) {
                            val newIndex =
                                if (index >= count - 1) 0
                                else index + 1

                            controller.seekToDefaultPosition(newIndex)
                            controller.play()
                        }
                    },
                    onToggleRepeatMode = { togglePlayMode() },
                    onShowQueue = { showQueueSheet = true },
                    onToggleFavorite = {
                        FavoritesStore.toggleFavorite(context, song.id)
                        favoriteIds = FavoritesStore.getFavorites(context)
                    },
                    onRefreshLyrics = {
                        scope.launch {
                            lyricsLoadToken += 1
                            val currentToken = lyricsLoadToken

                            lyricsState = "AI generating..."

                            val result = LyricsRepository.refreshLyrics(
                                context = context,
                                songId = song.id,
                                title = song.title,
                                artist = song.artist
                            )

                            if (currentToken == lyricsLoadToken && currentMediaId == song.id.toString()) {
                                lyricsState = result
                            }
                        }
                    }
                )
            }
        }

        if (showSettings) {
            FullScreenSettingsPage(
                songs = songs,
                hiddenIds = hiddenIds,
                onUnhideSong = { songId ->
                    HiddenSongsStore.unhideSong(context, songId)
                    hiddenIds = HiddenSongsStore.getHiddenSongs(context)
                },
                onDismiss = { showSettings = false }
            )
        }
    }

    if (showQueueSheet) {
        QueueSheet(
            songs = songs,
            currentMediaId = currentMediaId,
            onSongClick = { index, _ ->
                playSongAt(songs, index)
                showQueueSheet = false
            },
            onDismiss = { showQueueSheet = false }
        )
    }

    if (showSongMenu && longPressedSong != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showSongMenu = false
                longPressedSong = null
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val song = longPressedSong!!

            Column(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = song.title,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (favoriteIds.contains(song.id.toString())) "Cancel Favorite" else "Favorite",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            FavoritesStore.toggleFavorite(context, song.id)
                            favoriteIds = FavoritesStore.getFavorites(context)
                            showSongMenu = false
                            longPressedSong = null
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )

                Text(
                    text = "Hide",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HiddenSongsStore.hideSong(context, song.id)
                            hiddenIds = HiddenSongsStore.getHiddenSongs(context)
                            showSongMenu = false
                            longPressedSong = null
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )

                Text(
                    text = "Add to Folder",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            folders = com.jidedaizhijin.myapplication.data.folder.FolderRepository
                                .getFolders(context)
                            folderPickerSong = song
                            showSongMenu = false
                            longPressedSong = null
                            showAddToFolderSheet = true
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }
    }

    if (showAddToFolderSheet && folderPickerSong != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddToFolderSheet = false
                folderPickerSong = null
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val song = folderPickerSong!!

            Column(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Add to Folder",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                if (folders.isEmpty()) {
                    Text(
                        text = "No folders yet",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    folders.forEach { folder ->
                        Text(
                            text = folder.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.jidedaizhijin.myapplication.data.folder.FolderRepository
                                        .addSongToFolder(context, folder.id, song.id)

                                    showAddToFolderSheet = false
                                    folderPickerSong = null
                                }
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showFolderMenu && selectedFolder != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showFolderMenu = false
                selectedFolder = null
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val folder = selectedFolder!!

            Column(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    text = folder.name,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Rename",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showFolderMenu = false
                            showRenameFolderDialog = true
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )

                Text(
                    text = "Delete",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            com.jidedaizhijin.myapplication.data.folder.FolderRepository
                                .deleteFolder(context, folder.id)

                            folders = com.jidedaizhijin.myapplication.data.folder.FolderRepository
                                .getFolders(context)

                            if (selectedFolderId == folder.id) {
                                selectedFolderId = null
                            }

                            showFolderMenu = false
                            selectedFolder = null
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }
        }
    }

    if (showRenameFolderDialog && selectedFolder != null) {
        AlertDialog(
            onDismissRequest = {
                showRenameFolderDialog = false
            },
            title = {
                Text("Rename Folder")
            },
            text = {
                TextField(
                    value = renameFolderText,
                    onValueChange = { renameFolderText = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Text(
                    text = "Save",
                    modifier = Modifier.clickable {
                        val newName = renameFolderText.trim()
                        if (newName.isNotBlank()) {
                            com.jidedaizhijin.myapplication.data.folder.FolderRepository
                                .renameFolder(context, selectedFolder!!.id, newName)

                            folders = com.jidedaizhijin.myapplication.data.folder.FolderRepository
                                .getFolders(context)
                        }

                        showRenameFolderDialog = false
                        selectedFolder = null
                    }
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    modifier = Modifier.clickable {
                        showRenameFolderDialog = false
                    }
                )
            }
        )
    }
}

private suspend fun scanLocalMusic(context: Context): List<Song> = withContext(Dispatchers.IO) {
    val songs = mutableListOf<Song>()
    val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DATA
    )

    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
    val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

    context.contentResolver.query(
        collection,
        projection,
        selection,
        null,
        sortOrder
    )?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
        val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            val title = cursor.getString(titleCol) ?: "Unknown"
            val artist = cursor.getString(artistCol) ?: "Unknown Artist"
            val duration = cursor.getLong(durationCol)
            val albumId = cursor.getLong(albumIdCol)
            val filePath = cursor.getString(dataCol)
            val uri = ContentUris.withAppendedId(collection, id)
            val artworkUri = Uri.parse("content://media/external/audio/albumart/$albumId")

            songs.add(
                Song(
                    id = id,
                    title = title,
                    artist = artist,
                    duration = duration,
                    uri = uri,
                    artworkUri = artworkUri,
                    filePath = filePath
                )
            )
        }
    }

    return@withContext songs
}

private fun requiredPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "00:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
