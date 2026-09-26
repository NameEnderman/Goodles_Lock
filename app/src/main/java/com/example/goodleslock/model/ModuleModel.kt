package com.example.goodleslock.model

data class ModuleModel(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val oneUiVersion: String,
    val downloadUrl: String,
    val packageName: String,
    val iconUrl: String = ""
)
