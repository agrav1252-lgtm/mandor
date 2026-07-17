package com.mandor.ui.employee

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.mandor.domain.model.Invoice
import com.mandor.ui.components.DangerConfirmationDialog
import com.mandor.ui.components.LoadingOverlay
import com.mandor.ui.employee.components.*
import com.mandor.ui.employee.viewmodel.CreateInvoiceViewModel

@Composable
fun CreateInvoiceTab(
    viewModel: CreateInvoiceViewModel,
    pastInvoices: List<Invoice>,
    onInvoiceCreated: (Invoice) -> Unit
) {
    val rootFocusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(rootFocusRequester)
            .focusTarget()
            .onPreviewKeyEvent { ev ->
                if (ev.type == KeyEventType.KeyDown) {
                    when {
                        ev.key == Key.F3 -> {
                            viewModel.openProductSearch()
                            true
                        }
                        ev.key == Key.Enter -> {
                            viewModel.processBarcodeScan()
                            true
                        }
                        ev.key == Key.Backspace -> {
                            if (viewModel.barcodeBuffer.isNotEmpty()) {
                                viewModel.barcodeBuffer = viewModel.barcodeBuffer.dropLast(1)
                            }
                            true
                        }
                        ev.key.nativeKeyCode in 32..126 -> {
                            val char = ev.key.nativeKeyCode.toChar()
                            viewModel.appendBarcodeChar(char)
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        LaunchedEffect(Unit) { rootFocusRequester.requestFocus() }

        GlowCard(
            modifier = Modifier.fillMaxSize(),
            glowColor = if (viewModel.isEditing) EmployeeTokens.AccentAmber else EmployeeTokens.AccentIndigo,
            glowAlpha = if (viewModel.isEditing) 0.35f else 0.15f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                InvoiceEditBanner(
                    editingInvoiceId = viewModel.editingInvoiceId,
                    onCancelEdit = viewModel::requestCancelEdit
                )

                InvoiceToolbar(viewModel = viewModel, pastInvoices = pastInvoices)

                if (viewModel.paymentType == "دفع مجزء") {
                    PaidAmountInput(
                        paidAmount = viewModel.paidAmount,
                        totalAmount = viewModel.total,
                        isError = viewModel.showPaidAmountError,
                        onValueChange = viewModel::onPaidAmountChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                InvoiceTableHeader()

                if (viewModel.cartItems.isEmpty()) {
                    EmptyCartView(modifier = Modifier.weight(1f))
                } else {
                    InvoiceCartList(
                        cartItems = viewModel.cartItems,
                        onRemove = viewModel::removeCartItem,
                        onIncrement = viewModel::incrementCartItem,
                        onDecrement = viewModel::decrementCartItem,
                        onQuantityChange = viewModel::setCartItemQuantity,
                        modifier = Modifier.weight(1f)
                    )
                }

                InvoiceActionBar(
                    isEditing = viewModel.isEditing,
                    hasCartItems = viewModel.cartItems.isNotEmpty(),
                    formattedTotal = viewModel.formattedTotal,
                    itemCount = viewModel.cartItemCount,
                    totalQuantity = viewModel.cartTotalQuantity,
                    onSave = { viewModel.saveInvoice(onInvoiceCreated) },
                    onPrint = viewModel::printPreview,
                    onExportPdf = viewModel::exportPreviewPdf,
                    onClearCart = viewModel::requestClearCart,
                    paymentType = viewModel.paymentType,
                    formattedRemaining = viewModel.formattedRemaining
                )
            }
        }

        FeedbackToast(
            message = viewModel.feedbackMessage,
            isSuccess = viewModel.isSuccess
        )

        ProductSearchDialog(
            visible = viewModel.showSearchDialog,
            searchQuery = viewModel.dialogSearchQuery,
            products = viewModel.filteredProducts,
            onSearchQueryChange = viewModel::onDialogSearchQueryChange,
            onDismiss = viewModel::closeProductSearch,
            onProductSelect = viewModel::addProductToCart
        )

        LoadingOverlay(isLoading = viewModel.isLoading, message = "جاري حفظ الفاتورة...")

        DangerConfirmationDialog(
            visible = viewModel.showClearCartDialog,
            title = "مسح السلة؟",
            message = "هل أنت متأكد من رغبتك في مسح جميع المنتجات من السلة؟ لا يمكن التراجع عن هذه العملية.",
            confirmText = "مسح السلة",
            cancelText = "إلغاء",
            onConfirm = viewModel::confirmClearCart,
            onDismiss = viewModel::dismissClearCartDialog
        )

        DangerConfirmationDialog(
            visible = viewModel.showCancelEditDialog,
            title = "إلغاء التعديل؟",
            message = "هل أنت متأكد من إلغاء تعديل الفاتورة؟ سيتم فقدان جميع التغييرات.",
            confirmText = "إلغاء التعديل",
            cancelText = "العودة",
            onConfirm = viewModel::confirmCancelEdit,
            onDismiss = viewModel::dismissCancelEditDialog
        )

        if (viewModel.showProductNotFoundDialog) {
            ProductNotFoundDialog(
                barcode = viewModel.notFoundBarcode,
                onDismiss = viewModel::dismissProductNotFoundDialog
            )
        }

    }
}
