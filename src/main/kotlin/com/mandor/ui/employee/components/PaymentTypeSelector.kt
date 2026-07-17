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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
fun PaymentTypeSelector(
    paymentType: String,
    options: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCash = paymentType == "نقدي"
    val isPartial = paymentType == "دفع مجزء"
    val payColor = when {
        isPartial -> EmployeeTokens.AccentBlue
        isCash -> EmployeeTokens.AccentGreen
        else -> EmployeeTokens.AccentAmber
    }

    Box(modifier = modifier) {
        val dropInteraction = remember { MutableInteractionSource() }
        val dropHovered by dropInteraction.collectIsHoveredAsState()
        val dropScale by animateFloatAsState(
            if (dropHovered) 1.03f else 1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "dropScale"
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "نوع الدفع",
                color = EmployeeTokens.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 4.dp)
            )
            Row(
                modifier = Modifier
                    .width(140.dp)
                    .scale(dropScale)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(payColor.copy(alpha = 0.1f))
                    .border(1.dp, payColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable(interactionSource = dropInteraction, indication = null) { onToggle() }
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = payColor,
                    modifier = Modifier.size(18.dp)
                )
                AnimatedContent(
                    targetState = paymentType,
                    transitionSpec = {
                        (slideInVertically { -it } + fadeIn()) togetherWith
                            (slideOutVertically { it } + fadeOut())
                    },
                    label = "payAnim"
                ) { type ->
                    Text(type, color = payColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape).background(payColor)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .background(EmployeeTokens.BgElevated)
                .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(10.dp))
        ) {
            options.forEach { option ->
                val optColor = when (option) {
                    "نقدي" -> EmployeeTokens.AccentGreen
                    "دفع مجزء" -> EmployeeTokens.AccentBlue
                    else -> EmployeeTokens.AccentAmber
                }
                val isSelected = paymentType == option
                DropdownMenuItem(
                    onClick = { onSelect(option) },
                    modifier = Modifier.background(
                        if (isSelected) optColor.copy(alpha = 0.12f) else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = optColor, modifier = Modifier.size(14.dp))
                        } else {
                            Spacer(Modifier.size(14.dp))
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                option,
                                color = if (isSelected) optColor else EmployeeTokens.TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            Box(Modifier.size(8.dp).clip(CircleShape).background(optColor))
                        }
                    }
                }
            }
        }
    }
}
