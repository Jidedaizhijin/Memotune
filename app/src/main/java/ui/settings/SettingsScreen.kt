package com.jidedaizhijin.myapplication.ui.settings


import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.jidedaizhijin.myapplication.ApiSettingsStore
import com.jidedaizhijin.myapplication.Song
import com.jidedaizhijin.myapplication.pulseAccentBg

@Composable
fun FullScreenSettingsPage(
    songs: List<Song>,
    hiddenIds: Set<String>,
    onUnhideSong: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var page by rememberSaveable { mutableStateOf("home") }

    var provider by rememberSaveable {
        mutableStateOf(ApiSettingsStore.getProvider(context))
    }
    var baseUrl by rememberSaveable {
        mutableStateOf(ApiSettingsStore.getBaseUrl(context))
    }
    var model by rememberSaveable {
        mutableStateOf(ApiSettingsStore.getModel(context))
    }
    var apiKey by rememberSaveable {
        mutableStateOf(ApiSettingsStore.getApiKey(context))
    }
    var apiKeyVisible by rememberSaveable { mutableStateOf(false) }
    var modelMenuExpanded by rememberSaveable { mutableStateOf(false) }

    val hiddenSongs = remember(songs, hiddenIds) {
        songs.filter { hiddenIds.contains(it.id.toString()) }
    }

    val modelOptions = when (provider) {
        ApiSettingsStore.PROVIDER_DEEPSEEK -> listOf(
            "deepseek-chat",
            "deepseek-reasoner"
        )
        ApiSettingsStore.PROVIDER_OPENAI -> listOf(
            "gpt-4o-mini",
            "gpt-4o"
        )
        ApiSettingsStore.PROVIDER_QIANWEN -> listOf(
            "qwen-turbo",
            "qwen-plus"
        )
        else -> emptyList()
    }

    BackHandler {
        if (page == "home") {
            onDismiss()
        } else {
            page = "home"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (page == "home") onDismiss() else page = "home"
                        }
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = when (page) {
                        "api" -> "AI Model"
                        "hidden" -> "Hidden Songs"
                        "info" -> "Information"
                        else -> "Settings"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (page) {
                "home" -> {
                    SettingsMenuItem(
                        title = "AI Model",
                        subtitle = "Provider and model settings",
                        onClick = { page = "api" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsMenuItem(
                        title = "Hidden Songs",
                        subtitle = "${hiddenSongs.size} hidden",
                        onClick = { page = "hidden" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsMenuItem(
                        title = "Information",
                        subtitle = "App name and developer",
                        onClick = { page = "info" }
                    )
                }

                "api" -> {
                    Text(
                        text = "Provider",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProviderChip(
                            text = "DeepSeek",
                            selected = provider == ApiSettingsStore.PROVIDER_DEEPSEEK,
                            onClick = {
                                provider = ApiSettingsStore.PROVIDER_DEEPSEEK
                                ApiSettingsStore.applyProviderDefaults(context, provider)
                                baseUrl = ApiSettingsStore.getBaseUrl(context)
                                model = ApiSettingsStore.getModel(context)
                            }
                        )

                        ProviderChip(
                            text = "OpenAI",
                            selected = provider == ApiSettingsStore.PROVIDER_OPENAI,
                            onClick = {
                                provider = ApiSettingsStore.PROVIDER_OPENAI
                                ApiSettingsStore.applyProviderDefaults(context, provider)
                                baseUrl = ApiSettingsStore.getBaseUrl(context)
                                model = ApiSettingsStore.getModel(context)
                            }
                        )

                        ProviderChip(
                            text = "Qianwen",
                            selected = provider == ApiSettingsStore.PROVIDER_QIANWEN,
                            onClick = {
                                provider = ApiSettingsStore.PROVIDER_QIANWEN
                                ApiSettingsStore.applyProviderDefaults(context, provider)
                                baseUrl = ApiSettingsStore.getBaseUrl(context)
                                model = ApiSettingsStore.getModel(context)
                            }
                        )

                        ProviderChip(
                            text = "Custom",
                            selected = provider == ApiSettingsStore.PROVIDER_CUSTOM,
                            onClick = {
                                provider = ApiSettingsStore.PROVIDER_CUSTOM
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (provider != ApiSettingsStore.PROVIDER_CUSTOM) {
                        Text(
                            text = "Model",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { modelMenuExpanded = true },
                                shape = RoundedCornerShape(18.dp),
                                color = pulseAccentBg(),
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = model,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Select model",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = modelMenuExpanded,
                                onDismissRequest = { modelMenuExpanded = false },
                                containerColor = pulseAccentBg()
                            ) {
                                modelOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            model = option
                                            modelMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = "Model",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsTextField(
                        value = model,
                        onValueChange = { model = it },
                        placeholder = if (provider == ApiSettingsStore.PROVIDER_CUSTOM) {
                            "custom-model"
                        } else {
                            "deepseek-chat"
                        }
                    )

                    if (provider == ApiSettingsStore.PROVIDER_CUSTOM) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Base URL",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsTextField(
                            value = baseUrl,
                            onValueChange = { baseUrl = it },
                            placeholder = "https://api.example.com/v1/chat/completions"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "API Key",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Enter API Key") },
                        shape = RoundedCornerShape(18.dp),
                        visualTransformation = if (apiKeyVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },

                        trailingIcon = {
                            IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                                Icon(
                                    imageVector = if (apiKeyVisible) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
                                    contentDescription = "Toggle API Key visibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = pulseAccentBg(),
                            unfocusedContainerColor = pulseAccentBg(),
                            disabledContainerColor = pulseAccentBg(),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                ApiSettingsStore.saveProvider(context, provider)
                                ApiSettingsStore.saveApiKey(context, apiKey.trim())
                                ApiSettingsStore.saveBaseUrl(context, baseUrl.trim())
                                ApiSettingsStore.saveModel(context, model.trim())
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }

                "hidden" -> {
                    if (hiddenSongs.isEmpty()) {
                        Text(
                            text = "No hidden songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(hiddenSongs, key = { it.id }) { song ->
                                HiddenSongItem(
                                    song = song,
                                    onUnhide = { onUnhideSong(song.id) }
                                )
                            }
                        }
                    }
                }

                "info" -> {
                    Text(
                        text = "Pulse",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "by Jidedaizhijin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HiddenSongItem(
    song: Song,
    onUnhide: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = pulseAccentBg(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Unhide",
                modifier = Modifier.clickable(onClick = onUnhide),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SettingsMenuItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = pulseAccentBg(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProviderChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            pulseAccentBg()
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder) },
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = pulseAccentBg(),
            unfocusedContainerColor = pulseAccentBg(),
            disabledContainerColor = pulseAccentBg(),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
