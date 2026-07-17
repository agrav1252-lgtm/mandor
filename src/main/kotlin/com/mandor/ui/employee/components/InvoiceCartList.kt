package com.mandor.ui.employee.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mandor.domain.model.InvoiceItem
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun InvoiceCartList(
    cartItems: List<InvoiceItem>,
    onRemove: (Int) -> Unit,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onQuantityChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        itemsIndexed(cartItems, key = { _, item -> item.product.code }) { index, item ->
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally { it } + fadeIn(tween(300, delayMillis = index * 40)),
            ) {
                CartItemRow(
                    item = item,
                    index = index,
                    onRemove = { onRemove(index) },
                    onIncrement = { onIncrement(index) },
                    onDecrement = { onDecrement(index) },
                    onQuantityChange = { newQty -> onQuantityChange(index, newQty) }
                )
            }
            if (index < cartItems.lastIndex) {
                Divider(
                    color = EmployeeTokens.BorderColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}
