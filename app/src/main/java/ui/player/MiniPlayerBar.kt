@file:OptIn(ExperimentalFoundationApi::class)

package com.jidedaizhijin.myapplication.ui.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jidedaizhijin.myapplication.Song
import com.jidedaizhijin.myapplication.pulseAccentBg

@Composable
fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    onBarClick: () -> Unit,
    onSwipeUp: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()

    val backgroundColor = if (dark) {
        Color(0xFF1D2633)
    } else {
        pulseAccentBg()
    }

    val titleColor = if (dark) {
        MaterialTheme.colorScheme.onBackground
    } else {
        Color(0xFF1A2230)
    }

    val subtitleColor = if (dark) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        Color(0xFF5F6D80)
    }

    val iconColor = if (dark) {
        MaterialTheme.colorScheme.onBackground
    } else {
        Color(0xFF1A2230)
    }

    var totalDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (totalDrag < -120f) {
                            onSwipeUp()
                        }
                        totalDrag = 0f
                    }
                )
            }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(34.dp))
                .clickable(onClick = onBarClick),
            shape = RoundedCornerShape(34.dp),
            color = backgroundColor,
            tonalElevation = 0.dp,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtworkImage(
                    artworkUri = song.artworkUri,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        color = titleColor,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = if (isPlaying) {
                            Modifier
                                .fillMaxWidth()
                                .basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    animationMode = MarqueeAnimationMode.Immediately,
                                    repeatDelayMillis = 1200
                                )
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )

                    Text(
                        text = song.artist.ifBlank { "Unknown Artist" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = subtitleColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousClick) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = null,
                            tint = iconColor
                        )
                    }

                    IconButton(onClick = onPlayPauseClick) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = iconColor
                        )
                    }

                    IconButton(onClick = onNextClick) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = null,
                            tint = iconColor
                        )
                    }
                }
            }
        }
    }
}