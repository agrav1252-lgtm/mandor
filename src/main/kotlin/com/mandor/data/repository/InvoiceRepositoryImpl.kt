package com.mandor.data.repository

import com.mandor.SupabaseManager
import com.mandor.domain.model.Invoice
import com.mandor.domain.model.Product
import com.mandor.domain.model.Client
import com.mandor.domain.model.Result
import com.mandor.domain.model.AppException
import com.mandor.domain.model.safeApiCall
import com.mandor.domain.repository.InvoiceRepository

class InvoiceRepositoryImpl : InvoiceRepository {
    override suspend fun save(invoice: Invoice): Result<Invoice> = safeApiCall(
        errorMessage = "فشل حفظ الفاتورة"
    ) {
        val success = SupabaseManager.saveInvoice(invoice)
        if (success) {
            invoice
        } else {
            throw AppException.DatabaseError("فشل حفظ الفاتورة في قاعدة البيانات")
        }
    }

    override suspend fun update(invoice: Invoice): Result<Invoice> = safeApiCall(
        errorMessage = "فشل تحديث الفاتورة"
    ) {
        val success = SupabaseManager.updateInvoice(invoice)
        if (success) {
            invoice
        } else {
            throw AppException.DatabaseError("فشل تحديث الفاتورة في قاعدة البيانات")
        }
    }

    override suspend fun print(invoice: Invoice): Result<Boolean> {
        return try {
            println("Printing invoice: ${invoice.id}")
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(AppException.UnknownError("فشلت عملية الطباعة", e))
        }
    }

    override suspend fun getProducts(): Result<List<Product>> = safeApiCall(
        errorMessage = "فشل جلب قائمة المنتجات"
    ) {
        SupabaseManager.getProducts()
    }

    override suspend fun getClients(): Result<List<Client>> = safeApiCall(
        errorMessage = "فشل جلب قائمة العملاء"
    ) {
        SupabaseManager.getClients()
    }

    override suspend fun getPastInvoices(): Result<List<Invoice>> = safeApiCall(
        errorMessage = "فشل جلب سجل الفواتير"
    ) {
        SupabaseManager.getPastInvoices()
    }
}
