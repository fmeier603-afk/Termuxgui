package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PresetCommand
import com.example.ui.theme.OpenWebUiAccent
import com.example.ui.theme.OpenWebUiBackground
import com.example.ui.theme.OpenWebUiBorder
import com.example.ui.theme.OpenWebUiSurface
import com.example.ui.theme.OpenWebUiSurfaceVariant
import com.example.ui.theme.OpenWebUiTextPrimary
import com.example.ui.theme.OpenWebUiTextSecondary

@Composable
fun OpenWebUiPromptInput(
    commandInput: String,
    onCommandInputChange: (String) -> Unit,
    onExecuteClick: () -> Unit,
    runInBackground: Boolean,
    onToggleRunInBackground: () -> Unit,
    workingDir: String,
    presets: List<PresetCommand>,
    onPresetClick: (PresetCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OpenWebUiBackground)
            .padding(bottom = 12.dp)
    ) {
        // --- 1. Quick Presets Horizontal Strip ---
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                PresetChip(
                    preset = preset,
                    onClick = { onPresetClick(preset) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 2. Main OpenWebUI Input Box ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(OpenWebUiSurface)
                .border(width = 1.dp, color = OpenWebUiBorder, shape = RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Command Text Input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    if (commandInput.isEmpty()) {
                        Text(
                            text = "Send command to Termux (e.g. uname -a, top -n 1)...",
                            color = OpenWebUiTextSecondary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    BasicTextField(
                        value = commandInput,
                        onValueChange = onCommandInputChange,
                        textStyle = TextStyle(
                            color = OpenWebUiTextPrimary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(OpenWebUiAccent),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { onExecuteClick() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("command_text_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Row within the prompt box: mode switch & send button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode Toggle (Background vs Termux window)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OpenWebUiSurfaceVariant)
                            .clickable { onToggleRunInBackground() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("mode_toggle_chip"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (runInBackground) OpenWebUiAccent else OpenWebUiTextSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (runInBackground) "Mode: Background" else "Mode: Window",
                            color = OpenWebUiTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Run / Send Button
                    val isEnabled = commandInput.isNotBlank()
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) OpenWebUiAccent else OpenWebUiSurfaceVariant)
                            .clickable(enabled = isEnabled) { onExecuteClick() }
                            .testTag("run_command_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Run Command",
                            tint = if (isEnabled) OpenWebUiBackground else OpenWebUiTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    preset: PresetCommand,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(OpenWebUiSurface)
            .border(width = 0.5.dp, color = OpenWebUiBorder, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("preset_${preset.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = null,
            tint = OpenWebUiAccent,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = preset.title,
            color = OpenWebUiTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
