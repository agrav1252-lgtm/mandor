package com.mandor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.mandor.SupabaseManager

@Composable
fun LoginScreen(onLoginSuccess: (isAdmin: Boolean, rememberMe: Boolean, username: String, password: String) -> Unit) {
    var isAdmin by remember { mutableStateOf(false) } // False for Employee, True for Admin
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) } // ✓ NEW: Remember me checkbox
    var errorText by remember { mutableStateOf("") }
    
    val theme = ThemeManager.Theme // الحصول على الثيم الحالي

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(theme.BgSurface, theme.BgDeep),
                    center = Offset(300f, 300f),
                    radius = 1200f
                )
            )
    ) {
        // Left Side: Modern Wholesale Stationery Illustration & Branding
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Vector illustration drawn using Canvas
                StationeryIllustration(
                    modifier = Modifier
                        .size(350.dp)
                        .padding(bottom = 24.dp),
                    theme = theme
                )

                Text(
                    text = "مندور للجملة",
                    style = MaterialTheme.typography.h4.copy(
                        fontWeight = FontWeight.Black,
                        color = theme.TextPrimary
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "النظام الذكي لإدارة مبيعات القرطاسية، الأدوات المدرسية، والألعاب",
                    style = MaterialTheme.typography.subtitle1.copy(
                        color = theme.TextSecondary,
                        fontSize = 15.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // Right Side: Beautiful Glassmorphic Login Form
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(theme.BgBase.copy(alpha = 0.3f))
                .border(1.dp, theme.BorderColor.copy(alpha = 0.3f))
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(theme.BgElevated.copy(alpha = 0.8f))
                    .border(1.dp, theme.BorderColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(32.dp),
                horizontalAlignment = Alignment.End // RTL layout design
            ) {
                Text(
                    text = "تسجيل الدخول",
                    style = MaterialTheme.typography.h5.copy(
                        fontWeight = FontWeight.Bold,
                        color = theme.TextPrimary
                    ),
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Right
                )

                Text(
                    text = "الرجاء اختيار نوع الحساب وإدخال البيانات المعتمدة",
                    style = MaterialTheme.typography.body2.copy(color = theme.TextSecondary),
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Right
                )

                // Role Selector Switcher (Modern Animated Slider Design)
                RoleSelector(
                    isAdmin = isAdmin,
                    onRoleChanged = { 
                        isAdmin = it
                        errorText = "" 
                    },
                    theme = theme
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Username Input
                Text(
                    text = "اسم المستخدم",
                    style = MaterialTheme.typography.subtitle2.copy(color = theme.TextPrimary),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = theme.AccentIndigo) },
                        placeholder = { Text("أدخل اسم المستخدم", color = theme.TextSecondary) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = theme.TextPrimary,
                            focusedBorderColor = theme.AccentIndigo,
                            unfocusedBorderColor = theme.BorderColor,
                            backgroundColor = theme.BgBase
                        ),
                        singleLine = true
                    )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                Text(
                    text = "كلمة المرور",
                    style = MaterialTheme.typography.subtitle2.copy(color = theme.TextPrimary),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = theme.AccentIndigo) },
                        placeholder = { Text("أدخل كلمة المرور", color = theme.TextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = theme.TextPrimary,
                            focusedBorderColor = theme.AccentIndigo,
                            unfocusedBorderColor = theme.BorderColor,
                            backgroundColor = theme.BgBase
                        ),
                        singleLine = true
                    )

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = theme.AccentRed,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✓ NEW: Remember Me Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { rememberMe = !rememberMe },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تذكرني على هذا الجهاز",
                        color = theme.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = theme.AccentIndigo,
                            uncheckedColor = theme.BorderColor,
                            checkmarkColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Modern Interactive Submit Button
                val coroutineScope = rememberCoroutineScope()
                var isLoggingIn by remember { mutableStateOf(false) }
                
                Button(
                    onClick = {
                        if (username.isEmpty() || password.isEmpty()) {
                            errorText = "الرجاء ملء جميع الحقول المطلوبة!"
                        } else {
                            isLoggingIn = true
                            errorText = ""
                            
                            if (isAdmin) {
                                coroutineScope.launch {
                                    try {
                                        if (SupabaseManager.verifyAdmin(username, password)) {
                                            onLoginSuccess(true, rememberMe, username, password)
                                        } else {
                                            errorText = "اسم المستخدم أو كلمة مرور المدير غير صحيحة!"
                                        }
                                    } catch (e: Exception) {
                                        errorText = "خطأ في الاتصال. تحقق من الإنترنت وحاول مرة أخرى."
                                        e.printStackTrace()
                                    } finally {
                                        isLoggingIn = false
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    try {
                                        if (SupabaseManager.verifyEmployee(username, password)) {
                                            onLoginSuccess(false, rememberMe, username, password)
                                        } else {
                                            errorText = "اسم المستخدم أو كلمة مرور الموظف غير صحيحة!"
                                        }
                                    } catch (e: Exception) {
                                        errorText = "خطأ في الاتصال. تحقق من الإنترنت وحاول مرة أخرى."
                                        e.printStackTrace()
                                    } finally {
                                        isLoggingIn = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(12.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isAdmin) theme.AccentRed else theme.AccentIndigo
                    ),
                    enabled = !isLoggingIn
                ) {
                    if (isLoggingIn) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isAdmin) "الدخول كمدير النظام" else "الدخول كموظف مبيعات",
                            style = MaterialTheme.typography.button.copy(color = Color.White)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSelector(isAdmin: Boolean, onRoleChanged: (Boolean) -> Unit, theme: ThemeManager.Theme) {
    val indicatorOffset by animateFloatAsState(
        targetValue = if (isAdmin) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(theme.BgBase)
            .border(1.dp, theme.BorderColor, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        val width = maxWidth

        // Animated Slider Background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(width / 2)
                .offset(x = (width / 2) * indicatorOffset)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isAdmin) {
                        Brush.horizontalGradient(listOf(theme.AccentRed.copy(alpha = 0.9f), theme.AccentRed))
                    } else {
                        Brush.horizontalGradient(listOf(theme.AccentIndigo.copy(alpha = 0.9f), theme.AccentIndigo))
                    }
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onRoleChanged(false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "موظف مبيعات",
                    color = if (!isAdmin) Color.White else theme.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onRoleChanged(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "إدارة الشركة",
                    color = if (isAdmin) Color.White else theme.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Stunning Vector drawing of a colorful Lunchbox, Notebook, and Pencil using Canvas API
@Composable
fun StationeryIllustration(modifier: Modifier = Modifier, theme: ThemeManager.Theme) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 1. Draw glowing background accent
        drawCircle(
            color = theme.AccentIndigo.copy(alpha = 0.15f),
            radius = canvasWidth * 0.45f,
            center = Offset(canvasWidth * 0.5f, canvasHeight * 0.5f)
        )

        // 2. Draw a modern notebook (دفتر)
        val bookWidth = canvasWidth * 0.4f
        val bookHeight = canvasHeight * 0.5f
        val bookX = canvasWidth * 0.2f
        val bookY = canvasHeight * 0.25f

        // Shadow of the book
        drawRoundRect(
            color = Color(0x33000000),
            topLeft = Offset(bookX + 15f, bookY + 20f),
            size = Size(bookWidth, bookHeight),
            cornerRadius = CornerRadius(16f, 16f)
        )

        // The notebook cover
        drawRoundRect(
            color = Color(0xFFF43F5E), // Pink Rose notebook
            topLeft = Offset(bookX, bookY),
            size = Size(bookWidth, bookHeight),
            cornerRadius = CornerRadius(16f, 16f)
        )

        // Notebook pages stripe (White side)
        drawRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(bookX + bookWidth - 25f, bookY + 10f),
            size = Size(15f, bookHeight - 20f)
        )

        // Notebook design element (Cyan diagonal stripe)
        val stripePath = Path().apply {
            moveTo(bookX + 10f, bookY + bookHeight * 0.4f)
            lineTo(bookX + bookWidth * 0.6f, bookY + bookHeight * 0.9f)
            lineTo(bookX + bookWidth * 0.6f - 30f, bookY + bookHeight * 0.9f)
            lineTo(bookX + 10f, bookY + bookHeight * 0.4f + 30f)
            close()
        }
        drawPath(stripePath, color = Color(0xFF06B6D4))

        // 3. Draw lunchbox (لانش بوكس)
        val lbWidth = canvasWidth * 0.35f
        val lbHeight = canvasHeight * 0.35f
        val lbX = canvasWidth * 0.45f
        val lbY = canvasHeight * 0.45f

        // Lunchbox shadow
        drawRoundRect(
            color = Color(0x22000000),
            topLeft = Offset(lbX + 10f, lbY + 15f),
            size = Size(lbWidth, lbHeight),
            cornerRadius = CornerRadius(24f, 24f)
        )

        // Lunchbox body
        drawRoundRect(
            color = Color(0xFF0EA5E9), // Sky blue lunch box
            topLeft = Offset(lbX, lbY),
            size = Size(lbWidth, lbHeight),
            cornerRadius = CornerRadius(24f, 24f)
        )

        // Lunchbox lid highlight
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(lbX + 8f, lbY + 8f),
            size = Size(lbWidth - 16f, lbHeight * 0.3f),
            cornerRadius = CornerRadius(14f, 14f)
        )

        // Lunchbox handle
        val handlePath = Path().apply {
            moveTo(lbX + lbWidth * 0.3f, lbY)
            quadraticTo(lbX + lbWidth * 0.5f, lbY - 30f, lbX + lbWidth * 0.7f, lbY)
        }
        drawPath(
            path = handlePath,
            color = Color(0xFF0284C7),
            style = Stroke(width = 12f)
        )

        // 4. Draw modern yellow pencil (قلم رصاص)
        val pencilPath = Path().apply {
            moveTo(canvasWidth * 0.35f, canvasHeight * 0.75f)
            lineTo(canvasWidth * 0.75f, canvasHeight * 0.25f)
            lineTo(canvasWidth * 0.78f, canvasHeight * 0.28f)
            lineTo(canvasWidth * 0.38f, canvasHeight * 0.78f)
            close()
        }
        drawPath(pencilPath, color = Color(0xFFFBBF24)) // Yellow pencil body

        // Pencil tip (Wooden part & graphite lead tip)
        val pencilTipPath = Path().apply {
            moveTo(canvasWidth * 0.35f, canvasHeight * 0.75f)
            lineTo(canvasWidth * 0.38f, canvasHeight * 0.78f)
            lineTo(canvasWidth * 0.31f, canvasHeight * 0.82f) // Tip point
            close()
        }
        drawPath(pencilTipPath, color = Color(0xFFFED7AA)) // Peach wood tip

        val pencilLeadPath = Path().apply {
            moveTo(canvasWidth * 0.33f, canvasHeight * 0.80f)
            lineTo(canvasWidth * 0.34f, canvasHeight * 0.81f)
            lineTo(canvasWidth * 0.31f, canvasHeight * 0.82f)
            close()
        }
        drawPath(pencilLeadPath, color = Color(0xFF1E293B)) // Lead
    }
}
