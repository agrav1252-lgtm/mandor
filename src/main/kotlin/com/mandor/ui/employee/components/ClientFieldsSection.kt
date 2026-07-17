package com.mandor.ui.employee.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mandor.ui.employee.EmployeeTokens
import com.mandor.ui.employee.viewmodel.CreateInvoiceViewModel

@Composable
fun ClientFieldsSection(
    viewModel: CreateInvoiceViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
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

        Box(modifier = Modifier.weight(1f)) {
            CompactInputField(
                value = viewModel.clientName,
                onValueChange = viewModel::onClientNameInputChange,
                label = "اسم العميل / المكتبة",
                placeholder = "ابحث بالاسم أو الهاتف",
                leadingIcon = {
                    Icon(
                        Icons.Default.Storefront,
                        null,
                        tint = EmployeeTokens.AccentIndigo.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            ClientNameSuggestionsDropdown(
                expanded = viewModel.showClientNameSuggestions,
                suggestions = viewModel.clientNameSuggestions,
                onDismiss = viewModel::dismissSuggestions,
                onSelect = viewModel::selectClient
            )
        }

        ClientInfoCard(
            phone = viewModel.clientPhone,
            storeName = viewModel.clientStoreName,
            governorate = viewModel.clientGovernorate,
            visible = viewModel.hasClientDetails
        )
    }
}
