package com.example.viewmodel

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.TermuxResultReceiver
import com.example.model.CommandLog
import com.example.model.DeviceTelemetry
import com.example.model.PresetCommand
import com.example.service.TermuxManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class TermuxUiState(
    val commandInput: String = "",
    val workingDir: String = TermuxManager.DEFAULT_WORK_DIR,
    val runInBackground: Boolean = true,
    val logs: List<CommandLog> = emptyList(),
    val isTermuxInstalled: Boolean = true,
    val isSetupDialogVisible: Boolean = false,
    val isTelemetryVisible: Boolean = false,
    val deviceTelemetry: DeviceTelemetry? = null,
    val presets: List<PresetCommand> = emptyList(),
    val activeRunningCount: Int = 0
)

class TermuxViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TermuxUiState())
    val uiState: StateFlow<TermuxUiState> = _uiState.asStateFlow()

    init {
        initializePresets()
        listenForExecutionResults()
    }

    fun initSystemContext(context: Context) {
        val installed = TermuxManager.isTermuxInstalled(context)
        val telemetry = gatherDeviceTelemetry(context)
        _uiState.update {
            it.copy(
                isTermuxInstalled = installed,
                deviceTelemetry = telemetry
            )
        }
    }

    private fun listenForExecutionResults() {
        viewModelScope.launch {
            TermuxResultReceiver.resultsFlow.collect { result ->
                _uiState.update { state ->
                    val updatedLogs = state.logs.map { log ->
                        if (log.id == result.commandId) {
                            log.copy(
                                isExecuting = false,
                                exitCode = result.exitCode,
                                stdout = result.stdout,
                                stderr = result.stderr,
                                errorMessage = result.errorMessage
                            )
                        } else {
                            log
                        }
                    }
                    val running = updatedLogs.count { it.isExecuting }
                    state.copy(logs = updatedLogs, activeRunningCount = running)
                }
            }
        }
    }

    private fun initializePresets() {
        val presetsList = listOf(
            PresetCommand(
                id = "sys_info",
                title = "Hardware & Kernel",
                description = "Uname and CPU architecture details",
                command = "uname -a && cat /proc/cpuinfo | grep 'model name\\|Hardware\\|processor' | head -n 12"
            ),
            PresetCommand(
                id = "mem_disk",
                title = "Memory & Storage",
                description = "RAM usage and available filesystem storage",
                command = "df -h /data /storage/emulated/0 2>/dev/null; free -h 2>/dev/null || cat /proc/meminfo | head -n 10"
            ),
            PresetCommand(
                id = "procs",
                title = "Running Processes",
                description = "Inspect active process list",
                command = "ps -ef | head -n 25"
            ),
            PresetCommand(
                id = "net",
                title = "Network Interfaces",
                description = "Show IP addresses and interfaces",
                command = "ip addr show"
            ),
            PresetCommand(
                id = "pkg_list",
                title = "Installed Packages",
                description = "List top installed packages in Termux",
                command = "pkg list-installed 2>/dev/null | head -n 20"
            )
        )
        _uiState.update { it.copy(presets = presetsList) }
    }

    fun onCommandInputChange(input: String) {
        _uiState.update { it.copy(commandInput = input) }
    }

    fun onWorkingDirChange(dir: String) {
        _uiState.update { it.copy(workingDir = dir) }
    }

    fun toggleRunInBackground() {
        _uiState.update { it.copy(runInBackground = !it.runInBackground) }
    }

    fun setSetupDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isSetupDialogVisible = visible) }
    }

    fun setTelemetryVisible(visible: Boolean) {
        _uiState.update { it.copy(isTelemetryVisible = visible) }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList(), activeRunningCount = 0) }
    }

    fun executeCurrentCommand(context: Context) {
        val rawCommand = _uiState.value.commandInput.trim()
        if (rawCommand.isBlank()) return

        executeCommand(
            context = context,
            command = rawCommand,
            workingDir = _uiState.value.workingDir,
            runInBackground = _uiState.value.runInBackground
        )
        _uiState.update { it.copy(commandInput = "") }
    }

    fun executePreset(context: Context, preset: PresetCommand) {
        executeCommand(
            context = context,
            command = preset.command,
            workingDir = _uiState.value.workingDir,
            runInBackground = preset.runInBackground
        )
    }

    private fun executeCommand(
        context: Context,
        command: String,
        workingDir: String,
        runInBackground: Boolean
    ) {
        val commandId = UUID.randomUUID().toString()
        val args = arrayOf("-c", command)

        val newLog = CommandLog(
            id = commandId,
            command = command,
            arguments = args.toList(),
            workingDir = workingDir,
            runInBackground = runInBackground,
            timestamp = System.currentTimeMillis(),
            isExecuting = true
        )

        _uiState.update {
            it.copy(
                logs = listOf(newLog) + it.logs,
                activeRunningCount = it.activeRunningCount + 1
            )
        }

        val result = TermuxManager.dispatchCommand(
            context = context,
            commandId = commandId,
            scriptOrExecutable = TermuxManager.DEFAULT_BASH_PATH,
            arguments = args,
            workingDir = workingDir,
            runInBackground = runInBackground
        )

        if (result.isFailure) {
            val err = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
            _uiState.update { state ->
                val updatedLogs = state.logs.map { log ->
                    if (log.id == commandId) {
                        log.copy(
                            isExecuting = false,
                            exitCode = -1,
                            errorMessage = err
                        )
                    } else log
                }
                state.copy(
                    logs = updatedLogs,
                    activeRunningCount = updatedLogs.count { it.isExecuting }
                )
            }
        }
    }

    private fun gatherDeviceTelemetry(context: Context): DeviceTelemetry {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val totalMemMb = memoryInfo.totalMem / (1024 * 1024)
        val availMemMb = memoryInfo.availMem / (1024 * 1024)
        val abis = Build.SUPPORTED_ABIS?.toList() ?: listOf("unknown")
        val primaryAbi = abis.firstOrNull() ?: "arm64-v8a"
        val cores = Runtime.getRuntime().availableProcessors()

        return DeviceTelemetry(
            architecture = primaryAbi,
            supportedAbis = abis,
            cpuCores = cores,
            availableMemoryMb = availMemMb,
            totalMemoryMb = totalMemMb,
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
    }
}
