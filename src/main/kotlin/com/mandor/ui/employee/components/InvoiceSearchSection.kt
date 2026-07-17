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
import androidx.compose.material.icons.filled.Edit
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
import com.mandor.domain.model.Invoice
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun InvoiceSearchSection(
    invoiceSearchId: String,
    onInvoiceSearchIdChange: (String) -> Unit,
    onSearch: () -> Unit,
    onEditClick: () -> Unit,
    showSuggestions: Boolean = false,
    suggestions: List<Invoice> = emptyList(),
    onDismissSuggestions: () -> Unit = {},
    onSelectSuggestion: (Invoice) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val editInteraction = remember { MutableInteractionSource() }
        val editHovered by editInteraction.collectIsHoveredAsState()
        val editScale by animateFloatAsState(
            if (editHovered) 1.03f else 1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "editBtnScale"
        )

        Box(
            modifier = Modifier
                .scale(editScale)
                .height(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Brush.horizontalGradient(listOf(EmployeeTokens.AccentAmber, Color(0xFFF59E0B))))
                .clickable(interactionSource = editInteraction, indication = null, onClick = onEditClick)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, null, tint = EmployeeTokens.BgDeep, modifier = Modifier.size(12.dp))
                Text("تعديل", color = EmployeeTokens.BgDeep, fontWeight = FontWeight.Bold, fontSize = 9.sp)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            CompactInputField(
                value = invoiceSearchId,
                onValueChange = onInvoiceSearchIdChange,
                label = "كود الفاتورة - اضغط Enter",
                placeholder = "INV-1001",
                onEnterPressed = onSearch,
                modifier = Modifier.fillMaxWidth()
            )
            InvoiceSuggestionsDropdown(
                expanded = showSuggestions && suggestions.isNotEmpty(),
                suggestions = suggestions,
                onDismiss = onDismissSuggestions,
                onSelect = onSelectSuggestion
            )
        }
    }
}

@Composable
fun InvoiceSuggestionRow(
    invoice: Invoice,
    onSelect: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        if (hovered) 1.01f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "invSuggestionScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (hovered) {
                    Brush.horizontalGradient(
                        listOf(EmployeeTokens.AccentAmber.copy(alpha = 0.15f), EmployeeTokens.BgSurface)
                    )
                } else {
                    Brush.horizontalGradient(listOf(EmployeeTokens.BgSurface, EmployeeTokens.BgSurface))
                }
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.Start) {
            Text(
                "%.2f ج.م".format(invoice.totalAmount),
                fontSize = 12.sp,
                color = EmployeeTokens.AccentGreen,
                fontWeight = FontWeight.Bold
            )
            Text(invoice.date, fontSize = 10.sp, color = EmployeeTokens.TextMuted)
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.End) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmployeeTokens.AccentAmber.copy(alpha = 0.15f)
            ) {
                Text(
                    invoice.id,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmployeeTokens.AccentAmber,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(invoice.clientName, fontSize = 11.sp, color = EmployeeTokens.TextSecondary)
        }
    }
}
