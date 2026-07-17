package com.mandor.ui.employee.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.domain.model.Product
import com.mandor.ui.employee.EmployeeTokens
import kotlinx.coroutines.delay

@Composable
fun ProductSearchDialog(
    visible: Boolean,
    searchQuery: String,
    products: List<Product>,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onProductSelect: (Product) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(tween(220), initialScale = 0.94f),
        exit = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.94f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .fillMaxHeight(0.7f)
                    .shadow(24.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(listOf(EmployeeTokens.BgElevated, EmployeeTokens.BgSurface)))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                EmployeeTokens.AccentIndigo.copy(alpha = 0.6f),
                                EmployeeTokens.BorderColor,
                                EmployeeTokens.AccentIndigo.copy(alpha = 0.3f)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(enabled = false) {}
                    .padding(22.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EmployeeTokens.AccentRed.copy(alpha = 0.15f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, null, tint = EmployeeTokens.AccentRed, modifier = Modifier.size(16.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(6.dp), color = EmployeeTokens.AccentIndigo.copy(alpha = 0.2f)) {
                            Text(
                                " F3 ",
                                color = EmployeeTokens.AccentIndigo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            "البحث السريع عن المنتجات",
                            color = EmployeeTokens.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            "ابحث باسم الصنف، الكود، أو الباركود...",
                            fontSize = 12.sp,
                            color = EmployeeTokens.TextMuted
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = EmployeeTokens.AccentIndigo) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = EmployeeTokens.TextPrimary,
                        focusedBorderColor = EmployeeTokens.AccentIndigo,
                        unfocusedBorderColor = EmployeeTokens.BorderColor,
                        backgroundColor = EmployeeTokens.BgBase,
                        focusedLabelColor = EmployeeTokens.AccentIndigo
                    ),
                    singleLine = true
                )

                LaunchedEffect(visible) {
                    if (visible) {
                        delay(80)
                        focusRequester.requestFocus()
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "${products.size} نتيجة",
                    color = EmployeeTokens.TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(products, key = { _, p -> p.code }) { _, product ->
                        ProductSearchResultRow(product = product, onSelect = { onProductSelect(product) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductSearchResultRow(product: Product, onSelect: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val canAdd = product.stock > 0
    val scale by animateFloatAsState(
        if (hovered && canAdd) 1.015f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "itemScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (hovered && canAdd) {
                    Brush.horizontalGradient(listOf(EmployeeTokens.AccentIndigo.copy(alpha = 0.12f), EmployeeTokens.BgBase))
                } else {
                    Brush.horizontalGradient(listOf(EmployeeTokens.BgBase, EmployeeTokens.BgBase))
                }
            )
            .border(
                1.dp,
                if (hovered && canAdd) EmployeeTokens.AccentIndigo.copy(alpha = 0.4f)
                else EmployeeTokens.BorderColor.copy(alpha = 0.5f),
                RoundedCornerShape(10.dp)
            )
            .clickable(interactionSource = interaction, indication = null, enabled = canAdd, onClick = onSelect)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "%.2f ج.م".format(product.price),
                color = EmployeeTokens.AccentGreen,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
            StockBadge(product.stock)
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                product.name,
                color = if (!canAdd) EmployeeTokens.TextMuted else EmployeeTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.End
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(product.category, color = EmployeeTokens.AccentIndigo.copy(alpha = 0.8f), fontSize = 10.sp)
                Text("·", color = EmployeeTokens.TextMuted, fontSize = 10.sp)
                Text("كود: ${product.code}", color = EmployeeTokens.TextMuted, fontSize = 10.sp)
            }
        }
    }
}
