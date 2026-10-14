package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OpenWebUiAccent
import com.example.ui.theme.OpenWebUiBackground
import com.example.ui.theme.OpenWebUiBorder
import com.example.ui.theme.OpenWebUiError
import com.example.ui.theme.OpenWebUiSurface
import com.example.ui.theme.OpenWebUiTextPrimary
import com.example.ui.theme.OpenWebUiTextSecondary

@Composable
fun OpenWebUiTopBar(
    isTermuxInstalled: Boolean,
    activeRunningCount: Int,
    onTelemetryClick: () -> Unit,
    onSetupGuideClick: () -> Unit,
    onClearLogsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OpenWebUiBackground)
            .border(width = 0.5.dp, color = OpenWebUiBorder)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Model Selector Lookalike (OpenWebUI standard header)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(OpenWebUiSurface)
                    .border(width = 1.dp, color = OpenWebUiBorder, shape = RoundedCornerShape(12.dp))
                    .clickable { onTelemetryClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("model_selector_badge"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (!isTermuxInstalled) OpenWebUiError
                            else if (activeRunningCount > 0) OpenWebUiAccent
                            else OpenWebUiAccent
                        )
                        .then(
                            if (activeRunningCount > 0) Modifier.alpha(pulseAlpha) else Modifier
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Termux Shell",
                    color = OpenWebUiTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• bash",
                    color = OpenWebUiTextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Action icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTelemetryClick,
                    modifier = Modifier.testTag("telemetry_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Device Telemetry",
                        tint = OpenWebUiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onSetupGuideClick,
                    modifier = Modifier.testTag("setup_guide_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Setup Guide",
                        tint = OpenWebUiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onClearLogsClick,
                    modifier = Modifier.testTag("clear_logs_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear Logs",
                        tint = OpenWebUiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
