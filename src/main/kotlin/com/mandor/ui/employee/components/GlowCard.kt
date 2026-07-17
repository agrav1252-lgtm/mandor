package com.mandor.ui.employee.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    glowColor: Color = EmployeeTokens.AccentIndigo,
    glowAlpha: Float = 0.0f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(EmployeeTokens.BgElevated)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = glowAlpha.coerceIn(0f, 0.6f) + 0.05f),
                        EmployeeTokens.BorderColor,
                        EmployeeTokens.BorderColor,
                        glowColor.copy(alpha = glowAlpha.coerceIn(0f, 0.6f) + 0.05f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        content = content
    )
}
