package com.mandor.ui.employee.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.domain.model.Client
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun ClientSuggestionRow(
    client: Client,
    index: Int = 0,
    total: Int = 1,
    onSelect: () -> Unit
) {
    val itemInteraction = remember { MutableInteractionSource() }
    val itemHovered by itemInteraction.collectIsHoveredAsState()
    val itemScale by animateFloatAsState(
        if (itemHovered) 1.01f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "suggestionScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(itemScale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (itemHovered) {
                    Brush.horizontalGradient(
                        listOf(
                            EmployeeTokens.AccentIndigo.copy(alpha = 0.15f),
                            EmployeeTokens.BgSurface
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(EmployeeTokens.BgSurface, EmployeeTokens.BgSurface)
                    )
                }
            )
            .clickable(interactionSource = itemInteraction, indication = null) { onSelect() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (client.phone.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(client.phone, fontSize = 11.sp, color = EmployeeTokens.TextSecondary, fontWeight = FontWeight.Medium)
                    Text("📞", fontSize = 10.sp)
                }
            }
            if (client.storeName.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(client.storeName, fontSize = 10.sp, color = EmployeeTokens.TextMuted)
                    Text("🏪", fontSize = 10.sp)
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(client.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmployeeTokens.TextPrimary)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmployeeTokens.AccentIndigo.copy(alpha = 0.15f)
            ) {
                Text(
                    client.id,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmployeeTokens.AccentIndigo,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }

    if (index < total - 1) {
        Divider(
            color = EmployeeTokens.BorderColor.copy(alpha = 0.2f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
