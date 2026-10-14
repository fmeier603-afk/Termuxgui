package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DeviceTelemetry
import com.example.ui.theme.OpenWebUiAccent
import com.example.ui.theme.OpenWebUiAccentCyan
import com.example.ui.theme.OpenWebUiBorder
import com.example.ui.theme.OpenWebUiCodeBackground
import com.example.ui.theme.OpenWebUiSurface
import com.example.ui.theme.OpenWebUiTextPrimary
import com.example.ui.theme.OpenWebUiTextSecondary

@Composable
fun HardwareTelemetryCard(
    telemetry: DeviceTelemetry?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(OpenWebUiSurface)
                .border(width = 1.dp, color = OpenWebUiBorder, shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
                .testTag("hardware_telemetry_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = OpenWebUiAccentCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Hardware Telemetry",
                        color = OpenWebUiTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (telemetry != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(OpenWebUiCodeBackground)
                            .border(width = 0.5.dp, color = OpenWebUiBorder, shape = RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TelemetryRow("Device", telemetry.deviceModel)
                        TelemetryRow("OS", telemetry.androidVersion)
                        TelemetryRow("Primary ABI", telemetry.architecture)
                        TelemetryRow("Supported ABIs", telemetry.supportedAbis.joinToString(", "))
                        TelemetryRow("CPU Cores", "${telemetry.cpuCores} Cores detected")
                        TelemetryRow(
                            "Memory",
                            "${telemetry.availableMemoryMb} MB free / ${telemetry.totalMemoryMb} MB total"
                        )
                    }
                } else {
                    Text(
                        text = "Telemetry loading...",
                        color = OpenWebUiTextSecondary,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OpenWebUiAccent,
                            contentColor = OpenWebUiSurface
                        ),
                        modifier = Modifier.testTag("telemetry_close_button")
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = OpenWebUiTextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = OpenWebUiTextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
