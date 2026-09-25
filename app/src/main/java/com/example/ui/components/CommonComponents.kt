package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.DeviceStatus
import com.example.data.model.DeviceType
import com.example.data.model.ThreatLevel
import com.example.ui.theme.*

@Composable
fun StatusBadge(status: DeviceStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status) {
        DeviceStatus.ONLINE -> Triple(StatusOnlineGreen.copy(alpha = 0.15f), StatusOnlineGreen, Icons.Default.CheckCircle)
        DeviceStatus.OFFLINE -> Triple(StatusOfflineRed.copy(alpha = 0.15f), StatusOfflineRed, Icons.Default.Cancel)
        DeviceStatus.WARNING -> Triple(StatusWarningAmber.copy(alpha = 0.15f), StatusWarningAmber, Icons.Default.Warning)
        DeviceStatus.UNSTABLE -> Triple(CyberBlueLight.copy(alpha = 0.15f), CyberBlueLight, Icons.Default.Speed)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = status.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ThreatBadge(threat: ThreatLevel, modifier: Modifier = Modifier) {
    val color = when (threat) {
        ThreatLevel.LOW -> CyberCyan
        ThreatLevel.MEDIUM -> StatusWarningAmber
        ThreatLevel.HIGH, ThreatLevel.CRITICAL -> StatusRogueCrimson
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = threat.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PulsingRadarDot(
    color: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .size(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
fun MetricGaugeCard(
    title: String,
    value: Float,
    unit: String = "%",
    icon: ImageVector,
    color: Color = CyberCyan,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyberNavyBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${value.toInt()}$unit",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (value / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    value > 85f -> StatusOfflineRed
                    value > 70f -> StatusWarningAmber
                    else -> color
                },
                trackColor = CyberNavyDark
            )
        }
    }
}

@Composable
fun DeviceTypeIcon(type: DeviceType, tint: Color = CyberCyan, modifier: Modifier = Modifier) {
    val icon = when (type) {
        DeviceType.ROUTER -> Icons.Default.Router
        DeviceType.SWITCH -> Icons.Default.DeviceHub
        DeviceType.SERVER -> Icons.Default.Dns
        DeviceType.WORKSTATION -> Icons.Default.Computer
        DeviceType.CAMERA -> Icons.Default.Videocam
        DeviceType.PRINTER -> Icons.Default.Print
        DeviceType.ROGUE -> Icons.Default.Warning
    }
    Icon(
        imageVector = icon,
        contentDescription = type.displayName,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun TerminalConsoleBox(
    logs: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalBackground)
            .border(1.dp, CyberNavyBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = logs,
            color = TerminalGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}
