package com.example.model

data class CommandLog(
    val id: String,
    val command: String,
    val arguments: List<String>,
    val workingDir: String,
    val runInBackground: Boolean,
    val timestamp: Long,
    val exitCode: Int? = null,
    val stdout: String? = null,
    val stderr: String? = null,
    val errorMessage: String? = null,
    val isExecuting: Boolean = false
)

data class PresetCommand(
    val id: String,
    val title: String,
    val description: String,
    val command: String,
    val args: List<String> = emptyList(),
    val iconName: String = "terminal",
    val runInBackground: Boolean = true
)

data class DeviceTelemetry(
    val architecture: String,
    val supportedAbis: List<String>,
    val cpuCores: Int,
    val availableMemoryMb: Long,
    val totalMemoryMb: Long,
    val deviceModel: String,
    val androidVersion: String
)
