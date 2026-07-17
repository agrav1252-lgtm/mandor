package com.mandor.ui.employee

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.domain.model.Invoice
import com.mandor.ui.employee.components.GlowCard
import com.mandor.ui.employee.EmployeeTokens
import com.mandor.util.InvoicePrinter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PastInvoicesTab(invoices: List<Invoice>) {
    val scope = rememberCoroutineScope()
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackSuccess by remember { mutableStateOf(true) }
    var invoiceSearchQuery by remember { mutableStateOf("") }
    var selectedInvoice by remember { mutableStateOf<Invoice?>(null) }

    val filteredInvoices = remember(invoiceSearchQuery, invoices) {
        invoices.filter {
            it.id.contains(invoiceSearchQuery, ignoreCase = true) ||
            it.clientName.contains(invoiceSearchQuery, ignoreCase = true)
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlowCard(
            modifier = Modifier.weight(1.3f).fillMaxHeight(),
            glowColor = if (selectedInvoice != null) EmployeeTokens.AccentGreen else EmployeeTokens.AccentIndigo,
            glowAlpha = if (selectedInvoice != null) 0.15f else 0f
        ) {
            val inv = selectedInvoice
            AnimatedContent(
                targetState = inv,
                transitionSpec = {
                    (fadeIn(tween(250)) + slideInHorizontally { -it / 4 }) togetherWith
                    (fadeOut(tween(200)) + slideOutHorizontally { it / 4 })
                },
                label = "invoiceDetail"
            ) { currentInv ->
                if (currentInv != null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(6.dp), color = EmployeeTokens.AccentGreen.copy(alpha = 0.12f)) {
                                Text("مُصدرة", color = EmployeeTokens.AccentGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Text("تفاصيل الفاتورة: ${currentInv.id}", color = EmployeeTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text("📅 ${currentInv.date}", color = EmployeeTokens.TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))
                        Divider(color = EmployeeTokens.BorderColor, modifier = Modifier.padding(bottom = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Storefront, null, tint = EmployeeTokens.AccentIndigo, modifier = Modifier.size(14.dp))
                                Text(if (currentInv.clientId.isNotEmpty()) "${currentInv.clientName} (${currentInv.clientId})" else currentInv.clientName, color = EmployeeTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(currentInv.items) { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EmployeeTokens.BgBase)
                                        .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("%.2f ج.م".format(item.totalPrice), color = EmployeeTokens.AccentGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(item.product.name, color = EmployeeTokens.TextPrimary, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                                        Text("${item.quantity} × %.2f ج.م".format(item.product.price), color = EmployeeTokens.TextMuted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        Divider(color = EmployeeTokens.BorderColor, modifier = Modifier.padding(vertical = 12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("%.2f ج.م".format(currentInv.totalAmount), color = EmployeeTokens.AccentGreen, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            Text("الإجمالي المستحق:", color = EmployeeTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(14.dp))

                        // ── Print & PDF Buttons ───────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Print Button
                            val printInteraction = remember { MutableInteractionSource() }
                            val printHovered by printInteraction.collectIsHoveredAsState()
                            val printScale by animateFloatAsState(
                                if (printHovered) 1.04f else 1f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "printScale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(printScale)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.horizontalGradient(listOf(
                                        EmployeeTokens.AccentIndigo,
                                        Color(0xFF818CF8)
                                    )))
                                    .clickable(interactionSource = printInteraction, indication = null) {
                                        scope.launch(Dispatchers.IO) {
                                            feedbackSuccess = true
                                            feedbackMessage = "⏳ جاري إرسال للطابعة..."
                                            val ok = InvoicePrinter.printInvoice(currentInv)
                                            feedbackMessage = if (ok) "✓ تم الإرسال إلى الطابعة" else "✗ تم إلغاء الطباعة"
                                            feedbackSuccess = ok
                                        }
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Print, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("طباعة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            // PDF Button
                            val pdfInteraction = remember { MutableInteractionSource() }
                            val pdfHovered by pdfInteraction.collectIsHoveredAsState()
                            val pdfScale by animateFloatAsState(
                                if (pdfHovered) 1.04f else 1f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "pdfScale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(pdfScale)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.horizontalGradient(listOf(
                                        Color(0xFFDC2626),
                                        Color(0xFFEF4444)
                                    )))
                                    .clickable(interactionSource = pdfInteraction, indication = null) {
                                        scope.launch(Dispatchers.IO) {
                                            feedbackSuccess = true
                                            feedbackMessage = "⏳ جاري تصدير PDF..."
                                            val path = InvoicePrinter.exportToPdf(currentInv)
                                            feedbackMessage = if (path != null) "✓ تم حفظ PDF: $path" else "✗ تم إلغاء التصدير"
                                            feedbackSuccess = path != null
                                        }
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("تصدير PDF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // Feedback message
                        AnimatedVisibility(
                            visible = feedbackMessage.isNotEmpty(),
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it }
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (feedbackSuccess) EmployeeTokens.AccentGreen.copy(alpha = 0.15f)
                                        else EmployeeTokens.AccentRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    feedbackMessage,
                                    color = if (feedbackSuccess) EmployeeTokens.AccentGreen else EmployeeTokens.AccentRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val transition = rememberInfiniteTransition(label = "emptyInvoice")
                            val alpha by transition.animateFloat(
                                0.3f, 0.6f,
                                infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
                                label = "emptyAlpha"
                            )
                            Icon(Icons.Default.Receipt, null, tint = EmployeeTokens.TextMuted.copy(alpha = alpha), modifier = Modifier.size(52.dp))
                            Text("اختر فاتورة لعرض تفاصيلها", color = EmployeeTokens.TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        GlowCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("📋 سجل الفواتير", color = EmployeeTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = invoiceSearchQuery,
                    onValueChange = { invoiceSearchQuery = it },
                    placeholder = { Text("بحث برقم الفاتورة أو اسم العميل", color = EmployeeTokens.TextMuted, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = EmployeeTokens.AccentIndigo) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = EmployeeTokens.TextPrimary,
                        focusedBorderColor = EmployeeTokens.AccentIndigo,
                        unfocusedBorderColor = EmployeeTokens.BorderColor,
                        backgroundColor = EmployeeTokens.BgBase
                    )
                )
                LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredInvoices) { inv ->
                        val isSelected = selectedInvoice?.id == inv.id
                        val interactionSource = remember { MutableInteractionSource() }
                        val isHovered by interactionSource.collectIsHoveredAsState()
                        val scale by animateFloatAsState(if (isHovered) 1.01f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "invScale")

                        Card(
                            backgroundColor = when {
                                isSelected -> EmployeeTokens.AccentIndigo.copy(alpha = 0.18f)
                                isHovered -> EmployeeTokens.BgElevated
                                else -> EmployeeTokens.BgBase
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) EmployeeTokens.AccentIndigo.copy(alpha = 0.6f) else if (isHovered) EmployeeTokens.BorderGlow else EmployeeTokens.BorderColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().scale(scale)
                                .clickable(interactionSource = interactionSource, indication = null) { selectedInvoice = inv }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("%.2f ج.م".format(inv.totalAmount), color = EmployeeTokens.AccentGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    val pColor = if (inv.paymentType == "نقدي") EmployeeTokens.AccentGreen else EmployeeTokens.AccentAmber
                                    Surface(shape = RoundedCornerShape(4.dp), color = pColor.copy(alpha = 0.15f)) {
                                        Text(inv.paymentType, color = pColor, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (inv.clientId.isNotEmpty()) "${inv.clientName} (${inv.clientId})" else inv.clientName, color = EmployeeTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${inv.id}  |  ${inv.date}", color = EmployeeTokens.TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
