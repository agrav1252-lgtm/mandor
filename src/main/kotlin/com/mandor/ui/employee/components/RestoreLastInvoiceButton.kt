package com.mandor.ui.employee.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun RestoreLastInvoiceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        if (hovered) 1.03f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "restoreScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (hovered) EmployeeTokens.AccentAmber.copy(alpha = 0.15f)
                else EmployeeTokens.BgBase
            )
            .border(
                1.dp,
                if (hovered) EmployeeTokens.AccentAmber.copy(alpha = 0.5f) else EmployeeTokens.BorderColor,
                RoundedCornerShape(10.dp)
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Restore,
                contentDescription = null,
                tint = EmployeeTokens.AccentAmber,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "آخر فاتورة",
                color = if (hovered) EmployeeTokens.AccentAmber else EmployeeTokens.TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
