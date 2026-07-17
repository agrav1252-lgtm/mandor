package com.mandor

import java.io.File
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * مدير Preferences آمن مع تشفير AES-256
 * يحفظ بيانات تسجيل الدخول بشكل مشفر
 */
object SecurePreferences {
    private val prefsFile = File(System.getProperty("user.home"), ".mandor_prefs")
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
    
    // مفتاح التشفير (يجب تغييره في الإنتاج!)
    private const val SECRET_KEY = "M@nd0r_S3cur3_K3y_2024!#$%"
    
    /**
     * حفظ بيانات تسجيل الدخول بشكل مشفر
     */
    fun saveCredentials(username: String, password: String, isAdmin: Boolean) {
        try {
            val data = "$username:$password:$isAdmin"
            val encrypted = encrypt(data)
            prefsFile.writeText(encrypted)
            println("✓ تم حفظ بيانات تسجيل الدخول (مشفرة)")
        } catch (e: Exception) {
            println("✗ خطأ في حفظ البيانات: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * استرجاع بيانات تسجيل الدخول المشفرة
     * @return Triple(username, password, isAdmin) أو null إذا لم توجد بيانات
     */
    fun getCredentials(): Triple<String, String, Boolean>? {
        return try {
            if (!prefsFile.exists()) {
                println("⚠ لا توجد بيانات محفوظة")
                return null
            }
            
            val encrypted = prefsFile.readText()
            if (encrypted.isEmpty()) {
                println("⚠ ملف البيانات فارغ")
                return null
            }
            
            val decrypted = decrypt(encrypted)
            val parts = decrypted.split(":")
            
            if (parts.size != 3) {
                println("✗ بيانات غير صالحة")
                return null
            }
            
            val username = parts[0]
            val password = parts[1]
            val isAdmin = parts[2].toBoolean()
            
            println("✓ تم استرجاع بيانات تسجيل الدخول")
            Triple(username, password, isAdmin)
        } catch (e: Exception) {
            println("✗ خطأ في استرجاع البيانات: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * حذف بيانات تسجيل الدخول المحفوظة
     */
    fun clearCredentials() {
        try {
            if (prefsFile.exists()) {
                prefsFile.delete()
                println("✓ تم حذف بيانات تسجيل الدخول")
            }
        } catch (e: Exception) {
            println("✗ خطأ في حذف البيانات: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * التحقق من وجود بيانات محفوظة
     */
    fun hasCredentials(): Boolean {
        return prefsFile.exists() && prefsFile.length() > 0
    }
    
    // ══════════════════════════════════════════════════════════
    // Encryption / Decryption (AES-256)
    // ══════════════════════════════════════════════════════════
    
    private fun encrypt(data: String): String {
        val salt = "Mandor2024Salt".toByteArray()
        val iv = "Mandor2024IV1234".toByteArray() // 16 bytes
        
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val spec = PBEKeySpec(SECRET_KEY.toCharArray(), salt, 65536, 256)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        
        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }
    
    private fun decrypt(encryptedData: String): String {
        val salt = "Mandor2024Salt".toByteArray()
        val iv = "Mandor2024IV1234".toByteArray() // 16 bytes
        
        val factory = SecretKeyFactory.getInstance(KEY_ALGORITHM)
        val spec = PBEKeySpec(SECRET_KEY.toCharArray(), salt, 65536, 256)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        
        val decoded = Base64.getDecoder().decode(encryptedData)
        val decrypted = cipher.doFinal(decoded)
        return String(decrypted)
    }
}
