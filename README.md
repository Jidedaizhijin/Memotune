# Memotune / 记得听歌App

Memotune 是一款简洁、轻量的 Android 本地音乐播放器，主打干净克制的听歌体验，在保留基础播放能力的同时，加入歌词、桌面歌词、AI 悬浮球等实用功能。

### 已实现功能

- 本地音乐自动扫描，并自动过滤铃声、录音等非歌曲音频
- 支持多种常见音频格式播放，包括 MP3、MP4、FLAC 等
- 支持内嵌歌词与外挂歌词识别
- 支持歌词精准滚动显示
- 支持桌面悬浮歌词
- 支持 Android 系统媒体通知栏控制
- 支持车载蓝牙播放控制
- 支持 AI 悬浮球快捷控制，可贴边隐藏
- 支持自定义 AI API 来源
- 支持最近播放记录
- 支持歌单分类管理

### 兼容性

- 项目以 Android 16（API 36） 为编译与目标SDK版本，最低支持 Android 8.0（API 26）。

### 说明

后续将继续优化播放体验、歌词体验、在线音源能力与 AI 相关功能。

Memotune is a lightweight local music player for Android, built with Kotlin and Jetpack Compose.

It focuses on a clean, simple, and comfortable local music listening experience.  
Instead of becoming a heavy music platform, Memotune aims to make local playback, lyrics, floating controls, and daily music listening feel smooth and distraction-free.

## Preview


---

## Features

### Local Music Playback

- Local audio file playback
- Support for common audio formats such as MP3, FLAC, MP4 and more
- Background playback
- Media notification controls
- Playback queue support
- Mini player and full-screen player interaction
- Landscape playback mode

### Lyrics

- Embedded lyrics reading
- External lyrics support
- Lyric parsing and display
- Auto-scrolling lyrics
- Desktop floating lyrics
- Lyrics optimized for daily listening

### Floating Control

- Floating playback control
- Quick play / pause operation
- Lightweight control interaction without opening the full player
- Designed for quick music control during daily use

### User Interface

- Built with Jetpack Compose
- Clean Material-style interface
- Lightweight and restrained visual design
- Minimal local music library experience
- Simple song list, player page, and settings page

---

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Media3 / ExoPlayer
- MediaSession
- Android Foreground Service
- Local data storage
- MVVM-style architecture

---

## Project Structure

```text
app/
 └── src/main/java/com/jidedaizhijin/myapplication/
     ├── data/
     │   ├── lyrics/
     │   └── online/
     ├── ui/
     │   ├── home/
     │   ├── player/
     │   └── settings/
     ├── MainActivity.kt
     ├── PlaybackService.kt
     └── ...


---

Roadmap

Local Playback Experience

Improve playback state restoration

Optimize playback queue management

Polish full-screen player animations

Improve folder and local library management

Improve compatibility with different local audio files


Lyrics

Improve embedded lyrics compatibility

Improve external lyrics matching

Optimize lyric scrolling performance

Add better lyric fallback handling


Online Audio Experiments

Add experimental online music source support

Add online search and preview

Improve song metadata completion

Improve lyric matching from online sources


AI-related Experiments

Explore AI-assisted lyric and metadata completion

Explore AI-assisted audio enhancement

Improve floating control interactions


Distribution

Add GitHub Release version checking

Add in-app update notification

Prepare release builds

Improve app icon and branding assets



---

Current Status

Memotune is currently under active development.

The current focus is:

stabilizing local music playback

improving lyric support

polishing the player UI

keeping the app lightweight and comfortable to use


Online music source support and AI-related features are experimental and will be developed gradually.


---

Build

Clone the repository and open it with Android Studio.

Recommended environment:

Android Studio

Kotlin

Jetpack Compose

Android Gradle Plugin

Gradle


Build debug version:

./gradlew assembleDebug

On Windows:

gradlew.bat assembleDebug


---

Why Memotune?

Many music apps today are becoming increasingly complex.
For users who mainly listen to local music files, the experience can sometimes feel too heavy.

Memotune tries to keep things simple:

open the app

play local songs

view lyrics

control playback easily

enjoy music without unnecessary distractions


The goal is not to replace every music app, but to provide a clean, lightweight, and comfortable local music player for Android.


---

Notes

This project is mainly a personal Android development project and is still evolving.

Some features, especially online audio source support and AI-related experiments, are not yet finalized and may change in future versions.
