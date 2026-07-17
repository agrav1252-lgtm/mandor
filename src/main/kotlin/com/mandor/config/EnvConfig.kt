package com.mandor.config

import java.io.File
import java.util.Properties

/**
 * Secure configuration manager that loads sensitive data from .env file
 * instead of hardcoding them in source code.
 * 
 * Usage:
 * 1. Create a .env file in project root with:
 *    SUPABASE_URL=your_url
 *    SUPABASE_ANON_KEY=your_key
 * 2. Access via EnvConfig.supabaseUrl and EnvConfig.supabaseAnonKey
 */
object EnvConfig {
    private val properties = Properties()
    
    init {
        loadEnvFile()
    }
    
    private fun loadEnvFile() {
        val envFile = File(".env")
        if (envFile.exists()) {
            envFile.inputStream().use { properties.load(it) }
            println("✓ Loaded configuration from .env file")
        } else {
            println("⚠ Warning: .env file not found, using environment variables")
        }
    }
    
    /**
     * Get configuration value from .env file or system environment
     */
    private fun getConfigValue(key: String): String {
        // First try .env file
        val fromFile = properties.getProperty(key)
        if (fromFile != null) return fromFile
        
        // Fallback to system environment
        val fromEnv = System.getenv(key)
        if (fromEnv != null) return fromEnv
        
        // If not found, throw error
        throw IllegalStateException(
            """
            Missing required configuration: $key
            
            Please create a .env file in project root with:
            $key=your_value
            
            Or set it as system environment variable.
            """.trimIndent()
        )
    }
    
    val supabaseUrl: String by lazy {
        getConfigValue("SUPABASE_URL")
    }
    
    val supabaseAnonKey: String by lazy {
        getConfigValue("SUPABASE_ANON_KEY")
    }
    
    /**
     * Check if all required configuration is available
     */
    fun validate(): Boolean {
        return try {
            supabaseUrl
            supabaseAnonKey
            println("✓ All configuration values are available")
            true
        } catch (e: IllegalStateException) {
            println("✗ Configuration validation failed: ${e.message}")
            false
        }
    }
}
