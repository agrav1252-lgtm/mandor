package com.mandor.ui.employee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mandor.domain.model.Product
import com.mandor.domain.repository.InvoiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for Product Inquiry screen
 * Manages product list state and loading/error states
 */
class ProductInquiryViewModel(private val repository: InvoiceRepository) {
    private val scope = CoroutineScope(Dispatchers.Default)
    
    var products by mutableStateOf<List<Product>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf("")
        private set
    
    init {
        loadProducts()
    }
    
    fun loadProducts() {
        scope.launch {
            isLoading = true
            errorMessage = ""
            
            repository.getProducts()
                .onSuccess { productList ->
                    products = productList
                }
                .onError { error ->
                    errorMessage = error.getUserMessage()
                }
            
            isLoading = false
        }
    }
    
    fun retry() {
        loadProducts()
    }
}
