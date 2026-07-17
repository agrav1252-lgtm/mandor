package com.mandor.ui.employee.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.runtime.*
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
fun InvoiceActionBar(
    isEditing: Boolean,
    hasCartItems: Boolean,
    formattedTotal: String,
    itemCount: Int,
    totalQuantity: Int,
    onSave: () -> Unit,
    onPrint: () -> Unit,
    onExportPdf: () -> Unit,
    onClearCart: () -> Unit,
    paymentType: String = "نقدي",
    formattedRemaining: String = "0.00",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(EmployeeTokens.BgBase)
            .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GradientActionButton(
                label = if (isEditing) "حفظ التعديلات" else "إصدار الفاتورة",
                icon = { Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                gradient = listOf(EmployeeTokens.AccentGreen, Color(0xFF059669)),
                onClick = onSave
            )

            if (hasCartItems) {
                GradientActionButton(
                    label = "طباعة",
                    icon = { Icon(Icons.Default.Print, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                    gradient = listOf(EmployeeTokens.AccentIndigo, Color(0xFF818CF8)),
                    horizontalPadding = 14.dp,
                    onClick = onPrint
                )
                GradientActionButton(
                    label = "PDF",
                    icon = { Icon(Icons.Default.PictureAsPdf, null, tint = Color.White, modifier = Modifier.size(16.dp)) },
                    gradient = listOf(Color(0xFFDC2626), Color(0xFFEF4444)),
                    horizontalPadding = 14.dp,
                    onClick = onExportPdf
                )
                ClearCartButton(onClick = onClearCart)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                AnimatedContent(
                    targetState = formattedTotal,
                    transitionSpec = {
                        (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                    },
                    label = "totalAnim"
            ) { totalStr ->
                Text(
                    "$totalStr ج.م",
                    color = EmployeeTokens.AccentGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
            if (paymentType == "دفع مجزء" && formattedRemaining.toDoubleOrNull() != null && formattedRemaining.toDouble() > 0) {
                Text(
                    "متبقي: $formattedRemaining ج.م",
                    color = EmployeeTokens.AccentAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
            Text("الإجمالي الكلي", color = EmployeeTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text("$itemCount صنف — $totalQuantity قطعة", color = EmployeeTokens.TextMuted, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun GradientActionButton(
    label: String,
    icon: @Composable () -> Unit,
    gradient: List<Color>,
    onClick: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        if (hovered) 1.04f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "actionBtnScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .height(32.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Brush.horizontalGradient(gradient))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            icon()
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = if (horizontalPadding == 20.dp) 11.sp else 10.sp)
        }
    }
}

@Composable
private fun ClearCartButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        if (hovered) 1.03f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "clearBtnScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .height(32.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (hovered) EmployeeTokens.AccentRed.copy(alpha = 0.2f) else EmployeeTokens.BorderColor)
            .border(
                1.dp,
                if (hovered) EmployeeTokens.AccentRed.copy(alpha = 0.5f) else Color.Transparent,
                RoundedCornerShape(7.dp)
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "مسح السلة",
            color = if (hovered) EmployeeTokens.AccentRed else EmployeeTokens.TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}
