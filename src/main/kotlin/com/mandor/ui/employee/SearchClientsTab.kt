package com.mandor.ui.employee

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.mandor.domain.model.Client
import com.mandor.ui.employee.components.CompactInputField
import com.mandor.ui.employee.viewmodel.SearchClientsViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.delay

/**
 * Tab البحث عن العملاء
 */
@Composable
fun SearchClientsTab(
    modifier: Modifier = Modifier
) {
    val viewModel: SearchClientsViewModel = koinInject()
    val clipboardManager = LocalClipboardManager.current
    var copiedMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // إخفاء رسالة النسخ بعد 2 ثانية
    LaunchedEffect(copiedMessage) {
        if (copiedMessage.isNotEmpty()) {
            delay(2000)
            copiedMessage = ""
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadClients()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── عنوان + حقل البحث ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔍 البحث عن العملاء",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmployeeTokens.TextPrimary
                )
                
                Text(
                    text = "عدد العملاء: ${viewModel.filteredClients.size}",
                    fontSize = 13.sp,
                    color = EmployeeTokens.TextSecondary
                )
            }

            // ── حقل البحث ───────────────────────────────────
            CompactInputField(
                value = viewModel.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = "ابحث بالاسم، الرقم، الهاتف، أو المحافظة",
                placeholder = "أدخل كلمة البحث...",
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = EmployeeTokens.AccentIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            Divider(color = EmployeeTokens.BorderColor, thickness = 1.dp)

            // ── قائمة النتائج ────────────────────────────────
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = EmployeeTokens.AccentIndigo,
                        strokeWidth = 3.dp
                    )
                }
            } else if (viewModel.filteredClients.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "لا توجد نتائج",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmployeeTokens.TextSecondary
                        )
                        Text(
                            text = if (viewModel.searchQuery.isEmpty()) "قم بإضافة عملاء جدد" else "جرب كلمة بحث أخرى",
                            fontSize = 13.sp,
                            color = EmployeeTokens.TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.filteredClients) { client ->
                        ClientCard(
                            client = client,
                            onCopyId = {
                                clipboardManager.setText(AnnotatedString(client.id))
                                copiedMessage = "تم نسخ الكود: ${client.id}"
                            },
                            onCopyAll = {
                                val allData = buildString {
                                    appendLine("كود العميل: ${client.id}")
                                    appendLine("الاسم: ${client.name}")
                                    if (client.phone.isNotEmpty()) appendLine("الهاتف: ${client.phone}")
                                    if (client.storeName.isNotEmpty()) appendLine("المكتبة: ${client.storeName}")
                                    if (client.governorate.isNotEmpty()) appendLine("المحافظة: ${client.governorate}")
                                    if (client.address.isNotEmpty()) appendLine("العنوان: ${client.address}")
                                    if (client.notes.isNotEmpty()) appendLine("ملاحظات: ${client.notes}")
                                }
                                clipboardManager.setText(AnnotatedString(allData))
                                copiedMessage = "تم نسخ جميع بيانات ${client.name}"
                            }
                        )
                    }
                }
            }
        }

        // رسالة النسخ
        AnimatedVisibility(
            visible = copiedMessage.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Card(
                backgroundColor = EmployeeTokens.AccentGreen,
                shape = RoundedCornerShape(10.dp),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = EmployeeTokens.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = copiedMessage,
                        color = EmployeeTokens.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ClientCard(
    client: Client,
    onCopyId: () -> Unit,
    onCopyAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        backgroundColor = EmployeeTokens.BgElevated,
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .border(
                    1.dp,
                    EmployeeTokens.BorderColor,
                    RoundedCornerShape(12.dp)
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── الرقم والاسم مع زر النسخ ──────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // زر نسخ جميع البيانات
                    IconButton(
                        onClick = onCopyAll,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "نسخ جميع البيانات",
                            tint = EmployeeTokens.AccentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // زر نسخ الكود فقط
                    OutlinedButton(
                        onClick = onCopyId,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = EmployeeTokens.AccentIndigo.copy(alpha = 0.1f),
                            contentColor = EmployeeTokens.AccentIndigo
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = client.id,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Text(
                    text = client.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmployeeTokens.TextPrimary
                )
            }

            Divider(color = EmployeeTokens.BorderColor.copy(alpha = 0.5f))

            // ── التفاصيل ────────────────────────────────────
            if (client.phone.isNotEmpty()) {
                InfoRow("📞 الهاتف:", client.phone)
            }
            if (client.storeName.isNotEmpty()) {
                InfoRow("🏪 المكان:", client.storeName)
            }
            if (client.governorate.isNotEmpty()) {
                InfoRow("📍 المحافظة:", client.governorate)
            }
            if (client.address.isNotEmpty()) {
                InfoRow("🏠 العنوان:", client.address)
            }
            if (client.notes.isNotEmpty()) {
                InfoRow("📝 ملاحظات:", client.notes)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value,
            fontSize = 13.sp,
            color = EmployeeTokens.TextPrimary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = EmployeeTokens.TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}
