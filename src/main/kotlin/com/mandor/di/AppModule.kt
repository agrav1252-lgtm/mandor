package com.mandor.di

import com.mandor.SupabaseManager
import com.mandor.data.repository.InvoiceRepositoryImpl
import com.mandor.domain.repository.InvoiceRepository
import com.mandor.ui.admin.viewmodel.AdminViewModel
import com.mandor.ui.employee.viewmodel.AddClientViewModel
import com.mandor.ui.employee.viewmodel.CreateInvoiceViewModel
import com.mandor.ui.employee.viewmodel.EmployeeViewModel
import com.mandor.ui.employee.viewmodel.ProductInquiryViewModel
import com.mandor.ui.employee.viewmodel.SearchClientsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.koin.dsl.bind

/**
 * Koin Dependency Injection Module
 * 
 * This module defines all dependencies that will be injected throughout the application.
 * Using Koin provides:
 * - Type-safe dependency injection
 * - Easy testing with mock implementations
 * - Clear dependency graph
 * - Lifecycle-aware component management
 */
val appModule = module {
    
    // ── Supabase Manager ──────────────────────────────────
    // Singleton: One instance for database operations
    single { SupabaseManager }
    
    // ── Repository Layer ──────────────────────────────────
    // Singleton: One instance shared across the entire app
    singleOf(::InvoiceRepositoryImpl) bind InvoiceRepository::class
    
    // ── ViewModel Layer ───────────────────────────────────
    // Factory: New instance created each time it's requested
    // ViewModels are typically created per screen/composable
    factoryOf(::EmployeeViewModel)
    factoryOf(::CreateInvoiceViewModel)
    factoryOf(::ProductInquiryViewModel)
    factoryOf(::AdminViewModel)
    factory { AddClientViewModel(get()) }
    factory { SearchClientsViewModel(get()) }
}

/**
 * Alternative explicit syntax (same result as above):
 * 
 * val appModule = module {
 *     // Repository
 *     single<InvoiceRepository> { InvoiceRepositoryImpl() }
 *     
 *     // ViewModels
 *     factory { EmployeeViewModel(get()) }
 *     factory { CreateInvoiceViewModel(get()) }
 *     factory { ProductInquiryViewModel(get()) }
 * }
 */
