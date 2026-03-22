package com.jidedaizhijin.myapplication.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jidedaizhijin.myapplication.pulseAccentBg
import com.jidedaizhijin.myapplication.pulseAccentText
import com.jidedaizhijin.myapplication.pulseActionIconTint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicTopBar(
    showSearch: Boolean,
    searchText: String,
    currentTab: String,
    sortMode: String,
    onSearchToggle: () -> Unit,
    onSearchTextChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onTabChange: (String) -> Unit,
    onSortClick: () -> Unit
) {
    Column {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            title = {},
            navigationIcon = {
                Text(
                    text = "PULSE",
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            actions = {
                TopActionButton(
                    onClick = onSearchToggle,
                    icon = if (showSearch) Icons.Default.Close else Icons.Default.Search
                )

                Spacer(modifier = Modifier.size(8.dp))

                TopActionButton(
                    onClick = onSettingsClick,
                    icon = Icons.Default.Settings
                )

                Spacer(modifier = Modifier.size(12.dp))
            }
        )

        AnimatedContent(
            targetState = showSearch,
            transitionSpec = {
                (fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.98f))
                    .togetherWith(
                        fadeOut(animationSpec = tween(120)) + scaleOut(targetScale = 0.98f)
                    )
            },
            label = "search_bar"
        ) { visible ->
            if (visible) {
                TextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Search",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = pulseActionIconTint()
                        )
                    },
                    shape = RoundedCornerShape(22.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = pulseAccentBg(),
                        unfocusedContainerColor = pulseAccentBg(),
                        disabledContainerColor = pulseAccentBg(),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            } else {
                Spacer(modifier = Modifier.size(0.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CapsuleTabSwitcher(
                currentTab = currentTab,
                onTabChange = onTabChange
            )

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onSortClick),
                shape = RoundedCornerShape(999.dp),
                color = if (sortMode == "date") {
                    MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = if (isSystemInDarkTheme()) 0.35f else 0.45f
                    )
                } else {
                    pulseAccentBg()
                },
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = sortIconFor(sortMode),
                        contentDescription = null,
                        tint = if (sortMode == "date") {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            pulseAccentText()
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TopActionButton(
    onClick: () -> Unit,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = pulseAccentBg(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = pulseActionIconTint(),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CapsuleTabSwitcher(
    currentTab: String,
    onTabChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CapsuleTabItem(
            icon = Icons.Default.LibraryMusic,
            selected = currentTab == "ALL",
            onClick = { onTabChange("ALL") }
        )

        CapsuleTabItem(
            icon = Icons.Default.Favorite,
            selected = currentTab == "FAVORITES",
            onClick = { onTabChange("FAVORITES") }
        )

        CapsuleTabItem(
            icon = Icons.Default.Folder,
            selected = currentTab == "FOLDER",
            onClick = { onTabChange("FOLDER") }
        )
    }
}

@Composable
private fun CapsuleTabItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(999.dp),
        color = if (selected) pulseAccentBg() else Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) {
                    pulseAccentText()
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

private fun sortIconFor(mode: String): ImageVector {
    return when (mode) {
        "date" -> Icons.Default.Schedule
        "title" -> Icons.Default.SortByAlpha
        else -> Icons.Default.Person
    }
}
