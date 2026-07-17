package com.mandor.ui.employee.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mandor.SupabaseManager
import com.mandor.domain.model.Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * ViewModel لإدارة حالة إضافة عميل جديد
 */
class AddClientViewModel(
    private val supabaseManager: SupabaseManager
) {
    // ── حقول النموذج ──────────────────────────────────
    var clientId by mutableStateOf("")
        private set
    var clientName by mutableStateOf("")
        private set
    var clientPhone by mutableStateOf("")
        private set
    var storeName by mutableStateOf("")
        private set
    var governorate by mutableStateOf("")
        private set
    var address by mutableStateOf("")
        private set
    var notes by mutableStateOf("")
        private set

    // ── حالات UI ──────────────────────────────────────
    var isLoading by mutableStateOf(false)
        private set
    var feedbackMessage by mutableStateOf("")
        private set
    var isSuccess by mutableStateOf(false)
        private set
    var editingClientId by mutableStateOf<String?>(null)
        private set
    val isEditing: Boolean get() = editingClientId != null

    // ── الاقتراحات ────────────────────────────────────
    var clientCodeSuggestions by mutableStateOf<List<Client>>(emptyList())
        private set
    var showClientCodeSuggestions by mutableStateOf(false)
        private set
    var clientNameSuggestions by mutableStateOf<List<Client>>(emptyList())
        private set
    var showClientNameSuggestions by mutableStateOf(false)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    init {
        scope.launch {
            try {
                _clients.value = supabaseManager.getClients()
            } catch (_: Exception) { }
        }
        scope.launch {
            try {
                supabaseManager.observeClientsRealtime().collect { updated ->
                    _clients.value = updated
                }
            } catch (_: Exception) { }
        }
    }

    // ── تحديث الحقول ──────────────────────────────────
    fun updateClientId(value: String) { 
        // السماح بـ 6 أرقام فقط
        if (value.all { it.isDigit() } && value.length <= 6) {
            clientId = value
            if (editingClientId == null) {
                updateClientCodeSuggestions(value)
            }
        }
    }

    // ── اقتراحات كود العميل ───────────────────────────
    fun updateClientCodeSuggestions(query: String) {
        if (query.isEmpty()) {
            clientCodeSuggestions = emptyList()
            showClientCodeSuggestions = false
            return
        }
        val filtered = _clients.value.filter { it.id.contains(query) }.take(5)
        clientCodeSuggestions = filtered
        showClientCodeSuggestions = filtered.isNotEmpty()
    }

    // ── اقتراحات اسم العميل ───────────────────────────
    fun updateClientNameSuggestions(query: String) {
        if (query.isEmpty()) {
            clientNameSuggestions = emptyList()
            showClientNameSuggestions = false
            return
        }
        val filtered = _clients.value.filter { client ->
            client.name.contains(query, ignoreCase = true) ||
                client.phone.contains(query, ignoreCase = true) ||
                client.storeName.contains(query, ignoreCase = true)
        }.take(5)
        clientNameSuggestions = filtered
        showClientNameSuggestions = filtered.isNotEmpty()
    }

    fun selectClient(client: Client) {
        clientId = client.id
        clientName = client.name
        clientPhone = client.phone
        storeName = client.storeName
        governorate = client.governorate
        address = client.address
        notes = client.notes
        clientCodeSuggestions = emptyList()
        showClientCodeSuggestions = false
        clientNameSuggestions = emptyList()
        showClientNameSuggestions = false
        editingClientId = client.id
        isSuccess = true
        feedbackMessage = "✓ تم تحميل بيانات العميل: ${client.name}"
    }

    fun dismissSuggestions() {
        showClientCodeSuggestions = false
        showClientNameSuggestions = false
    }

    // ── البحث عن عميل عند الضغط على Enter ────────────
    fun lookupClient() {
        dismissSuggestions()
        val id = clientId.trim()
        if (id.length != 6 || !id.all { it.isDigit() }) {
            feedbackMessage = "⚠️ رقم العميل يجب أن يكون 6 أرقام"
            isSuccess = false
            return
        }

        val cached = _clients.value.firstOrNull { it.id == id }
        if (cached != null) {
            selectClient(cached)
            return
        }

        isLoading = true
        scope.launch {
            try {
                val client = supabaseManager.getClientById(id)
                withContext(Dispatchers.Default) {
                    if (client != null) {
                        selectClient(client)
                    } else {
                        editingClientId = null
                        isSuccess = false
                        feedbackMessage = "✗ لم يتم العثور على عميل بهذا الرقم"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    isSuccess = false
                    feedbackMessage = "✗ خطأ في البحث: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Default) {
                    isLoading = false
                }
            }
        }
    }
    fun updateClientName(value: String) {
        clientName = value
        if (editingClientId == null) {
            updateClientNameSuggestions(value)
        }
    }
    fun updateClientPhone(value: String) { clientPhone = value }
    fun updateStoreName(value: String) { storeName = value }
    fun updateGovernorate(value: String) { governorate = value }
    fun updateAddress(value: String) { address = value }
    fun updateNotes(value: String) { notes = value }

    /**
     * توليد ID تلقائي (6 أرقام) والتأكد من عدم وجوده
     */
    fun generateAutoId() {
        isLoading = true
        scope.launch {
            try {
                var newId: String
                var isUnique = false
                var attempts = 0
                val maxAttempts = 10

                // محاولة توليد ID فريد
                while (!isUnique && attempts < maxAttempts) {
                    // توليد 6 أرقام عشوائية
                    newId = (100000..999999).random().toString()
                    
                    // التحقق من أن الكود غير موجود
                    isUnique = _clients.value.none { it.id == newId }
                    
                    if (isUnique) {
                        withContext(Dispatchers.Default) {
                            clientId = newId
                            feedbackMessage = "✓ تم توليد الكود: $newId"
                            isSuccess = true
                        }
                        kotlinx.coroutines.delay(2000)
                        withContext(Dispatchers.Default) {
                            feedbackMessage = ""
                        }
                        break
                    }
                    attempts++
                }

                if (!isUnique) {
                    withContext(Dispatchers.Default) {
                        feedbackMessage = "⚠️ فشل توليد كود فريد، حاول مرة أخرى"
                        isSuccess = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    feedbackMessage = "✗ خطأ في التوليد: ${e.message}"
                    isSuccess = false
                }
            } finally {
                withContext(Dispatchers.Default) {
                    isLoading = false
                }
            }
        }
    }

    /**
     * مسح جميع الحقول
     */
    fun clearForm() {
        clientId = ""
        clientName = ""
        clientPhone = ""
        storeName = ""
        governorate = ""
        address = ""
        notes = ""
        editingClientId = null
        clientCodeSuggestions = emptyList()
        showClientCodeSuggestions = false
        clientNameSuggestions = emptyList()
        showClientNameSuggestions = false
        feedbackMessage = ""
        isSuccess = false
    }

    /**
     * حفظ العميل الجديد أو تحديثه
     */
    fun saveClient(onSuccess: () -> Unit = {}) {
        // Validation
        if (clientId.isBlank()) {
            feedbackMessage = "⚠️ يرجى إدخال رقم العميل أو توليده تلقائياً"
            isSuccess = false
            return
        }
        if (clientId.length != 6 || !clientId.all { it.isDigit() }) {
            feedbackMessage = "⚠️ رقم العميل يجب أن يكون 6 أرقام فقط"
            isSuccess = false
            return
        }
        if (clientName.isBlank()) {
            feedbackMessage = "⚠️ يرجى إدخال اسم العميل"
            isSuccess = false
            return
        }

        isLoading = true
        feedbackMessage = ""

        scope.launch {
            try {
                val client = Client(
                    id = clientId.trim(),
                    name = clientName.trim(),
                    phone = clientPhone.trim(),
                    address = address.trim(),
                    storeName = storeName.trim(),
                    governorate = governorate.trim(),
                    notes = notes.trim()
                )

                val success = if (isEditing) {
                    supabaseManager.updateClient(client)
                } else {
                    // التحقق من عدم وجود الكود مسبقاً للإضافة الجديدة فقط
                    if (_clients.value.any { it.id == clientId.trim() }) {
                        withContext(Dispatchers.Default) {
                            isSuccess = false
                            feedbackMessage = "⚠️ رقم العميل موجود مسبقاً، اختر رقم آخر"
                            isLoading = false
                        }
                        return@launch
                    }
                    supabaseManager.saveClient(client)
                }

                if (success) {
                    _clients.value = if (isEditing) {
                        _clients.value.map { if (it.id == client.id) client else it }
                    } else {
                        _clients.value + client
                    }
                }

                withContext(Dispatchers.Default) {
                    if (success) {
                        isSuccess = true
                        feedbackMessage = "✓ تم ${if (isEditing) "تحديث" else "حفظ"} العميل بنجاح"
                        onSuccess()
                        
                        kotlinx.coroutines.delay(2000)
                        clearForm()
                    } else {
                        isSuccess = false
                        feedbackMessage = "✗ فشل ${if (isEditing) "تحديث" else "حفظ"} العميل - يرجى المحاولة مرة أخرى"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Default) {
                    isSuccess = false
                    feedbackMessage = "✗ خطأ: ${e.message ?: "حدث خطأ غير متوقع"}"
                }
            } finally {
                withContext(Dispatchers.Default) {
                    isLoading = false
                }
            }
        }
    }

    /**
     * إخفاء رسالة الـ Feedback
     */
    fun clearFeedback() {
        feedbackMessage = ""
    }
}
