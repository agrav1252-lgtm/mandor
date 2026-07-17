package com.mandor

import com.mandor.domain.model.Product
import com.mandor.domain.model.Invoice
import com.mandor.domain.model.Client
import com.mandor.domain.model.InvoiceItem

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.selectAsFlow
import io.github.jan.supabase.annotations.SupabaseExperimental
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ────────────────────────────────────────────────
// Supabase Database Models (Serializable)
// ────────────────────────────────────────────────

@Serializable
data class SupabaseCategory(
    val id: Long,
    val name: String
)

@Serializable
data class SupabaseProduct(
    val code: String,
    val barcode: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val description: String,
    val category: String
) {
    fun toLocalProduct() = Product(code, barcode, name, price, stock, description, category)
}

@Serializable
data class SupabaseClient(
    val id: String,
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    @SerialName("store_name")
    val storeName: String? = null,
    val governorate: String? = null,
    val notes: String? = null
) {
    fun toLocalClient() = Client(
        id = id,
        name = name,
        phone = phone ?: "",
        address = address ?: "",
        storeName = storeName ?: "",
        governorate = governorate ?: "",
        notes = notes ?: ""
    )
}

@Serializable
data class SupabaseInvoice(
    val id: String,
    @SerialName("client_id")
    val clientId: String = "",
    @SerialName("client_name")
    val clientName: String,
    val date: String,
    @SerialName("total_amount")
    val totalAmount: Double,
    @SerialName("payment_type")
    val paymentType: String,
    @SerialName("paid_amount")
    val paidAmount: Double = 0.0
)

@Serializable
data class SupabaseInvoiceItem(
    @SerialName("invoice_id")
    val invoiceId: String,
    @SerialName("product_code")
    val productCode: String,
    val quantity: Int,
    val price: Double
)

