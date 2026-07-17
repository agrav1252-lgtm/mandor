package com.mandor.ui.employee.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EmployeeTokens.BgBase.copy(alpha = 0.4f))
            .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "emptyPulse")
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "emptyAlpha"
            )
            Icon(
                Icons.Default.ShoppingCart,
                null,
                tint = EmployeeTokens.TextMuted.copy(alpha = alpha),
                modifier = Modifier.size(48.dp)
            )
            Text(
                "السلة فارغة",
                color = EmployeeTokens.TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "امسح الباركود أو أدخل كود الفاتورة لاسترجاعها",
                color = EmployeeTokens.TextMuted.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}
