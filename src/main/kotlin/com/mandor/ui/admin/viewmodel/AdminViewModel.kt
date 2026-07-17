package com.mandor.ui.admin.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mandor.SupabaseCategory
import com.mandor.SupabaseEmployee
import com.mandor.SupabaseManager
import com.mandor.domain.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CategoryWithTypes(
    val category: SupabaseCategory,
    val products: List<Product>
)

class AdminViewModel {
    private val scope = CoroutineScope(Dispatchers.IO)

    var activeTab by mutableStateOf(0) // 0 = المخزن, 1 = الموظفين

    val categoriesWithTypes: StateFlow<List<CategoryWithTypes>> =
        combine(
            SupabaseManager.observeCategoriesRealtime(),
            SupabaseManager.observeProductsRealtime()
        ) { cats, prods ->
            cats.map { cat ->
                CategoryWithTypes(
                    category = cat,
                    products = prods.filter { it.category == cat.name }
                )
            }
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    var employees by mutableStateOf<List<SupabaseEmployee>>(emptyList())
        private set

    var feedbackMessage by mutableStateOf("")
        private set
    var isSuccess by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set

    // ── Category Form ──────────────────────────────
    var newCategoryName by mutableStateOf("")
        private set
    var editingCategoryId by mutableStateOf<Long?>(null)
        private set
    var editingCategoryName by mutableStateOf("")
        private set

    // ── Product (Type) Form ────────────────────────
    var activeCategoryId by mutableStateOf<Long?>(null)
        private set
    var activeCategoryName by mutableStateOf("")
        private set
    var productName by mutableStateOf("")
        private set
    var productPrice by mutableStateOf("")
        private set
    var productStock by mutableStateOf("")
        private set
    var productCode by mutableStateOf("")
        private set
    var productBarcode by mutableStateOf("")
        private set
    var editingProductCode by mutableStateOf<String?>(null)
        private set
    val isEditingProduct: Boolean get() = editingProductCode != null

    // ── Employee Form ──────────────────────────────
    var employeeName by mutableStateOf("")
        private set
    var employeePassword by mutableStateOf("")
        private set

    init {
        loadEmployees()
    }

    private fun loadEmployees() {
        scope.launch {
            try {
                employees = SupabaseManager.getEmployees()
            } catch (_: Exception) { }
        }
    }

    // ── Category Actions ───────────────────────────

    fun updateNewCategoryName(v: String) { newCategoryName = v }
    fun updateEditingCategoryName(v: String) { editingCategoryName = v }

    fun startRenameCategory(cat: SupabaseCategory) {
        editingCategoryId = cat.id
        editingCategoryName = cat.name
    }

    fun cancelRenameCategory() {
        editingCategoryId = null
        editingCategoryName = ""
    }

    fun saveCategory() {
        val name = newCategoryName.trim()
        if (name.isEmpty()) {
            feedbackMessage = "⚠️ اسم الصنف مطلوب"
            isSuccess = false
            return
        }
        isLoading = true
        scope.launch {
            try {
                val ok = SupabaseManager.saveCategory(name)
                withContext(Dispatchers.Default) {
                    if (ok) {
                        newCategoryName = ""
                        isSuccess = true
                        feedbackMessage = "✓ تم إضافة الصنف"
                    } else {
                        isSuccess = false
                        feedbackMessage = "✗ فشل إضافة الصنف"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    isSuccess = false
                    feedbackMessage = "✗ ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Default) { isLoading = false }
            }
        }
    }

    fun renameCategory(id: Long) {
        val name = editingCategoryName.trim()
        if (name.isEmpty()) {
            feedbackMessage = "⚠️ اسم الصنف مطلوب"
            isSuccess = false
            return
        }
        isLoading = true
        scope.launch {
            try {
                SupabaseManager.renameCategory(id, name)
                val allProducts = mutableListOf<Product>()
                categoriesWithTypes.value.forEach { allProducts.addAll(it.products) }
                withContext(Dispatchers.Default) {
                    editingCategoryId = null
                    isSuccess = true
                    feedbackMessage = "✓ تم تعديل الاسم"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    isSuccess = false
                    feedbackMessage = "✗ ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Default) { isLoading = false }
            }
        }
    }

    fun deleteCategory(id: Long) {
        scope.launch {
            try {
                val ok = SupabaseManager.deleteCategory(id)
                withContext(Dispatchers.Default) {
                    if (ok) {
                        isSuccess = true
                        feedbackMessage = "✓ تم حذف الصنف"
                    } else {
                        isSuccess = false
                        feedbackMessage = "✗ فشل حذف الصنف"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    isSuccess = false
                    feedbackMessage = "✗ ${e.message}"
                }
            }
        }
    }

    // ── Product (Type) Actions ─────────────────────

    fun setActiveCategory(categoryId: Long?, categoryName: String) {
        activeCategoryId = categoryId
        activeCategoryName = categoryName
        if (categoryId != null) {
            productCode = ""
            productName = ""
            productPrice = ""
            productStock = ""
            productBarcode = ""
            editingProductCode = null
        }
    }

    fun updateProductName(v: String) { productName = v }
    fun updateProductPrice(v: String) {
        if (v.all { it.isDigit() || it == '.' } && v.count { it == '.' } <= 1) productPrice = v
    }
    fun updateProductStock(v: String) {
        if (v.all { it.isDigit() }) productStock = v
    }
    fun updateProductCode(v: String) { productCode = v }
    fun updateProductBarcode(v: String) { productBarcode = v }

    fun loadProductForEdit(product: Product, categoryId: Long, categoryName: String) {
        productCode = product.code
        productName = product.name
        productPrice = product.price.toString()
        productStock = product.stock.toString()
        productBarcode = product.barcode
        editingProductCode = product.code
        activeCategoryId = categoryId
        activeCategoryName = categoryName
    }

    fun saveProduct() {
        if (productName.isBlank() || productPrice.isBlank()) {
            feedbackMessage = "⚠️ الاسم والسعر إجباريان"
            isSuccess = false
            return
        }
        if (!isEditingProduct && productCode.isBlank() && productBarcode.isBlank()) {
            feedbackMessage = "⚠️ يجب إدخال كود أو باركود"
            isSuccess = false
            return
        }

        isLoading = true
        scope.launch {
            try {
                val finalCode = editingProductCode ?: productCode.ifBlank { productBarcode.trim() }
                val product = Product(
                    code = finalCode,
                    barcode = productBarcode.trim(),
                    name = productName.trim(),
                    price = productPrice.toDoubleOrNull() ?: 0.0,
                    stock = productStock.toIntOrNull() ?: 0,
                    description = "",
                    category = activeCategoryName
                )

                val success = if (isEditingProduct) {
                    SupabaseManager.updateProduct(product)
                } else {
                    SupabaseManager.saveProduct(product)
                }

                withContext(Dispatchers.Default) {
                    if (success) {
                        isSuccess = true
                        feedbackMessage = "✓ تم ${if (isEditingProduct) "تحديث" else "إضافة"} النوع"
                        productCode = ""
                        productName = ""
                        productPrice = ""
                        productStock = ""
                        productBarcode = ""
                        editingProductCode = null
                    } else {
                        isSuccess = false
                        feedbackMessage = "✗ فشل الحفظ"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    isSuccess = false
                    feedbackMessage = "✗ ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Default) { isLoading = false }
            }
        }
    }

    fun deleteProduct(code: String) {
        scope.launch {
            try {
                val success = SupabaseManager.deleteProduct(code)
                withContext(Dispatchers.Default) {
                    if (success) {
                        isSuccess = true
                        feedbackMessage = "✓ تم حذف النوع"
                    } else {
                        isSuccess = false
                        feedbackMessage = "✗ فشل الحذف"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    isSuccess = false
                    feedbackMessage = "✗ ${e.message}"
                }
            }
        }
    }

    // ── Employee Actions ───────────────────────────

    fun updateEmployeeName(v: String) { employeeName = v }
    fun updateEmployeePassword(v: String) { employeePassword = v }

    fun saveEmployee() {
        if (employeeName.isBlank() || employeePassword.isBlank()) {
            feedbackMessage = "⚠️ الاسم وكلمة السر إجباريان"
            isSuccess = false
            return
        }
        isLoading = true
        scope.launch {
            try {
                val success = SupabaseManager.saveEmployee(employeeName.trim(), employeePassword.trim())
                withContext(Dispatchers.Default) {
                    if (success) {
                        employeeName = ""
                        employeePassword = ""
                        loadEmployees()
                        isSuccess = true
                        feedbackMessage = "✓ تم إضافة الموظف"
                    } else {
                        isSuccess = false
                        feedbackMessage = "✗ فشل الإضافة"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    isSuccess = false
                    feedbackMessage = "✗ ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Default) { isLoading = false }
            }
        }
    }

    fun deleteEmployee(id: Long) {
        scope.launch {
            try {
                val success = SupabaseManager.deleteEmployee(id)
                withContext(Dispatchers.Default) {
                    if (success) {
                        loadEmployees()
                        isSuccess = true
                        feedbackMessage = "✓ تم حذف الموظف"
                    } else {
                        isSuccess = false
                        feedbackMessage = "✗ فشل الحذف"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    isSuccess = false
                    feedbackMessage = "✗ ${e.message}"
                }
            }
        }
    }

    fun clearFeedback() { feedbackMessage = "" }
}