// ────────────────────────────────────────────────
// Supabase Manager (Connection & Logic)
// ────────────────────────────────────────────────
object SupabaseManager {
    // ✓ Secure configuration loaded from .env file
    // No more hardcoded credentials in source code!
    val client = createSupabaseClient(
        supabaseUrl = com.mandor.config.EnvConfig.supabaseUrl,
        supabaseKey = com.mandor.config.EnvConfig.supabaseAnonKey
    ) {
        install(Postgrest)
        install(Realtime)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * جلب قائمة المنتجات بالكامل
     */
    suspend fun getProducts(): List<Product> {
        return try {
            client.postgrest["products"]
                .select()
                .decodeList<SupabaseProduct>()
                .map { it.toLocalProduct() }
        } catch (e: Exception) {
            println("Error fetching products: ${e.message}")
            e.printStackTrace()
            throw e // Re-throw to be caught by repository layer
        }
    }

    /**
     * الاستماع للتغييرات اللحظية في المنتجات (Realtime Flow)
     */
    @OptIn(SupabaseExperimental::class)
    fun observeProductsRealtime(): Flow<List<Product>> {
        return client.postgrest["products"].selectAsFlow(
            primaryKey = SupabaseProduct::code
        ).map { list -> list.map { it.toLocalProduct() } }
    }

    /**
     * الاستماع للتغييرات اللحظية في العملاء (Realtime Flow)
     */
    @OptIn(SupabaseExperimental::class)
    fun observeClientsRealtime(): Flow<List<Client>> {
        return client.postgrest["clients"].selectAsFlow(
            primaryKey = SupabaseClient::id
        ).map { list -> list.map { it.toLocalClient() } }
    }

    /**
     * جلب قائمة العملاء
     */
    suspend fun getClients(): List<Client> {
        return try {
            client.postgrest["clients"]
                .select()
                .decodeList<SupabaseClient>()
                .map { it.toLocalClient() }
        } catch (e: Exception) {
            println("Error fetching clients: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    /**
     * حفظ عميل جديد
     */
    suspend fun saveClient(client: Client): Boolean {
        return try {
            val dbClient = SupabaseClient(
                id = client.id,
                name = client.name,
                phone = client.phone.ifEmpty { null },
                address = client.address.ifEmpty { null },
                storeName = client.storeName.ifEmpty { null },
                governorate = client.governorate.ifEmpty { null },
                notes = client.notes.ifEmpty { null }
            )
            this.client.postgrest["clients"].insert(dbClient)
            println("✓ Client ${client.id} saved successfully")
            true
        } catch (e: Exception) {
            println("✗ Error saving client: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun getClientById(id: String): Client? {
        return try {
            client.postgrest["clients"]
                .select {
                    filter { eq("id", id) }
                }
                .decodeSingleOrNull<SupabaseClient>()
                ?.toLocalClient()
        } catch (e: Exception) {
            println("Error fetching client by id: ${e.message}")
            null
        }
    }

    suspend fun updateClient(updatedClient: Client): Boolean {
        return try {
            this.client.postgrest["clients"].update({
                SupabaseClient::name setTo updatedClient.name
                SupabaseClient::phone setTo updatedClient.phone.ifEmpty { null }
                SupabaseClient::address setTo updatedClient.address.ifEmpty { null }
                SupabaseClient::storeName setTo updatedClient.storeName.ifEmpty { null }
                SupabaseClient::governorate setTo updatedClient.governorate.ifEmpty { null }
                SupabaseClient::notes setTo updatedClient.notes.ifEmpty { null }
            }) {
                filter { eq("id", updatedClient.id) }
            }
            println("✓ Client ${updatedClient.id} updated successfully")
            true
        } catch (e: Exception) {
            println("✗ Error updating client: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * حفظ فاتورة جديدة وعناصرها في قاعدة البيانات مع تحديث المخزن
     */
    suspend fun saveInvoice(invoice: Invoice): Boolean {
        return try {
            // 1. حفظ الفاتورة الأساسية
            val dbInvoice = SupabaseInvoice(
                id = invoice.id,
                clientId = invoice.clientId,
                clientName = invoice.clientName,
                date = invoice.date,
                totalAmount = invoice.totalAmount,
                paymentType = invoice.paymentType,
                paidAmount = invoice.paidAmount
            )
            client.postgrest["invoices"].insert(dbInvoice)

            // 2. حفظ عناصر الفاتورة وتحديث مخزون المنتجات
            invoice.items.forEach { item ->
                val dbItem = SupabaseInvoiceItem(
                    invoiceId = invoice.id,
                    productCode = item.product.code,
                    quantity = item.quantity,
                    price = item.product.price
                )
                client.postgrest["invoice_items"].insert(dbItem)

                // تحديث كمية المنتج في المخزن
                val newStock = (item.product.stock - item.quantity).coerceAtLeast(0)
                client.postgrest["products"].update({
                    SupabaseProduct::stock setTo newStock
                }) {
                    filter {
                        SupabaseProduct::code eq item.product.code
                    }
                }
            }
            println("✓ Invoice ${invoice.id} saved successfully")
            true
        } catch (e: Exception) {
            println("✗ Error saving invoice: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun updateInvoice(invoice: Invoice): Boolean {
        return try {
            val dbInvoice = SupabaseInvoice(
                id = invoice.id,
                clientId = invoice.clientId,
                clientName = invoice.clientName,
                date = invoice.date,
                totalAmount = invoice.totalAmount,
                paymentType = invoice.paymentType,
                paidAmount = invoice.paidAmount
            )
            client.postgrest["invoices"].update({
                SupabaseInvoice::clientId setTo invoice.clientId
                SupabaseInvoice::clientName setTo invoice.clientName
                SupabaseInvoice::date setTo invoice.date
                SupabaseInvoice::totalAmount setTo invoice.totalAmount
                SupabaseInvoice::paymentType setTo invoice.paymentType
                SupabaseInvoice::paidAmount setTo invoice.paidAmount
            }) {
                filter { eq("id", invoice.id) }
            }

            // حذف العناصر القديمة وإضافة الجديدة
            client.postgrest["invoice_items"].delete {
                filter { eq("invoice_id", invoice.id) }
            }
            invoice.items.forEach { item ->
                val dbItem = SupabaseInvoiceItem(
                    invoiceId = invoice.id,
                    productCode = item.product.code,
                    quantity = item.quantity,
                    price = item.product.price
                )
                client.postgrest["invoice_items"].insert(dbItem)
            }

            println("✓ Invoice ${invoice.id} updated successfully")
            true
        } catch (e: Exception) {
            println("✗ Error updating invoice: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * جلب سجل الفواتير السابقة مع عناصرها
     */
    suspend fun getPastInvoices(): List<Invoice> {
        return try {
            val dbInvoices = client.postgrest["invoices"]
                .select()
                .decodeList<SupabaseInvoice>()

            dbInvoices.map { dbInv ->
                // جلب عناصر هذه الفاتورة
                val dbItems = client.postgrest["invoice_items"]
                    .select {
                        filter {
                            eq("invoice_id", dbInv.id)
                        }
                    }
                    .decodeList<SupabaseInvoiceItem>()

                // جلب المنتجات المحددة
                val items = dbItems.mapNotNull { dbItem ->
                    val prod = client.postgrest["products"]
                        .select {
                            filter {
                                eq("code", dbItem.productCode)
                            }
                        }
                        .decodeSingleOrNull<SupabaseProduct>()?.toLocalProduct()
                    
                    if (prod != null) InvoiceItem(prod, dbItem.quantity) else null
                }

                Invoice(
                    id = dbInv.id,
                    clientId = dbInv.clientId,
                    clientName = dbInv.clientName,
                    date = dbInv.date,
                    items = items,
                    totalAmount = dbInv.totalAmount,
                    paymentType = dbInv.paymentType,
                    paidAmount = dbInv.paidAmount
                )
            }
        } catch (e: Exception) {
            println("Error fetching past invoices: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    /**
     * التحقق من بيانات دخول المدير
     */
    suspend fun verifyAdmin(name: String, pass: String): Boolean {
        return try {
            println("Attempting login for admin: $name")
            val result = client.postgrest["admins"]
                .select {
                    filter {
                        eq("name", name)
                        eq("password", pass)
                    }
                }
                .decodeSingleOrNull<SupabaseAdmin>()
            
            if (result == null) {
                println("Login failed: No admin found with provided credentials")
            } else {
                println("Login successful for admin: ${result.name}")
            }
            result != null
        } catch (e: Exception) {
            println("Login error: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * التحقق من بيانات دخول الموظف
     */
    suspend fun verifyEmployee(name: String, pass: String): Boolean {
        return try {
            println("Attempting login for employee: $name")
            val result = client.postgrest["emps"]
                .select {
                    filter {
                        eq("name", name)
                        eq("password", pass)
                    }
                }
                .decodeSingleOrNull<SupabaseEmployee>()
            
            if (result == null) {
                println("Login failed: No employee found with provided credentials")
            } else {
                println("Login successful for employee: ${result.name}")
            }
            result != null
        } catch (e: Exception) {
            println("Login error: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    // ── Admin: Category CRUD ────────────────────────────

    suspend fun getCategories(): List<SupabaseCategory> {
        return try {
            client.postgrest["categories"]
                .select()
                .decodeList<SupabaseCategory>()
        } catch (e: Exception) {
            println("Error fetching categories: ${e.message}")
            throw e
        }
    }

    @OptIn(SupabaseExperimental::class)
    fun observeCategoriesRealtime(): Flow<List<SupabaseCategory>> {
        return client.postgrest["categories"].selectAsFlow(
            primaryKey = SupabaseCategory::id
        ).map { it }
    }

    suspend fun saveCategory(name: String): Boolean {
        return try {
            client.postgrest["categories"].insert(mapOf("name" to name))
            println("✓ Category $name saved")
            true
        } catch (e: Exception) {
            println("✗ Error saving category: ${e.message}")
            false
        }
    }

    suspend fun deleteCategory(id: Long): Boolean {
        return try {
            client.postgrest["categories"].delete {
                filter { eq("id", id) }
            }
            println("✓ Category $id deleted")
            true
        } catch (e: Exception) {
            println("✗ Error deleting category: ${e.message}")
            false
        }
    }

    suspend fun renameCategory(id: Long, newName: String): Boolean {
        return try {
            client.postgrest["categories"].update({
                SupabaseCategory::name setTo newName
            }) {
                filter { eq("id", id) }
            }
            println("✓ Category $id renamed to $newName")
            true
        } catch (e: Exception) {
            println("✗ Error renaming category: ${e.message}")
            false
        }
    }

    // ── Admin: Product CRUD ───────────────────────────────

    suspend fun saveProduct(product: Product): Boolean {
        return try {
            val dbProduct = SupabaseProduct(
                code = product.code,
                barcode = product.barcode,
                name = product.name,
                price = product.price,
                stock = product.stock,
                description = product.description,
                category = product.category
            )
            client.postgrest["products"].insert(dbProduct)
            println("✓ Product ${product.code} saved")
            true
        } catch (e: Exception) {
            println("✗ Error saving product: ${e.message}")
            false
        }
    }

    suspend fun updateProduct(product: Product): Boolean {
        return try {
            client.postgrest["products"].update({
                SupabaseProduct::name setTo product.name
                SupabaseProduct::barcode setTo product.barcode
                SupabaseProduct::price setTo product.price
                SupabaseProduct::stock setTo product.stock
                SupabaseProduct::description setTo product.description
                SupabaseProduct::category setTo product.category
            }) {
                filter { eq("code", product.code) }
            }
            println("✓ Product ${product.code} updated")
            true
        } catch (e: Exception) {
            println("✗ Error updating product: ${e.message}")
            false
        }
    }

    suspend fun deleteProduct(code: String): Boolean {
        return try {
            client.postgrest["products"].delete {
                filter { eq("code", code) }
            }
            println("✓ Product $code deleted")
            true
        } catch (e: Exception) {
            println("✗ Error deleting product: ${e.message}")
            false
        }
    }

    // ── Admin: Employee CRUD ──────────────────────────────

    suspend fun getEmployees(): List<SupabaseEmployee> {
        return try {
            client.postgrest["emps"]
                .select()
                .decodeList<SupabaseEmployee>()
        } catch (e: Exception) {
            println("Error fetching employees: ${e.message}")
            throw e
        }
    }

    suspend fun saveEmployee(name: String, password: String): Boolean {
        return try {
            client.postgrest["emps"].insert(
                mapOf("name" to name, "password" to password)
            )
            println("✓ Employee $name saved")
            true
        } catch (e: Exception) {
            println("✗ Error saving employee: ${e.message}")
            false
        }
    }

    suspend fun deleteEmployee(id: Long): Boolean {
        return try {
            client.postgrest["emps"].delete {
                filter { eq("id", id) }
            }
            println("✓ Employee $id deleted")
            true
        } catch (e: Exception) {
            println("✗ Error deleting employee: ${e.message}")
            false
        }
    }
}

@Serializable
data class SupabaseAdmin(
    val id: Long,
    val name: String,
    val password: String
)

@Serializable
data class SupabaseEmployee(
    val id: Long,
    val name: String,
    val password: String
)
