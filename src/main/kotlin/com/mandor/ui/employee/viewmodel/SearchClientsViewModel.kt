package com.mandor.ui.employee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mandor.SupabaseManager
import com.mandor.domain.model.Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel للبحث عن العملاء
 */
class SearchClientsViewModel(
    private val supabaseManager: SupabaseManager
) {
    var searchQuery by mutableStateOf("")
        private set
    
    var allClients by mutableStateOf<List<Client>>(emptyList())
        private set
    
    var filteredClients by mutableStateOf<List<Client>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * تحديث كلمة البحث وتصفية النتائج
     */
    fun updateSearchQuery(query: String) {
        searchQuery = query
        filterClients()
    }

    /**
     * تحميل جميع العملاء
     */
    fun loadClients() {
        isLoading = true
        scope.launch {
            try {
                val clients = supabaseManager.getClients()
                withContext(Dispatchers.Default) {
                    allClients = clients
                    filterClients()
                }
            } catch (e: Exception) {
                println("Error loading clients: ${e.message}")
                withContext(Dispatchers.Default) {
                    allClients = emptyList()
                    filteredClients = emptyList()
                }
            } finally {
                withContext(Dispatchers.Default) {
                    isLoading = false
                }
            }
        }
    }

    /**
     * تصفية العملاء بناءً على كلمة البحث
     */
    private fun filterClients() {
        filteredClients = if (searchQuery.isEmpty()) {
            allClients
        } else {
            allClients.filter { client ->
                client.id.contains(searchQuery, ignoreCase = true) ||
                client.name.contains(searchQuery, ignoreCase = true) ||
                client.phone.contains(searchQuery, ignoreCase = true) ||
                client.storeName.contains(searchQuery, ignoreCase = true) ||
                client.governorate.contains(searchQuery, ignoreCase = true) ||
                client.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }
}
