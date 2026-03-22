@file:OptIn(ExperimentalMaterial3Api::class)

package com.jidedaizhijin.myapplication.ui.player

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.jidedaizhijin.myapplication.Song
import com.jidedaizhijin.myapplication.formatDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun FullPlayerScreen(
    song: Song,
    isPlaying: Boolean,
    progress: Long,
    duration: Long,
    lyrics: String,
    showLyrics: Boolean,
    repeatMode: Int,
    shuffleEnabled: Boolean,
    isFavorite: Boolean,
    aiEnabled: Boolean,
    onDismiss: () -> Unit,
    onToggleLyrics: () -> Unit,
    onSeek: (Float) -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleRepeatMode: () -> Unit,
    onShowQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRefreshLyrics: () -> Unit
) {
    var totalDrag by remember { mutableFloatStateOf(0f) }
    var aiLoading by remember { mutableStateOf(false) }

    var coverDragTarget by remember(song.id) { mutableFloatStateOf(0f) }
    var coverDragging by remember(song.id) { mutableStateOf(false) }
    val coverOffsetX by animateFloatAsState(
        targetValue = coverDragTarget,
        animationSpec = tween(durationMillis = if (coverDragging) 0 else 180),
        label = "cover_offset"
    )

    val transition = rememberInfiniteTransition(label = "thumb")
    val thumbSize by transition.animateFloat(
        initialValue = 11f,
        targetValue = 13f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thumb"
    )

    val highlight = remember(lyrics) {
        lyrics.lines().firstOrNull { it.trim().startsWith("✨") }?.trim()
    }

    val lyricLines = remember(lyrics) {
        lyrics.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("✨") }
    }

    LaunchedEffect(lyrics) {
        if (aiLoading) {
            delay(300)
            aiLoading = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, drag ->
                        totalDrag += drag
                    },
                    onDragEnd = {
                        if (totalDrag > 140f) {
                            onDismiss()
                        }
                        totalDrag = 0f
                    }
                )
            },
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        if (showLyrics) {
                            onToggleLyrics()
                        } else {
                            onDismiss()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f),
                contentAlignment = Alignment.Center
            ) {
                if (showLyrics) {
                    if (aiLoading || lyrics.trim() == "AI generating...") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AI generating...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        FocusLyricsView(lines = lyricLines)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .aspectRatio(1f)
                                .graphicsLayer {
                                    translationX = coverOffsetX
                                }
                                .clip(RoundedCornerShape(30.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .pointerInput(song.id) {
                                    detectHorizontalDragGestures(
                                        onDragStart = {
                                            coverDragging = true
                                        },
                                        onHorizontalDrag = { _, dragAmount ->
                                            coverDragTarget += dragAmount
                                        },
                                        onDragEnd = {
                                            coverDragging = false
                                            when {
                                                coverDragTarget <= -120f -> {
                                                    coverDragTarget = 0f
                                                    onNext()
                                                }
                                                coverDragTarget >= 120f -> {
                                                    coverDragTarget = 0f
                                                    onPrevious()
                                                }
                                                else -> {
                                                    coverDragTarget = 0f
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            coverDragging = false
                                            coverDragTarget = 0f
                                        }
                                    )
                                }
                                .clickable(
                                    enabled = abs(coverOffsetX) < 8f
                                ) {
                                    onToggleLyrics()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            ArtworkImage(
                                artworkUri = song.artworkUri,
                                contentDescription = song.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            if (!highlight.isNullOrBlank()) {
                                HighlightFrame(
                                    text = highlight.removePrefix("✨").trim()
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.10f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = song.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = song.artist.ifBlank { "Unknown Artist" },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    AiButton(
                        isLoading = aiLoading,
                        enabled = aiEnabled,
                        onClick = {
                            if (aiEnabled) {
                                aiLoading = true
                                onRefreshLyrics()
                            }
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = if (duration > 0) {
                            progress.toFloat().coerceAtMost(duration.toFloat())
                        } else {
                            0f
                        },
                        onValueChange = onSeek,
                        valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        thumb = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 5.dp
                            ) {
                                Box(
                                    modifier = Modifier.size(thumbSize.dp)
                                )
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(progress),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = formatDuration(duration),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onToggleRepeatMode) {
                        Icon(
                            imageVector = when {
                                shuffleEnabled -> Icons.Default.Shuffle
                                repeatMode == Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = null,
                            tint = when {
                                shuffleEnabled -> MaterialTheme.colorScheme.primary
                                repeatMode == Player.REPEAT_MODE_ONE -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPrevious) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = null,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.size(10.dp))

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 10.dp,
                            modifier = Modifier.size(78.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onPlayPause() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) {
                                        Icons.Default.Pause
                                    } else {
                                        Icons.Default.PlayArrow
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(10.dp))

                        IconButton(onClick = onNext) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    IconButton(onClick = onShowQueue) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightFrame(
    text: String
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            )

            Text(
                text = "⌜",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                modifier = Modifier.align(Alignment.TopStart)
            )

            Text(
                text = "⌟",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun AiButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "ai")

    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_scale"
    )

    val breathAlpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_alpha"
    )

    val textColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }

    Box(
        modifier = Modifier
            .size(34.dp)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AI",
            fontSize = 12.sp,
            color = textColor,
            modifier = if (isLoading && enabled) {
                Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = breathAlpha
                }
            } else {
                Modifier
            }
        )
    }
}

private fun isChineseLine(text: String): Boolean {
    return text.any { it in '\u4e00'..'\u9fff' }
}

@Composable
private fun FocusLyricsView(
    lines: List<String>
) {
    val listState = rememberLazyListState()

    LaunchedEffect(lines) {
        listState.scrollToItem(0)
    }

    val focusedIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo

            if (visibleItems.isEmpty()) {
                0
            } else {
                val viewportCenter =
                    (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

                visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    abs(itemCenter - viewportCenter)
                }?.index ?: 0
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 52.dp)
        ) {
            itemsIndexed(lines) { index, line ->
                val distance = abs(index - focusedIndex)

                val isMetaLine =
                    line == "Lyrics formatted by AI" ||
                            line == "Lyrics generated by AI" ||
                            line == "AI failed"

                val isChinese = isChineseLine(line)

                val alpha = when {
                    isMetaLine -> 0.72f
                    distance == 0 -> 1f
                    distance == 1 -> 0.74f
                    distance == 2 -> 0.50f
                    else -> 0.28f
                }

                val fontSize = when {
                    isMetaLine -> 13.sp
                    !isChinese && distance == 0 -> 18.sp
                    !isChinese -> 17.sp
                    distance == 0 -> 17.sp
                    else -> 15.sp
                }

                val lineHeight = when {
                    isMetaLine -> 20.sp
                    !isChinese -> 28.sp
                    else -> 24.sp
                }

                val fontWeight = when {
                    isMetaLine -> FontWeight.Normal
                    distance == 0 -> FontWeight.SemiBold
                    else -> FontWeight.Normal
                }

                val textColor = when {
                    isMetaLine -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                    isChinese -> MaterialTheme.colorScheme.onBackground.copy(alpha = alpha * 0.82f)
                    else -> MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = line,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        textAlign = TextAlign.Center,
                        color = textColor,
                        lineHeight = lineHeight,
                        maxLines = 3,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
    }
}

@Composable
fun ArtworkImage(
    artworkUri: Uri?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = artworkUri) {
        value = withContext(Dispatchers.IO) {
            try {
                artworkUri?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    val bitmap = bitmapState.value

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlayingIndicator(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "playing_indicator")

    val h1 by transition.animateFloat(
        initialValue = 6f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(260),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val h2 by transition.animateFloat(
        initialValue = 10f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(360),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val h3 by transition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier.size(height = 24.dp, width = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val barColor = MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .size(width = 3.dp, height = h1.dp)
                .clip(RoundedCornerShape(50))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = h2.dp)
                .clip(RoundedCornerShape(50))
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = h3.dp)
                .clip(RoundedCornerShape(50))
                .background(barColor)
        )
    }
}