package com.mandor.ui.employee.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun AnimatedTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgAlpha by animateFloatAsState(
        targetValue = when {
            isSelected -> 1f
            isHovered -> 0.12f
            else -> 0f
        },
        animationSpec = tween(200),
        label = "tabBgAlpha"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            isHovered -> EmployeeTokens.AccentIndigo.copy(alpha = 0.9f)
            else -> EmployeeTokens.TextSecondary
        },
        animationSpec = tween(200),
        label = "tabTextColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else if (isHovered) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tabScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected)
                    Brush.horizontalGradient(listOf(EmployeeTokens.AccentIndigo, EmployeeTokens.AccentIndigo.copy(red = 0.5f)))
                else
                    Brush.horizontalGradient(listOf(EmployeeTokens.AccentIndigo.copy(alpha = bgAlpha), EmployeeTokens.AccentIndigo.copy(alpha = bgAlpha * 0.6f)))
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
