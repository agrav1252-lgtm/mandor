package com.mandor.ui.employee

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.ui.employee.components.ClientCodeSuggestionsDropdown
import com.mandor.ui.employee.components.ClientNameSuggestionsDropdown
import com.mandor.ui.employee.components.CompactInputField
import com.mandor.ui.employee.viewmodel.AddClientViewModel
import kotlinx.coroutines.delay

/**
 * Tab إضافة عميل جديد
 */
@Composable
fun AddClientTab(
    viewModel: AddClientViewModel,
    modifier: Modifier = Modifier
) {
    // إخفاء Feedback بعد 4 ثواني
    LaunchedEffect(viewModel.feedbackMessage) {
        if (viewModel.feedbackMessage.isNotEmpty()) {
            delay(4000)
            viewModel.clearFeedback()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (viewModel.isEditing) "✏️ ${viewModel.editingClientId}" else "➕ عميل جديد",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = EmployeeTokens.TextPrimary
            )

            // ── بطاقة النموذج ──────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmployeeTokens.BgElevated)
                    .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ── رقم العميل مع زر التوليد التلقائي ──────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CompactInputField(
                            value = viewModel.clientId,
                            onValueChange = viewModel::updateClientId,
                            label = "رقم العميل * — Enter للبحث",
                            placeholder = "6 أرقام",
                            onEnterPressed = viewModel::lookupClient,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ClientCodeSuggestionsDropdown(
                            expanded = viewModel.showClientCodeSuggestions,
                            suggestions = viewModel.clientCodeSuggestions,
                            onDismiss = viewModel::dismissSuggestions,
                            onSelect = viewModel::selectClient
                        )
                    }

                    Button(
                        onClick = viewModel::generateAutoId,
                        modifier = Modifier.height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = EmployeeTokens.AccentIndigo,
                            contentColor = EmployeeTokens.TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تلقائي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CompactInputField(
                            value = viewModel.clientName,
                            onValueChange = viewModel::updateClientName,
                            label = "اسم العميل *",
                            placeholder = "الاسم الكامل",
                            modifier = Modifier.fillMaxWidth()
                        )
                        ClientNameSuggestionsDropdown(
                            expanded = viewModel.showClientNameSuggestions,
                            suggestions = viewModel.clientNameSuggestions,
                            onDismiss = viewModel::dismissSuggestions,
                            onSelect = viewModel::selectClient
                        )
                    }
                    CompactInputField(
                        value = viewModel.clientPhone,
                        onValueChange = viewModel::updateClientPhone,
                        label = "الهاتف",
                        placeholder = "01012345678",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactInputField(
                        value = viewModel.storeName,
                        onValueChange = viewModel::updateStoreName,
                        label = "المكتبة / المكان",
                        placeholder = "مكتبة النور",
                        modifier = Modifier.weight(1f)
                    )
                    CompactInputField(
                        value = viewModel.governorate,
                        onValueChange = viewModel::updateGovernorate,
                        label = "المحافظة",
                        placeholder = "القاهرة",
                        modifier = Modifier.weight(1f)
                    )
                }

                CompactInputField(
                    value = viewModel.address,
                    onValueChange = viewModel::updateAddress,
                    label = "العنوان",
                    placeholder = "الشارع، المنطقة..."
                )

                CompactInputField(
                    value = viewModel.notes,
                    onValueChange = viewModel::updateNotes,
                    label = "ملاحظات",
                    placeholder = "أي معلومات إضافية...",
                    multiline = true,
                    minLines = 2
                )

                // ── أزرار الحفظ والمسح ──────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveClient() },
                        enabled = !viewModel.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = EmployeeTokens.AccentGreen,
                            contentColor = EmployeeTokens.TextPrimary,
                            disabledBackgroundColor = EmployeeTokens.BorderColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                color = EmployeeTokens.TextPrimary,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (viewModel.isEditing) "💾 حفظ التعديلات" else "💾 حفظ العميل",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = viewModel::clearForm,
                        enabled = !viewModel.isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = EmployeeTokens.BgSurface,
                            contentColor = EmployeeTokens.TextSecondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            EmployeeTokens.BorderColor
                        )
                    ) {
                        Text(
                            "🗑️ مسح",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (viewModel.isEditing) {
                    OutlinedButton(
                        onClick = viewModel::clearForm,
                        enabled = !viewModel.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            backgroundColor = EmployeeTokens.BgSurface,
                            contentColor = EmployeeTokens.AccentRed
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            EmployeeTokens.AccentRed.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("❌ إلغاء التعديل", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // ── Feedback Overlay ────────────────────────────────
        AnimatedVisibility(
            visible = viewModel.feedbackMessage.isNotEmpty(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (viewModel.isSuccess) EmployeeTokens.AccentGreen
                        else EmployeeTokens.AccentRed
                    )
                        .border(
                            1.dp,
                            if (viewModel.isSuccess) EmployeeTokens.AccentGreen.copy(alpha = 0.5f)
                            else EmployeeTokens.AccentRed.copy(alpha = 0.5f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.feedbackMessage,
                    color = EmployeeTokens.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── Loading Overlay ─────────────────────────────────
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EmployeeTokens.BgDeep.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = EmployeeTokens.AccentIndigo,
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
            }
        }

    }
}
