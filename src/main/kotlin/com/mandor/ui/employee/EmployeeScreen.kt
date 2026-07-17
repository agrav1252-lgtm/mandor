package com.mandor.ui.employee

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.mandor.ui.employee.components.AnimatedTabButton
import org.koin.compose.koinInject

/**
 * Main employee dashboard screen.
 * Uses Koin DI to inject ViewModels - no manual construction needed!
 */
@Composable
fun EmployeeDashboard(modifier: Modifier = Modifier) {
    // ── Koin Dependency Injection ────────────────────────────────
    // koinInject() automatically provides the ViewModel instances
    val employeeViewModel: com.mandor.ui.employee.viewmodel.EmployeeViewModel = koinInject()
    val createInvoiceViewModel: com.mandor.ui.employee.viewmodel.CreateInvoiceViewModel = koinInject()
    val addClientViewModel: com.mandor.ui.employee.viewmodel.AddClientViewModel = koinInject()

    // ── Collect past invoices from EmployeeViewModel ────────
    val pastInvoices = employeeViewModel.pastInvoices

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EmployeeTokens.BgDeep)
            .padding(16.dp)
    ) {
        // ── Navigation Bar ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(EmployeeTokens.BgElevated, EmployeeTokens.BgSurface)))
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(
                        EmployeeTokens.BorderGlow,
                        EmployeeTokens.BorderColor,
                        EmployeeTokens.BorderColor,
                        EmployeeTokens.BorderGlow
                    )),
                    RoundedCornerShape(14.dp)
                )
                .padding(6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabTitles = listOf("📋 سجل الفواتير", "🧾 فاتورة جديدة", "➕ إضافة عميل", "👥 بحث العملاء")
            tabTitles.forEachIndexed { index, title ->
                AnimatedTabButton(
                    title = title,
                    isSelected = employeeViewModel.activeTab == index,
                    onClick = { employeeViewModel.activeTab = index },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── Tab Content ─────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = employeeViewModel.activeTab,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { it * direction * -1 } + fadeIn(tween(250))) togetherWith
                    (slideOutHorizontally { it * direction } + fadeOut(tween(200)))
                },
                label = "tabSwitch"
            ) { tab ->
                when (tab) {
                    3 -> SearchClientsTab()
                    2 -> AddClientTab(viewModel = addClientViewModel)
                    1 -> CreateInvoiceTab(
                        viewModel = createInvoiceViewModel,
                        pastInvoices = pastInvoices,
                        onInvoiceCreated = { newInv ->
                            employeeViewModel.addInvoiceToList(newInv)
                        }
                    )
                    0 -> PastInvoicesTab(pastInvoices)
                }
            }
        }
    }
}
