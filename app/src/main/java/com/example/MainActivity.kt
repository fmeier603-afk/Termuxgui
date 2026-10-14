package com.example

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.PresetCommand
import com.example.ui.components.CommandLogItem
import com.example.ui.components.HardwareTelemetryCard
import com.example.ui.components.OpenWebUiPromptInput
import com.example.ui.components.OpenWebUiTopBar
import com.example.ui.components.SetupGuideModal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OpenWebUiAccent
import com.example.ui.theme.OpenWebUiBackground
import com.example.ui.theme.OpenWebUiBorder
import com.example.ui.theme.OpenWebUiSurface
import com.example.ui.theme.OpenWebUiSurfaceVariant
import com.example.ui.theme.OpenWebUiTextPrimary
import com.example.ui.theme.OpenWebUiTextSecondary
import com.example.ui.theme.OpenWebUiWarning
import com.example.viewmodel.TermuxViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TermuxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: TermuxViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Permission launcher for com.termux.permission.RUN_COMMAND
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.initSystemContext(context)
    }

    LaunchedEffect(Unit) {
        viewModel.initSystemContext(context)
        val termuxPerm = "com.termux.permission.RUN_COMMAND"
        if (ContextCompat.checkSelfPermission(context, termuxPerm) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(termuxPerm)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(OpenWebUiBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        topBar = {
            OpenWebUiTopBar(
                isTermuxInstalled = uiState.isTermuxInstalled,
                activeRunningCount = uiState.activeRunningCount,
                onTelemetryClick = { viewModel.setTelemetryVisible(true) },
                onSetupGuideClick = { viewModel.setSetupDialogVisible(true) },
                onClearLogsClick = { viewModel.clearLogs() }
            )
        },
        bottomBar = {
            OpenWebUiPromptInput(
                commandInput = uiState.commandInput,
                onCommandInputChange = { viewModel.onCommandInputChange(it) },
                onExecuteClick = { viewModel.executeCurrentCommand(context) },
                runInBackground = uiState.runInBackground,
                onToggleRunInBackground = { viewModel.toggleRunInBackground() },
                workingDir = uiState.workingDir,
                presets = uiState.presets,
                onPresetClick = { viewModel.executePreset(context, it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OpenWebUiBackground)
        ) {
            if (uiState.logs.isEmpty()) {
                // OpenWebUI-style Empty / Welcome Dashboard
                WelcomeConsoleView(
                    isTermuxInstalled = uiState.isTermuxInstalled,
                    onOpenSetupGuide = { viewModel.setSetupDialogVisible(true) },
                    onPresetClick = { viewModel.executePreset(context, it) },
                    presets = uiState.presets,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("console_logs_list"),
                    reverseLayout = true
                ) {
                    items(uiState.logs, key = { it.id }) { log ->
                        CommandLogItem(log = log)
                    }
                }
            }

            // Dialogs
            if (uiState.isSetupDialogVisible) {
                SetupGuideModal(
                    onDismissRequest = { viewModel.setSetupDialogVisible(false) }
                )
            }

            if (uiState.isTelemetryVisible) {
                HardwareTelemetryCard(
                    telemetry = uiState.deviceTelemetry,
                    onDismissRequest = { viewModel.setTelemetryVisible(false) }
                )
            }
        }
    }
}

@Composable
private fun WelcomeConsoleView(
    isTermuxInstalled: Boolean,
    onOpenSetupGuide: () -> Unit,
    onPresetClick: (PresetCommand) -> Unit,
    presets: List<PresetCommand>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo / Icon Badge
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(OpenWebUiSurfaceVariant)
                .border(width = 1.dp, color = OpenWebUiBorder, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = OpenWebUiAccent,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Termux Command Hub",
            color = OpenWebUiTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Send commands, monitor processes, and inspect hardware directly through Termux with an OpenWebUI interface.",
            color = OpenWebUiTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (!isTermuxInstalled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(OpenWebUiWarning.copy(alpha = 0.12f))
                    .border(width = 1.dp, color = OpenWebUiWarning.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
                    .clickable { onOpenSetupGuide() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = OpenWebUiWarning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Termux not detected or setup needed",
                        color = OpenWebUiTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to view instructions on enabling external apps in Termux.",
                        color = OpenWebUiTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Quick Preset Grid
        Text(
            text = "Quick Starters",
            color = OpenWebUiTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.take(4).forEach { preset ->
                StarterCard(
                    preset = preset,
                    onClick = { onPresetClick(preset) }
                )
            }
        }
    }
}

@Composable
private fun StarterCard(
    preset: PresetCommand,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (preset.id) {
        "sys_info" -> Icons.Default.Memory
        "mem_disk" -> Icons.Default.Storage
        "procs" -> Icons.Default.Code
        else -> Icons.Default.Terminal
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OpenWebUiSurface)
            .border(width = 1.dp, color = OpenWebUiBorder, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("starter_card_${preset.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(OpenWebUiSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OpenWebUiAccent,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = preset.title,
                color = OpenWebUiTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = preset.description,
                color = OpenWebUiTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
