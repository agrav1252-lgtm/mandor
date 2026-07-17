package com.mandor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductCategory(
    val id: Long,
    val name: String
)

@Serializable
data class Product(
    val code: String,
    val barcode: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val description: String = "",
    val category: String = ""
)

@Serializable
data class InvoiceItem(
    val product: Product,
    var quantity: Int
) {
    val totalPrice: Double get() = product.price * quantity
}

@Serializable
data class Client(
    val id: String,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val storeName: String = "",      // اسم المكتبة/المكان
    val governorate: String = "",     // المحافظة
    val notes: String = ""            // تفاصيل إضافية
)

@Serializable
data class Invoice(
    val id: String,
    val clientId: String = "",
    val clientName: String,
    val date: String,
    val items: List<InvoiceItem>,
    val totalAmount: Double,
    val paymentType: String = "نقدي",
    val paidAmount: Double = 0.0
) {
    val remainingAmount: Double get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}
