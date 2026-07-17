package com.mandor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mandor.ui.admin.AdminDashboard
import com.mandor.ui.employee.EmployeeDashboard
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import com.mandor.di.appModule

fun main() = application {
    // Initialize Koin DI framework
    startKoin {
        modules(appModule)
    }
    
    // Validate configuration on startup
    if (!com.mandor.config.EnvConfig.validate()) {
        println("⚠️ Application cannot start without proper configuration")
        println("Please check .env file and ensure all required values are set")
        stopKoin()
        exitApplication()
        return@application
    }
    
    val windowState = rememberWindowState(placement = WindowPlacement.Maximized)
    val appIcon = painterResource("icon.png")

    Window(
        onCloseRequest = {
            stopKoin() // Clean up Koin on app close
            exitApplication()
        },
        state = windowState,
        title = "Mandor Wholesale - نظام مندور لإدارة مبيعات الجملة",
        icon = appIcon,
        onKeyEvent = { keyEvent ->
            // اختصار F1 للتبديل بين الثيمات
            if (keyEvent.key == Key.F1 && 
                keyEvent.type == KeyEventType.KeyDown) {
                ThemeManager.toggleTheme()
                true
            } else {
                false
            }
        }
    ) {
        var isLoggedIn by remember { mutableStateOf(false) }
        var loggedInAsAdmin by remember { mutableStateOf(false) }
        var isCheckingAutoLogin by remember { mutableStateOf(true) }

        // ── تسجيل دخول تلقائي ──────────────────────────────────
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(500) // انتظار قصير لتحميل UI
            
            val credentials = SecurePreferences.getCredentials()
            if (credentials != null) {
                val (username, password, isAdmin) = credentials
                println("🔐 محاولة تسجيل دخول تلقائي...")
                
                try {
                    val isValid = if (isAdmin) {
                        SupabaseManager.verifyAdmin(username, password)
                    } else {
                        SupabaseManager.verifyEmployee(username, password)
                    }
                    
                    if (isValid) {
                        println("✓ تسجيل دخول تلقائي ناجح")
                        loggedInAsAdmin = isAdmin
                        isLoggedIn = true
                    } else {
                        println("✗ بيانات تسجيل الدخول المحفوظة غير صالحة")
                        SecurePreferences.clearCredentials()
                    }
                } catch (e: Exception) {
                    println("✗ خطأ في تسجيل الدخول التلقائي: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            isCheckingAutoLogin = false
        }

        MandorTheme {
            if (isCheckingAutoLogin) {
                // شاشة تحميل أثناء التحقق من تسجيل الدخول التلقائي
                Box(
                    modifier = Modifier.fillMaxSize().background(ThemeManager.Theme.BgDeep),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ThemeManager.Theme.AccentIndigo,
                            strokeWidth = 3.dp
                        )
                        Text(
                            "جاري التحقق من بيانات تسجيل الدخول...",
                            color = ThemeManager.Theme.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (!isLoggedIn) {
                LoginScreen(
                    onLoginSuccess = { isAdmin, rememberMe, username, password ->
                        loggedInAsAdmin = isAdmin
                        isLoggedIn = true
                        
                        // حفظ بيانات تسجيل الدخول إذا طُلب ذلك
                        if (rememberMe) {
                            SecurePreferences.saveCredentials(username, password, isAdmin)
                        }
                    }
                )
            } else {
                DashboardScreen(
                    isAdmin = loggedInAsAdmin,
                    onLogout = { 
                        // حذف بيانات تسجيل الدخول عند تسجيل الخروج
                        SecurePreferences.clearCredentials()
                        isLoggedIn = false 
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(isAdmin: Boolean, onLogout: () -> Unit) {
    val theme = ThemeManager.Theme // الحصول على الثيم الحالي
    
    Row(modifier = Modifier.fillMaxSize().background(theme.BgDeep)) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onLogout, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp, 
                            "تسجيل الخروج", 
                            tint = theme.AccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // زر تغيير الثيم
                    IconButton(
                        onClick = { ThemeManager.toggleTheme() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = if (ThemeManager.isDarkMode) "☀️" else "🌙",
                            fontSize = 18.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isAdmin) "لوحة تحكم الإدارة" else "شاشة المبيعات والكاشير",
                        style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                        color = theme.TextPrimary
                    )
                    Text(
                        text = if (isAdmin) "أهلاً بك، المدير العام" else "أهلاً بك، موظف المبيعات",
                        style = MaterialTheme.typography.caption.copy(fontSize = 11.sp),
                        color = theme.TextSecondary
                    )
                }
            }
            if (isAdmin) AdminDashboard() else EmployeeDashboard()
        }
    }
}

@Composable
fun AdminDashboardContent() {
    Column(
        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color(0xFF1E293B)).padding(24.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text("إحصائيات المبيعات الإجمالية للجملة", style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DashboardCard("إجمالي المبيعات اليوم", "٤٥,٢٠٠ ج.م", Color(0xFF10B981), Modifier.weight(1f))
            DashboardCard("الطلبات النشطة", "١٢ طلب", Color(0xFF3B82F6), Modifier.weight(1f))
            DashboardCard("مستوى المخزون", "ممتاز (٩٢٪)", Color(0xFFF59E0B), Modifier.weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Text("المنتجات الأكثر مبيعاً هذا الأسبوع", style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(bottom = 12.dp))
        listOf("لانش بوكس حافظ للحرارة - ٥٠ حبة", "دفتر سلك مسطر ١٠٠ ورقة - ٢٠٠ حبة", "أقلام جاف ملونة - ١٠ علب").forEach { item ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), backgroundColor = Color(0xFF0F172A), elevation = 0.dp) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("مكتمل", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    Text(item, color = Color.White, textAlign = TextAlign.Right)
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(120.dp), shape = RoundedCornerShape(12.dp), backgroundColor = Color(0xFF0F172A), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
            Text(title, style = MaterialTheme.typography.caption, color = Color(0xFF94A3B8))
            Text(value, style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold), color = color)
        }
    }
}
