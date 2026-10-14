package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommandLog
import com.example.ui.theme.OpenWebUiAccent
import com.example.ui.theme.OpenWebUiBorder
import com.example.ui.theme.OpenWebUiCodeBackground
import com.example.ui.theme.OpenWebUiError
import com.example.ui.theme.OpenWebUiSuccess
import com.example.ui.theme.OpenWebUiSurface
import com.example.ui.theme.OpenWebUiSurfaceVariant
import com.example.ui.theme.OpenWebUiTextPrimary
import com.example.ui.theme.OpenWebUiTextSecondary
import com.example.ui.theme.OpenWebUiWarning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommandLogItem(
    log: CommandLog,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: stdout, 1: stderr

    val formattedTime = remember(log.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("command_log_item_${log.id}")
    ) {
        // --- 1. USER COMMAND MESSAGE (Chat Prompt) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                    .background(OpenWebUiSurfaceVariant)
                    .border(width = 1.dp, color = OpenWebUiBorder, shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(OpenWebUiAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (log.runInBackground) "background" else "termux session",
                            color = OpenWebUiTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = formattedTime,
                        color = OpenWebUiTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$ ${log.command}",
                    color = OpenWebUiTextPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                if (log.workingDir.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "cwd: ${log.workingDir}",
                        color = OpenWebUiTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. TERMINAL RESPONSE CARD (OpenWebUI Assistant Block) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OpenWebUiSurface)
                    .border(width = 1.dp, color = OpenWebUiBorder, shape = RoundedCornerShape(16.dp))
            ) {
                // Header Bar of the Output Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OpenWebUiSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Console Output",
                            tint = OpenWebUiTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Output",
                            color = OpenWebUiTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Status Badge
                        if (log.isExecuting) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(OpenWebUiWarning.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(10.dp),
                                    strokeWidth = 1.5.dp,
                                    color = OpenWebUiWarning
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Running...",
                                    color = OpenWebUiWarning,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (log.exitCode != null) {
                            val isOk = log.exitCode == 0
                            val badgeColor = if (isOk) OpenWebUiSuccess else OpenWebUiError
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isOk) "Exit: 0 (OK)" else "Exit: ${log.exitCode}",
                                    color = badgeColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Copy Output Button
                    IconButton(
                        onClick = {
                            val textToCopy = buildString {
                                if (!log.stdout.isNullOrBlank()) append(log.stdout)
                                if (!log.stderr.isNullOrBlank()) {
                                    if (isNotEmpty()) append("\n---\n")
                                    append(log.stderr)
                                }
                                if (!log.errorMessage.isNullOrBlank()) {
                                    if (isNotEmpty()) append("\n---\n")
                                    append(log.errorMessage)
                                }
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Termux Output", textToCopy))
                            Toast.makeText(context, "Output copied to clipboard", Toast.LENGTH_SHORT).show()
                            isCopied = true
                            coroutineScope.launch {
                                delay(2000)
                                isCopied = false
                            }
                        },
                        modifier = Modifier.size(28.dp).testTag("copy_log_button_${log.id}")
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy output",
                            tint = if (isCopied) OpenWebUiSuccess else OpenWebUiTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Stdout / Stderr Tabs (if both exist)
                val hasStderr = !log.stderr.isNullOrBlank()
                val hasStdout = !log.stdout.isNullOrBlank()

                if (hasStderr && hasStdout) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OpenWebUiCodeBackground)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TabChip(
                            title = "stdout",
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        TabChip(
                            title = "stderr",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            isError = true
                        )
                    }
                }

                // Terminal Code Output Box
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OpenWebUiCodeBackground)
                        .padding(14.dp)
                        .horizontalScroll(scrollState)
                ) {
                    val displayedContent = when {
                        log.isExecuting -> "Waiting for Termux execution result..."
                        selectedTab == 1 && hasStderr -> log.stderr.orEmpty()
                        !log.errorMessage.isNullOrBlank() -> "Error: ${log.errorMessage}"
                        !log.stdout.isNullOrBlank() -> log.stdout.orEmpty()
                        !log.stderr.isNullOrBlank() -> log.stderr.orEmpty()
                        else -> "(No standard output returned)"
                    }

                    Text(
                        text = displayedContent,
                        color = if (selectedTab == 1 || !log.errorMessage.isNullOrBlank()) OpenWebUiError else OpenWebUiTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TabChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isError: Boolean = false
) {
    val bg = if (isSelected) {
        if (isError) OpenWebUiError.copy(alpha = 0.2f) else OpenWebUiSurfaceVariant
    } else {
        OpenWebUiSurface
    }
    val textColor = if (isSelected) {
        if (isError) OpenWebUiError else OpenWebUiTextPrimary
    } else {
        OpenWebUiTextSecondary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(width = 0.5.dp, color = OpenWebUiBorder, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.testTag("tab_${title}")
        )
    }
}
