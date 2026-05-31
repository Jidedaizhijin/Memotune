<!--suppress ALL -->
<h1 align="center">Memotune / 记得听歌App</h1>

<p align="center">
  <b>简洁轻量的 Android 本地音乐播放器，专注纯粹听歌体验</b>
</p>

<p align="center">
  <a href="https://github.com/jidedaizhijin/Memotune/releases">
    <img src="https://img.shields.io/github/v/release/jidedaizhijin/Memotune?style=flat&color=6750A4" alt="Version">
  </a>
  <a href="https://github.com/jidedaizhijin/Memotune/releases">
    <img src="https://img.shields.io/github/downloads/jidedaizhijin/Memotune/total?style=flat&color=orange" alt="Downloads">
  </a>
  <a href="https://github.com/jidedaizhijin/Memotune/commits">
    <img src="https://img.shields.io/github/last-commit/jidedaizhijin/Memotune?style=flat" alt="Last Commit">
  </a>
  <a href="https://github.com/jidedaizhijin/Memotune/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/jidedaizhijin/Memotune?style=flat" alt="License">
  </a>
</p>

<p align="center">
  <b>本地播放 · 多格式音频 · 内外挂歌词 · 桌面歌词 · AI悬浮球 · 歌单管理</b>
</p>

---

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
