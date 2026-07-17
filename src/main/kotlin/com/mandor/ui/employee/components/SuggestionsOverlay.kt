package com.mandor.ui.employee.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.domain.model.Client
import com.mandor.domain.model.Invoice
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun ClientCodeSuggestionsDropdown(
    expanded: Boolean,
    suggestions: List<Client>,
    onDismiss: () -> Unit,
    onSelect: (Client) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(220.dp)
            .background(EmployeeTokens.BgElevated)
            .border(1.dp, EmployeeTokens.AccentIndigo.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    ) {
        suggestions.forEach { client ->
            DropdownClientRow(client = client, onSelect = { onSelect(client) })
        }
    }
}

@Composable
fun ClientNameSuggestionsDropdown(
    expanded: Boolean,
    suggestions: List<Client>,
    onDismiss: () -> Unit,
    onSelect: (Client) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(320.dp)
            .background(EmployeeTokens.BgElevated)
            .border(1.dp, EmployeeTokens.AccentIndigo.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    ) {
        suggestions.forEachIndexed { index, client ->
            DropdownClientRow(client = client, onSelect = { onSelect(client) })
            if (index < suggestions.size - 1) {
                Divider(color = EmployeeTokens.BorderColor.copy(alpha = 0.15f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun InvoiceSuggestionsDropdown(
    expanded: Boolean,
    suggestions: List<Invoice>,
    onDismiss: () -> Unit,
    onSelect: (Invoice) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(280.dp)
            .background(EmployeeTokens.BgElevated)
            .border(1.dp, EmployeeTokens.AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    ) {
        suggestions.forEachIndexed { index, invoice ->
            DropdownInvoiceRow(invoice = invoice, onSelect = { onSelect(invoice) })
            if (index < suggestions.size - 1) {
                Divider(color = EmployeeTokens.BorderColor.copy(alpha = 0.15f), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun DropdownClientRow(
    client: Client,
    onSelect: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onSelect)
            .background(if (hovered) EmployeeTokens.AccentIndigo.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(client.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmployeeTokens.TextPrimary)
            Text(client.id, fontSize = 11.sp, color = EmployeeTokens.TextMuted)
        }
        Column(horizontalAlignment = Alignment.End) {
            if (client.storeName.isNotEmpty()) {
                Text(client.storeName, fontSize = 11.sp, color = EmployeeTokens.AccentIndigo)
            }
            if (client.phone.isNotEmpty()) {
                Text(client.phone, fontSize = 10.sp, color = EmployeeTokens.TextSecondary)
            }
        }
    }
}

@Composable
private fun DropdownInvoiceRow(
    invoice: Invoice,
    onSelect: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onSelect)
            .background(if (hovered) EmployeeTokens.AccentAmber.copy(alpha = 0.1f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(invoice.id, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmployeeTokens.AccentAmber)
            Text(invoice.clientName, fontSize = 11.sp, color = EmployeeTokens.TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("%.2f ج.م".format(invoice.totalAmount), fontSize = 12.sp, color = EmployeeTokens.AccentGreen, fontWeight = FontWeight.Bold)
            Text(invoice.date, fontSize = 10.sp, color = EmployeeTokens.TextMuted)
        }
    }
}
