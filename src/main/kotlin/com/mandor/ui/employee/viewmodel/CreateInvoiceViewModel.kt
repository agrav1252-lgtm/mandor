package com.mandor.ui.employee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mandor.domain.model.Client
import com.mandor.domain.model.Invoice
import com.mandor.domain.model.InvoiceItem
import com.mandor.domain.model.Product
import com.mandor.domain.repository.InvoiceRepository
import com.mandor.util.InvoicePrinter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * ViewModel for the Create Invoice screen.
 * Owns all mutable UI state and business logic for creating/editing invoices.
 */
class CreateInvoiceViewModel(private val repository: InvoiceRepository) {
    private val scope = CoroutineScope(Dispatchers.Default)

    // ── Form State ───────────────────────────────────────────
    var clientName by mutableStateOf("")
    var clientIdInput by mutableStateOf("")
    var resolvedClientId by mutableStateOf("")
    var clientPhone by mutableStateOf("")
    var clientStoreName by mutableStateOf("")
    var clientGovernorate by mutableStateOf("")
    var clientNotFound by mutableStateOf(false)
    var invoiceSearchId by mutableStateOf("")
    var editingInvoiceId by mutableStateOf<String?>(null)
    val cartItems = mutableStateListOf<InvoiceItem>()

    // ── اقتراحات ────────────────────────────────────────────
    var clientCodeSuggestions by mutableStateOf<List<Client>>(emptyList())
        private set
    var showClientCodeSuggestions by mutableStateOf(false)
        private set

    var clientNameSuggestions by mutableStateOf<List<Client>>(emptyList())
        private set
    var showClientNameSuggestions by mutableStateOf(false)
        private set

    var invoiceSuggestions by mutableStateOf<List<Invoice>>(emptyList())
        private set
    var showInvoiceSuggestions by mutableStateOf(false)
        private set

    var paymentType by mutableStateOf("نقدي")
    var showPaymentDropdown by mutableStateOf(false)
    val paymentOptions = listOf("نقدي", "آجل", "دفع مجزء")
    var paidAmount by mutableStateOf("")
    var showPaidAmountError by mutableStateOf(false)

    // ── الباركود (تلقاءي عبر السكانر) ────────────────────────
    var barcodeBuffer by mutableStateOf("")
    var showProductNotFoundDialog by mutableStateOf(false)
    var notFoundBarcode by mutableStateOf("")

    // ── Data State ───────────────────────────────────────────
    var products by mutableStateOf<List<Product>>(emptyList())
        private set
    var clients by mutableStateOf<List<Client>>(emptyList())
        private set

    // ── UI Feedback ──────────────────────────────────────────
    var feedbackMessage by mutableStateOf("")
    var isSuccess by mutableStateOf(true)
    var isLoading by mutableStateOf(false)

    // ── Dialogs ──────────────────────────────────────────────
    var showSearchDialog by mutableStateOf(false)
    var dialogSearchQuery by mutableStateOf("")
    var showClearCartDialog by mutableStateOf(false)
    var showCancelEditDialog by mutableStateOf(false)

