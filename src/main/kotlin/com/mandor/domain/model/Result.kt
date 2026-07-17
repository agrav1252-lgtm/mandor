package com.mandor.domain.model

/**
 * A generic wrapper for operation results that can either succeed or fail.
 * This provides type-safe error handling throughout the application.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: AppException) : Result<Nothing>()
    
    /**
     * Check if the result is successful
     */
    fun isSuccess(): Boolean = this is Success
    
    /**
     * Check if the result is an error
     */
    fun isError(): Boolean = this is Error
    
    /**
     * Get data if successful, or null if error
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    /**
     * Get data if successful, or throw exception if error
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
    }
    
    /**
     * Transform the success value
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(exception)
    }
    
    /**
     * Execute action if successful
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Execute action if error
     */
    inline fun onError(action: (AppException) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }
}

/**
 * Custom exception types for better error handling and user feedback
 */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    /**
     * Network-related errors (no internet, timeout, etc.)
     */
    data class NetworkError(
        val errorMessage: String = "فشل الاتصال بالخادم. تحقق من اتصال الإنترنت.",
        override val cause: Throwable? = null
    ) : AppException(errorMessage, cause)
    
    /**
     * Database operation errors
     */
    data class DatabaseError(
        val errorMessage: String = "حدث خطأ في قاعدة البيانات. حاول مرة أخرى.",
        override val cause: Throwable? = null
    ) : AppException(errorMessage, cause)
    
    /**
     * Authentication errors
     */
    data class AuthenticationError(
        val errorMessage: String = "فشل تسجيل الدخول. تحقق من اسم المستخدم وكلمة المرور.",
        override val cause: Throwable? = null
    ) : AppException(errorMessage, cause)
    
    /**
     * Validation errors (invalid input, business rules violation)
     */
    data class ValidationError(
        val errorMessage: String = "البيانات المدخلة غير صالحة.",
        override val cause: Throwable? = null
    ) : AppException(errorMessage, cause)
    
    /**
     * Not found errors (product, invoice, client not found)
     */
    data class NotFoundError(
        val entityName: String,
        val errorMessage: String = "لم يتم العثور على $entityName.",
        override val cause: Throwable? = null
    ) : AppException(errorMessage, cause)
    
    /**
     * Insufficient stock error
     */
    data class InsufficientStockError(
        val productName: String,
        val requestedQty: Int,
        val availableStock: Int,
        val errorMessage: String = "المخزون غير كافٍ. متوفر: $availableStock، مطلوب: $requestedQty"
    ) : AppException(errorMessage)
    
    /**
     * Generic unknown errors
     */
    data class UnknownError(
        val errorMessage: String = "حدث خطأ غير متوقع. حاول مرة أخرى لاحقاً.",
        override val cause: Throwable? = null
    ) : AppException(errorMessage, cause)
    
    /**
     * Configuration errors (missing .env, invalid config)
     */
    data class ConfigurationError(
        val errorMessage: String = "خطأ في إعدادات التطبيق. تحقق من ملف .env",
        override val cause: Throwable? = null
    ) : AppException(errorMessage, cause)
    
    /**
     * Get user-friendly Arabic error message
     */
    fun getUserMessage(): String = message ?: "حدث خطأ غير متوقع"
}

/**
 * Helper function to wrap operations in try-catch and return Result
 */
inline fun <T> resultOf(block: () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: AppException) {
        Result.Error(e)
    } catch (e: Exception) {
        Result.Error(AppException.UnknownError(cause = e))
    }
}

/**
 * Helper function for async operations with better error categorization
 */
suspend inline fun <T> safeApiCall(
    errorMessage: String = "فشلت العملية",
    block: suspend () -> T
): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: java.net.UnknownHostException) {
        Result.Error(AppException.NetworkError("لا يوجد اتصال بالإنترنت", e))
    } catch (e: java.net.SocketTimeoutException) {
        Result.Error(AppException.NetworkError("انتهت مهلة الاتصال بالخادم", e))
    } catch (e: java.io.IOException) {
        Result.Error(AppException.NetworkError("فشل الاتصال بالخادم", e))
    } catch (e: AppException) {
        Result.Error(e)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.Error(AppException.UnknownError(errorMessage, e))
    }
}
