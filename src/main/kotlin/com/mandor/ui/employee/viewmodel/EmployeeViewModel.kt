package com.mandor.ui.employee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mandor.SupabaseManager
import com.mandor.domain.model.Client
import com.mandor.domain.model.Invoice
import com.mandor.domain.model.Product
import com.mandor.domain.repository.InvoiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the overall Employee Dashboard.
 * Manages the active tab, past invoices list, clients list, and real-time product updates.
 */
class EmployeeViewModel(private val repository: InvoiceRepository) {
    private val scope = CoroutineScope(Dispatchers.Default)

    var activeTab by mutableStateOf(2) // 2 is CreateInvoiceTab by default
    val pastInvoices = mutableStateListOf<Invoice>()
    
    // Real-time products flow using Supabase manager
    val productsFlow: StateFlow<List<Product>> = SupabaseManager.observeProductsRealtime()
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    var clients by mutableStateOf<List<Client>>(emptyList())
        private set
    
    var errorMessage by mutableStateOf("")
        private set

    init {
        loadData()
    }

    fun loadData() {
        scope.launch {
            // Load clients
            repository.getClients()
                .onSuccess { clientList ->
                    clients = clientList
                }
                .onError { error ->
                    errorMessage = error.getUserMessage()
                    println("Error loading clients: ${error.message}")
                }
            
            // Load past invoices
            repository.getPastInvoices()
                .onSuccess { invoices ->
                    pastInvoices.clear()
                    pastInvoices.addAll(invoices)
                }
                .onError { error ->
                    errorMessage = error.getUserMessage()
                    println("Error loading invoices: ${error.message}")
                }
        }
    }
    
    fun addInvoiceToList(newInv: Invoice) {
        val idx = pastInvoices.indexOfFirst { it.id == newInv.id }
        if (idx != -1) {
            pastInvoices[idx] = newInv
        } else {
            pastInvoices.add(0, newInv)
        }
    }
}
