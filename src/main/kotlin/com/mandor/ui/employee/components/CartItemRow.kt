package com.mandor.ui.employee.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.domain.model.InvoiceItem
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun CartItemRow(
    item: InvoiceItem,
    index: Int,
    onRemove: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var isEditing by remember { mutableStateOf(false) }
    var quantityText by remember(item.quantity) { mutableStateOf(item.quantity.toString()) }

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.01f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rowScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isHovered) EmployeeTokens.BgSurface else EmployeeTokens.BgBase,
        label = "bgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(6.dp))
            .padding(vertical = 6.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ── إجراء (حذف) ─ weight(0.7f) ─ center
        Box(
            modifier = Modifier
                .weight(0.7f)
                .size(22.dp)
                .clip(CircleShape)
                .background(EmployeeTokens.AccentRed.copy(alpha = 0.1f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "حذف",
                tint = EmployeeTokens.AccentRed,
                modifier = Modifier.size(12.dp)
            )
        }

        // ── الإجمالي ─ weight(1f) ─ center
        Text(
            text = "%.2f".format(item.totalPrice),
            color = EmployeeTokens.AccentGreen,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // ── الكمية ─ weight(2f) ─ center
        Row(
            modifier = Modifier.weight(2f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(EmployeeTokens.AccentRed.copy(alpha = 0.15f))
                    .clickable { onDecrement() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "نقصان",
                    tint = EmployeeTokens.AccentRed,
                    modifier = Modifier.size(12.dp)
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(EmployeeTokens.BgElevated)
                    .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(4.dp))
                    .clickable { isEditing = true },
                contentAlignment = Alignment.Center
            ) {
                if (isEditing) {
                    BasicTextField(
                        value = quantityText,
                        onValueChange = { newVal ->
                            if (newVal.all { it.isDigit() } && newVal.length <= 4) {
                                quantityText = newVal
                            }
                        },
                        textStyle = TextStyle(
                            color = EmployeeTokens.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(EmployeeTokens.AccentIndigo),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(36.dp)
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.Enter -> {
                                            val newQty = quantityText.toIntOrNull()
                                            if (newQty != null && newQty > 0) {
                                                onQuantityChange(newQty)
                                            } else {
                                                quantityText = item.quantity.toString()
                                            }
                                            isEditing = false
                                            true
                                        }
                                        Key.Escape -> {
                                            quantityText = item.quantity.toString()
                                            isEditing = false
                                            true
                                        }
                                        else -> false
                                    }
                                } else false
                            },
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.Center) {
                                innerTextField()
                            }
                        }
                    )
                } else {
                    Text(
                        text = "${item.quantity}",
                        color = EmployeeTokens.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .width(36.dp)
                            .padding(horizontal = 2.dp, vertical = 1.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(EmployeeTokens.AccentGreen.copy(alpha = 0.15f))
                    .clickable { onIncrement() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "زيادة",
                    tint = EmployeeTokens.AccentGreen,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // ── سعر الوحدة ─ weight(1f) ─ center
        Text(
            text = "%.2f".format(item.product.price),
            color = EmployeeTokens.TextSecondary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        // ── اسم الصنف ─ weight(2.8f) ─ end
        Column(
            modifier = Modifier.weight(2.8f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = item.product.name,
                color = EmployeeTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "كود: ${item.product.code}",
                    color = EmployeeTokens.TextMuted,
                    fontSize = 9.sp,
                    textAlign = TextAlign.End
                )
                StockBadge(stock = item.product.stock)
            }
        }
    }
}
