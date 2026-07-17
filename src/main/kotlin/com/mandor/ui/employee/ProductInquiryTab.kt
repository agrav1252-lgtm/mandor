package com.mandor.ui.employee

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.domain.model.Product
import com.mandor.ui.employee.components.GlowCard
import com.mandor.ui.employee.components.StockBadge
import com.mandor.ui.employee.EmployeeTokens
import org.koin.compose.koinInject

/**
 * Product Inquiry Tab - now using Koin DI!
 * No more manual repository construction
 */
@Composable
fun ProductInquiryTab() {
    // ✓ Inject ViewModel using Koin
    val viewModel: com.mandor.ui.employee.viewmodel.ProductInquiryViewModel = koinInject()
    
    var query by remember { mutableStateOf("") }
    
    // State from ViewModel
    val products = viewModel.products
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    val filteredProducts = remember(query, products) {
        if (query.isBlank()) products
        else products.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.code == query.trim() ||
            it.barcode == query.trim()
        }
    }

    GlowCard(modifier = Modifier.fillMaxSize(), glowColor = EmployeeTokens.AccentBlue, glowAlpha = 0.1f) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "🔍 الاستعلام السريع عن المنتجات",
                color = EmployeeTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("ابحث بالاسم، كود الصنف، أو الرمز الشريطي...", color = EmployeeTokens.TextMuted, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = EmployeeTokens.AccentBlue) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = EmployeeTokens.TextPrimary,
                    focusedBorderColor = EmployeeTokens.AccentBlue,
                    unfocusedBorderColor = EmployeeTokens.BorderColor,
                    backgroundColor = EmployeeTokens.BgBase,
                    focusedLabelColor = EmployeeTokens.AccentBlue
                )
            )

            // Error message display
            if (errorMessage.isNotEmpty()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(EmployeeTokens.AccentRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, EmployeeTokens.AccentRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.retry() }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "إعادة المحاولة",
                            tint = EmployeeTokens.AccentRed,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                        Text(
                            text = "إعادة المحاولة",
                            color = EmployeeTokens.AccentRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = errorMessage,
                        color = EmployeeTokens.AccentRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmployeeTokens.AccentBlue)
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(filteredProducts) { idx, prod ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()
                    val scale by animateFloatAsState(if (isHovered) 1.01f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "cardScale")

                    Card(
                        backgroundColor = if (isHovered) EmployeeTokens.BgElevated else EmployeeTokens.BgBase,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isHovered) EmployeeTokens.AccentBlue.copy(alpha = 0.4f) else EmployeeTokens.BorderColor
                        ),
                        modifier = Modifier.fillMaxWidth().scale(scale)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "%.2f ج.م".format(prod.price),
                                    color = EmployeeTokens.AccentGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                                StockBadge(prod.stock)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(prod.name, color = EmployeeTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.End)
                                Text("كود: ${prod.code}  |  باركود: ${prod.barcode}", color = EmployeeTokens.TextMuted, fontSize = 11.sp)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmployeeTokens.AccentBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(prod.category, color = EmployeeTokens.AccentBlue.copy(alpha = 0.9f), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Text(prod.description, color = EmployeeTokens.TextSecondary, fontSize = 11.sp, textAlign = TextAlign.End, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}
