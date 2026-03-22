package com.jidedaizhijin.myapplication.data.folder

data class Folder(
    val id: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