    // ── Computed ─────────────────────────────────────────────
    val total: Double get() = cartItems.sumOf { it.totalPrice }
    val formattedTotal: String get() = "%.2f".format(total)
    val cartItemCount: Int get() = cartItems.size
    val cartTotalQuantity: Int get() = cartItems.sumOf { it.quantity }
    val paidAmountDouble: Double get() = paidAmount.toDoubleOrNull() ?: 0.0
    val remainingAmount: Double get() = if (paymentType == "دفع مجزء") (total - paidAmountDouble).coerceAtLeast(0.0) else 0.0
    val formattedRemaining: String get() = "%.2f".format(remainingAmount)
    val hasClientDetails: Boolean
        get() = clientPhone.isNotEmpty() || clientStoreName.isNotEmpty() || clientGovernorate.isNotEmpty()
    val isEditing: Boolean get() = editingInvoiceId != null
    val filteredProducts: List<Product>
        get() {
            val query = dialogSearchQuery
            if (query.isBlank()) return products
            return products.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.code == query.trim() ||
                    it.barcode == query.trim()
            }
        }

    init {
        loadData()
    }

    fun loadData() {
        scope.launch {
            isLoading = true

            repository.getProducts()
                .onSuccess { productList -> products = productList }
                .onError { error ->
                    showError("✗ ${error.getUserMessage()}")
                }

            repository.getClients()
                .onSuccess { clientList -> clients = clientList }
                .onError { error ->
                    showError("✗ ${error.getUserMessage()}")
                }

            isLoading = false
        }
    }

    fun clearFeedbackAfterDelay() {
        scope.launch {
            delay(4000)
            if (feedbackMessage.isNotEmpty()) {
                feedbackMessage = ""
            }
        }
    }

    // ── Product Search Dialog ────────────────────────────────

    fun openProductSearch() {
        showSearchDialog = true
        dialogSearchQuery = ""
    }

    fun closeProductSearch() {
        showSearchDialog = false
    }

    fun onDialogSearchQueryChange(query: String) {
        dialogSearchQuery = query
    }

    fun addProductToCart(product: Product) {
        if (product.stock <= 0) return

        val existingIndex = cartItems.indexOfFirst { it.product.code == product.code }
        if (existingIndex != -1) {
            val currentQty = cartItems[existingIndex].quantity
            if (currentQty + 1 <= product.stock) {
                cartItems[existingIndex] = InvoiceItem(product, currentQty + 1)
                showSuccess("✓ تمت إضافة ${product.name}")
            } else {
                showError("⚠ تجاوزت الكمية المخزون المتاح!")
            }
        } else {
            cartItems.add(InvoiceItem(product, 1))
            showSuccess("✓ تمت إضافة ${product.name}")
        }
        closeProductSearch()
    }

    // ── Barcode Auto-Scan ────────────────────────────────────

    fun appendBarcodeChar(char: Char) {
        barcodeBuffer += char
    }

    fun processBarcodeScan() {
        val barcode = barcodeBuffer.trim()
        barcodeBuffer = ""
        if (barcode.isEmpty()) return

        val product = products.firstOrNull { it.barcode == barcode }
        if (product == null) {
            notFoundBarcode = barcode
            showProductNotFoundDialog = true
            return
        }

        if (product.stock <= 0) {
            showError("⚠ ${product.name} غير متوفر في المخزن!")
            return
        }

        val existingIndex = cartItems.indexOfFirst { it.product.code == product.code }
        if (existingIndex != -1) {
            val currentQty = cartItems[existingIndex].quantity
            if (currentQty + 1 <= product.stock) {
                cartItems[existingIndex] = InvoiceItem(product, currentQty + 1)
                showSuccess("✓ ${product.name} (${currentQty + 1})")
            } else {
                showError("⚠ تجاوزت الكمية المخزون المتاح!")
            }
        } else {
            cartItems.add(InvoiceItem(product, 1))
            showSuccess("✓ تمت إضافة ${product.name}")
        }
    }

    fun dismissProductNotFoundDialog() {
        showProductNotFoundDialog = false
        notFoundBarcode = ""
    }

    // ── Invoice Search ───────────────────────────────────────

    fun onInvoiceSearchIdChange(query: String, pastInvoices: List<Invoice>) {
        invoiceSearchId = query
        updateInvoiceSuggestions(query, pastInvoices)
    }

    fun searchAndLoadInvoice(pastInvoices: List<Invoice>) {
        val code = invoiceSearchId.trim()
        val found = pastInvoices.firstOrNull { it.id.equals(code, ignoreCase = true) }
        if (found != null) {
            selectInvoice(found)
        } else {
            showError("✗ لم يتم العثور على الفاتورة!")
        }
    }

    // ── Client Fields ────────────────────────────────────────

    fun onClientIdInputChange(query: String) {
        clientIdInput = query
        updateClientCodeSuggestions(query)
    }

    fun onClientNameInputChange(query: String) {
        clientName = query
        updateClientNameSuggestions(query)
    }

    fun lookupClient() {
        val code = clientIdInput.trim()

        if (code.isEmpty()) {
            clientNotFound = false
            clearClientData()
            return
        }

        if (!code.all { it.isDigit() } || code.length != 6) {
            clientNotFound = true
            clearClientData()
            showError("⚠️ كود العميل يجب أن يكون 6 أرقام")
            return
        }

        val found = clients.firstOrNull { it.id == code }
        if (found != null) {
            selectClient(found)
        } else {
            clientNotFound = true
            clearClientData()
            showError("⚠️ العميل غير موجود")
        }
    }

    fun updateClientCodeSuggestions(query: String) {
        if (query.isEmpty()) {
            clientCodeSuggestions = emptyList()
            showClientCodeSuggestions = false
            return
        }

        val filtered = clients.filter { it.id.contains(query, ignoreCase = true) }.take(5)
        clientCodeSuggestions = filtered
        showClientCodeSuggestions = filtered.isNotEmpty()
    }

    fun updateClientNameSuggestions(query: String) {
        if (query.isEmpty()) {
            clientNameSuggestions = emptyList()
            showClientNameSuggestions = false
            return
        }

        val filtered = clients.filter { client ->
            client.name.contains(query, ignoreCase = true) ||
                client.phone.contains(query, ignoreCase = true) ||
                client.storeName.contains(query, ignoreCase = true)
        }.take(5)

        clientNameSuggestions = filtered
        showClientNameSuggestions = filtered.isNotEmpty()
    }

    fun updateInvoiceSuggestions(query: String, allInvoices: List<Invoice>) {
        if (query.isEmpty()) {
            invoiceSuggestions = emptyList()
            showInvoiceSuggestions = false
            return
        }

        val filtered = allInvoices.filter { invoice ->
            invoice.id.contains(query, ignoreCase = true) ||
                invoice.clientName.contains(query, ignoreCase = true)
        }.take(5)

        invoiceSuggestions = filtered
        showInvoiceSuggestions = filtered.isNotEmpty()
    }

    fun selectClient(client: Client) {
        clientIdInput = client.id
        clientName = client.name
        resolvedClientId = client.id
        clientPhone = client.phone
        clientStoreName = client.storeName
        clientGovernorate = client.governorate
        clientNotFound = false
        hideAllSuggestions()
        showSuccess("✓ تم اختيار العميل: ${client.name}")
    }

    fun selectInvoice(invoice: Invoice) {
        invoiceSearchId = invoice.id
        hideAllSuggestions()
        loadInvoiceForEdit(invoice.id, listOf(invoice))
    }

    fun hideAllSuggestions() {
        showClientCodeSuggestions = false
        showClientNameSuggestions = false
        showInvoiceSuggestions = false
    }

    // ── Payment ──────────────────────────────────────────────

    fun selectPaymentType(type: String) {
        paymentType = type
        showPaymentDropdown = false
        if (type != "دفع مجزء") {
            paidAmount = ""
            showPaidAmountError = false
        }
    }

    fun onPaidAmountChange(amount: String) {
        if (amount.all { it.isDigit() || it == '.' } && amount.length <= 10) {
            paidAmount = amount
            showPaidAmountError = false
        }
    }

    fun togglePaymentDropdown() {
        showPaymentDropdown = true
    }

    fun dismissPaymentDropdown() {
        showPaymentDropdown = false
    }

    fun dismissSuggestions() {
        showClientCodeSuggestions = false
        showClientNameSuggestions = false
        showInvoiceSuggestions = false
    }

    // ── Cart ─────────────────────────────────────────────────

    fun incrementCartItem(index: Int) {
        if (index !in cartItems.indices) return
        val item = cartItems[index]
        if (item.quantity + 1 <= item.product.stock) {
            cartItems[index] = item.copy(quantity = item.quantity + 1)
        } else {
            showError("⚠ تم الوصول لأقصى مخزون متاح!")
        }
    }

    fun decrementCartItem(index: Int) {
        if (index !in cartItems.indices) return
        val item = cartItems[index]
        if (item.quantity > 1) {
            cartItems[index] = item.copy(quantity = item.quantity - 1)
        } else {
            cartItems.removeAt(index)
        }
    }

    fun removeCartItem(index: Int) {
        if (index in cartItems.indices) {
            cartItems.removeAt(index)
        }
    }

    fun setCartItemQuantity(index: Int, quantity: Int) {
        if (index !in cartItems.indices) return
        if (quantity <= 0) {
            cartItems.removeAt(index)
            return
        }
        val item = cartItems[index]
        if (quantity <= item.product.stock) {
            cartItems[index] = item.copy(quantity = quantity)
        } else {
            showError("⚠ الكمية تتجاوز المخزون المتاح (${item.product.stock})!")
        }
    }

    fun requestClearCart() {
        showClearCartDialog = true
    }

    fun dismissClearCartDialog() {
        showClearCartDialog = false
    }

    fun confirmClearCart() {
        cartItems.clear()
        showClearCartDialog = false
        showSuccess("✓ تم مسح السلة")
    }

    // ── Restore Last Invoice ─────────────────────────────────

    fun restoreLastInvoice(pastInvoices: List<Invoice>) {
        if (pastInvoices.isEmpty()) {
            showError("✗ لا توجد فواتير سابقة!")
            return
        }
        val lastInvoice = pastInvoices.first()
        loadInvoiceForEdit(lastInvoice.id, pastInvoices)
    }

    // ── Edit / Save ──────────────────────────────────────────

    fun requestCancelEdit() {
        showCancelEditDialog = true
    }

    fun dismissCancelEditDialog() {
        showCancelEditDialog = false
    }

    fun confirmCancelEdit() {
        showCancelEditDialog = false
        cancelEdit()
    }

    fun loadInvoiceForEdit(invoiceId: String, pastInvoices: List<Invoice>) {
        val found = pastInvoices.firstOrNull { it.id.equals(invoiceId.trim(), ignoreCase = true) }
        if (found != null) {
            clientName = found.clientName
            clientIdInput = found.clientId
            resolvedClientId = found.clientId
            paymentType = found.paymentType
            paidAmount = if (found.paidAmount > 0) "%.2f".format(found.paidAmount) else ""
            cartItems.clear()
            cartItems.addAll(found.items)
            editingInvoiceId = found.id
            invoiceSearchId = found.id
            clientNotFound = false

            clients.firstOrNull { it.id == found.clientId }?.let { client ->
                clientPhone = client.phone
                clientStoreName = client.storeName
                clientGovernorate = client.governorate
            } ?: clearClientDetailsOnly()

            showSuccess("✓ تم تحميل الفاتورة للتعديل")
        } else {
            showError("✗ لم يتم العثور على الفاتورة!")
        }
    }

    fun cancelEdit() {
        resetForm()
        showSuccess("✓ تم إلغاء التعديل")
    }

    fun saveInvoice(onSuccess: (Invoice) -> Unit) {
        when {
            clientName.isEmpty() -> showError("✗ الرجاء إدخال اسم العميل أولاً!")
            cartItems.isEmpty() -> showError("✗ السلة فارغة! لا يمكن الحفظ.")
            paymentType == "دفع مجزء" && paidAmount.isBlank() -> {
                showPaidAmountError = true
                showError("✗ الرجاء إدخال المبلغ المدفوع!")
            }
            paymentType == "دفع مجزء" && (paidAmount.toDoubleOrNull() == null || paidAmount.toDouble() <= 0) -> {
                showPaidAmountError = true
                showError("✗ المبلغ المدفوع غير صحيح!")
            }
            paymentType == "دفع مجزء" && paidAmount.toDoubleOrNull() != null && paidAmount.toDouble() > total -> {
                showPaidAmountError = true
                showError("✗ المبلغ المدفوع أكبر من الإجمالي!")
            }
            else -> {
                feedbackMessage = "⏳ جاري حفظ الفاتورة..."
                isSuccess = true
                isLoading = true

                scope.launch {
                    val existingIds = repository.getPastInvoices()
                        .getOrNull()
                        ?.map { it.id }
                        .orEmpty()
                        .toSet()

                    val invId = editingInvoiceId ?: generateUniqueInvoiceId(existingIds)
                    val invoice = buildInvoice(invId)

                    val result = if (editingInvoiceId != null) {
                        repository.update(invoice)
                    } else {
                        repository.save(invoice)
                    }

                    result
                        .onSuccess { savedInvoice ->
                            onSuccess(savedInvoice)
                            resetForm()
                            showSuccess("✓ تم ${if (editingInvoiceId != null) "تحديث" else "حفظ"} الفاتورة بنجاح: $invId")
                        }
                        .onError { error ->
                            showError("✗ ${error.getUserMessage()}")
                        }

                    isLoading = false
                }
            }
        }
    }

    private fun generateUniqueInvoiceId(existingIds: Set<String>): String {
        var id: String
        var attempts = 0
        do {
            id = "INV-${(1000..9999).random()}"
            attempts++
            if (attempts > 100) {
                id = "INV-${(10000..99999).random()}"
                break
            }
        } while (id in existingIds)
        return id
    }

    fun buildPreviewInvoice(): Invoice = buildInvoice(editingInvoiceId ?: "PREVIEW")

    fun printPreview() {
        printInvoice(buildPreviewInvoice())
    }

    fun exportPreviewPdf() {
        exportPdf(buildPreviewInvoice())
    }

    fun exportPdf(invoice: Invoice) {
        scope.launch(Dispatchers.IO) {
            feedbackMessage = "⏳ جاري تصدير PDF..."
            isSuccess = true
            val path = InvoicePrinter.exportToPdf(invoice)
            if (path != null) {
                showSuccess("✓ تم تصدير PDF إلى: $path")
            } else {
                showError("✗ تم إلغاء التصدير.")
            }
        }
    }

    fun printInvoice(invoice: Invoice) {
        scope.launch(Dispatchers.IO) {
            feedbackMessage = "⏳ جاري إرسال الفاتورة للطابعة..."
            isSuccess = true
            val ok = InvoicePrinter.printInvoice(invoice)
            if (ok) {
                showSuccess("✓ تم الإرسال إلى الطابعة بنجاح!")
            } else {
                showError("✗ تم إلغاء الطباعة.")
            }
        }
    }

    // ── Private Helpers ──────────────────────────────────────

    private fun buildInvoice(id: String): Invoice {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val paid = if (paymentType == "دفع مجزء") paidAmount.toDoubleOrNull() ?: 0.0 else 0.0
        return Invoice(
            id = id,
            clientId = resolvedClientId,
            clientName = clientName,
            date = now,
            items = cartItems.toList(),
            totalAmount = total,
            paymentType = paymentType,
            paidAmount = paid
        )
    }

    private fun showSuccess(message: String) {
        feedbackMessage = message
        isSuccess = true
        clearFeedbackAfterDelay()
    }

    private fun showError(message: String) {
        feedbackMessage = message
        isSuccess = false
        clearFeedbackAfterDelay()
    }

    private fun clearClientData() {
        clientName = ""
        resolvedClientId = ""
        clearClientDetailsOnly()
    }

    private fun clearClientDetailsOnly() {
        clientPhone = ""
        clientStoreName = ""
        clientGovernorate = ""
    }

    private fun resetForm() {
        clientName = ""
        clientIdInput = ""
        resolvedClientId = ""
        clientPhone = ""
        clientStoreName = ""
        clientGovernorate = ""
        clientNotFound = false
        invoiceSearchId = ""
        editingInvoiceId = null
        paymentType = "نقدي"
        paidAmount = ""
        showPaidAmountError = false
        cartItems.clear()
        hideAllSuggestions()
    }
}
