package com.mandor.ui.employee.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mandor.domain.model.Invoice
import com.mandor.ui.employee.viewmodel.CreateInvoiceViewModel

@Composable
fun InvoiceToolbar(
    viewModel: CreateInvoiceViewModel,
    pastInvoices: List<Invoice>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── كود الفاتورة ─
            Box(modifier = Modifier.weight(1f)) {
                CompactInputField(
                    value = viewModel.invoiceSearchId,
                    onValueChange = { viewModel.onInvoiceSearchIdChange(it, pastInvoices) },
                    label = "كود الفاتورة",
                    placeholder = "INV-1001",
                    onEnterPressed = { viewModel.searchAndLoadInvoice(pastInvoices) },
                    modifier = Modifier.fillMaxWidth()
                )
                InvoiceSuggestionsDropdown(
                    expanded = viewModel.showInvoiceSuggestions && viewModel.invoiceSearchId.isNotEmpty(),
                    suggestions = viewModel.invoiceSuggestions,
                    onDismiss = viewModel::dismissSuggestions,
                    onSelect = { viewModel.selectInvoice(it) }
                )
            }

            // ── كود العميل ─
            Box(modifier = Modifier.weight(1f)) {
                CompactInputField(
                    value = viewModel.clientIdInput,
                    onValueChange = viewModel::onClientIdInputChange,
                    label = "كود العميل",
                    placeholder = "123456",
                    isError = viewModel.clientNotFound,
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

            // ── اسم العميل ─
            Box(modifier = Modifier.weight(1f)) {
                CompactInputField(
                    value = viewModel.clientName,
                    onValueChange = viewModel::onClientNameInputChange,
                    label = "اسم العميل / المكتبة",
                    placeholder = "ابحث بالاسم أو الهاتف",
                    modifier = Modifier.fillMaxWidth()
                )
                ClientNameSuggestionsDropdown(
                    expanded = viewModel.showClientNameSuggestions,
                    suggestions = viewModel.clientNameSuggestions,
                    onDismiss = viewModel::dismissSuggestions,
                    onSelect = viewModel::selectClient
                )
            }

            // ── أزرار ─
            RestoreLastInvoiceButton(
                onClick = { viewModel.restoreLastInvoice(pastInvoices) },
                modifier = Modifier.height(32.dp)
            )

            PaymentTypeSelector(
                paymentType = viewModel.paymentType,
                options = viewModel.paymentOptions,
                expanded = viewModel.showPaymentDropdown,
                onToggle = viewModel::togglePaymentDropdown,
                onDismiss = viewModel::dismissPaymentDropdown,
                onSelect = viewModel::selectPaymentType
            )
        }
    }
}
