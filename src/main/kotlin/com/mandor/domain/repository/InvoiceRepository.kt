package com.mandor.domain.repository

import com.mandor.domain.model.Invoice
import com.mandor.domain.model.Product
import com.mandor.domain.model.Client
import com.mandor.domain.model.Result

interface InvoiceRepository {
    suspend fun save(invoice: Invoice): Result<Invoice>
    suspend fun update(invoice: Invoice): Result<Invoice>
    suspend fun print(invoice: Invoice): Result<Boolean>
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getClients(): Result<List<Client>>
    suspend fun getPastInvoices(): Result<List<Invoice>>
}
