package com.mandor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * مدير الثيمات - التبديل بين الوضع الداكن والفاتح
 */
object ThemeManager {
    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleTheme() {
        isDarkMode = !isDarkMode
        println("Theme switched to: ${if (isDarkMode) "Dark" else "Light"}")
    }

    // ── ألوان الوضع الداكن ──────────────────────────────
    object DarkTheme {
        val BgDeep = Color(0xFF070B14)
        val BgBase = Color(0xFF0D1117)
        val BgSurface = Color(0xFF161B27)
        val BgElevated = Color(0xFF1C2333)
        val BorderColor = Color(0xFF2A3547)
        val BorderGlow = Color(0xFF3D4F6B)

        val AccentIndigo = Color(0xFF6366F1)
        val AccentGreen = Color(0xFF10B981)
        val AccentAmber = Color(0xFFFBBF24)
        val AccentRed = Color(0xFFF43F5E)
        val AccentBlue = Color(0xFF3B82F6)

        val TextPrimary = Color(0xFFEFF6FF)
        val TextSecondary = Color(0xFF94A3B8)
        val TextMuted = Color(0xFF475569)
    }

    // ── ألوان الوضع الفاتح ──────────────────────────────
    object LightTheme {
        val BgDeep = Color(0xFFF8FAFC)
        val BgBase = Color(0xFFFFFFFF)
        val BgSurface = Color(0xFFF1F5F9)
        val BgElevated = Color(0xFFE2E8F0)
        val BorderColor = Color(0xFFCBD5E1)
        val BorderGlow = Color(0xFF94A3B8)

        val AccentIndigo = Color(0xFF4F46E5)
        val AccentGreen = Color(0xFF059669)
        val AccentAmber = Color(0xFFF59E0B)
        val AccentRed = Color(0xFFDC2626)
        val AccentBlue = Color(0xFF2563EB)

        val TextPrimary = Color(0xFF0F172A)
        val TextSecondary = Color(0xFF475569)
        val TextMuted = Color(0xFF94A3B8)
    }

    // ── الألوان الحالية (ديناميكية) ──────────────────────
    object Theme {
        val BgDeep: Color get() = if (isDarkMode) DarkTheme.BgDeep else LightTheme.BgDeep
        val BgBase: Color get() = if (isDarkMode) DarkTheme.BgBase else LightTheme.BgBase
        val BgSurface: Color get() = if (isDarkMode) DarkTheme.BgSurface else LightTheme.BgSurface
        val BgElevated: Color get() = if (isDarkMode) DarkTheme.BgElevated else LightTheme.BgElevated
        val BorderColor: Color get() = if (isDarkMode) DarkTheme.BorderColor else LightTheme.BorderColor
        val BorderGlow: Color get() = if (isDarkMode) DarkTheme.BorderGlow else LightTheme.BorderGlow

        val AccentIndigo: Color get() = if (isDarkMode) DarkTheme.AccentIndigo else LightTheme.AccentIndigo
        val AccentGreen: Color get() = if (isDarkMode) DarkTheme.AccentGreen else LightTheme.AccentGreen
        val AccentAmber: Color get() = if (isDarkMode) DarkTheme.AccentAmber else LightTheme.AccentAmber
        val AccentRed: Color get() = if (isDarkMode) DarkTheme.AccentRed else LightTheme.AccentRed
        val AccentBlue: Color get() = if (isDarkMode) DarkTheme.AccentBlue else LightTheme.AccentBlue

        val TextPrimary: Color get() = if (isDarkMode) DarkTheme.TextPrimary else LightTheme.TextPrimary
        val TextSecondary: Color get() = if (isDarkMode) DarkTheme.TextSecondary else LightTheme.TextSecondary
        val TextMuted: Color get() = if (isDarkMode) DarkTheme.TextMuted else LightTheme.TextMuted
    }
}
