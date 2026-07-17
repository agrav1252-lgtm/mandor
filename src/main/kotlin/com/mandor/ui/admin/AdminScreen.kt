package com.mandor.ui.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.SupabaseCategory
import com.mandor.SupabaseEmployee
import com.mandor.domain.model.Product
import com.mandor.ui.admin.viewmodel.AdminViewModel
import com.mandor.ui.admin.viewmodel.CategoryWithTypes
import com.mandor.ui.employee.EmployeeTokens
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun AdminDashboard(modifier: Modifier = Modifier) {
    val vm: AdminViewModel = koinInject()

    LaunchedEffect(vm.feedbackMessage) {
        if (vm.feedbackMessage.isNotEmpty()) {
            delay(3000)
            vm.clearFeedback()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EmployeeTokens.BgDeep)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(EmployeeTokens.BgElevated, EmployeeTokens.BgSurface)))
                .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(14.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabTitles = listOf("🏪 المخزن", "👥 الموظفين")
            tabTitles.forEachIndexed { index, title ->
                val interaction = remember { MutableInteractionSource() }
                val hovered by interaction.collectIsHoveredAsState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (vm.activeTab == index) EmployeeTokens.AccentIndigo.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable(interactionSource = interaction, indication = null) { vm.activeTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        color = if (vm.activeTab == index) EmployeeTokens.AccentIndigo else EmployeeTokens.TextSecondary,
                        fontWeight = if (vm.activeTab == index) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = vm.activeTab,
                transitionSpec = {
                    val d = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { it * d * -1 } + fadeIn(tween(250))) togetherWith
                    (slideOutHorizontally { it * d } + fadeOut(tween(200)))
                },
                label = "adminTab"
            ) { tab ->
                when (tab) {
                    0 -> WarehouseTab(vm)
                    1 -> EmployeesTab(vm)
                }
            }
        }
    }

    AnimatedVisibility(
        visible = vm.feedbackMessage.isNotEmpty(),
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (vm.isSuccess) EmployeeTokens.AccentGreen else EmployeeTokens.AccentRed)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(vm.feedbackMessage, color = EmployeeTokens.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Warehouse Tab ──────────────────────────────────────

@Composable
private fun WarehouseTab(vm: AdminViewModel) {
    val categoriesWithTypes by vm.categoriesWithTypes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Add Category Section ──
        CategoryAddSection(vm)

        // ── Categories List ──
        categoriesWithTypes.forEach { cwt ->
            CategoryCard(vm, cwt)
        }

        if (categoriesWithTypes.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                Text("لا توجد أصناف بعد", color = EmployeeTokens.TextMuted)
            }
        }
    }
}

@Composable
private fun CategoryAddSection(vm: AdminViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EmployeeTokens.BgElevated)
            .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("➕ إضافة صنف جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmployeeTokens.TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AdminStyledField(
                value = vm.newCategoryName,
                onValueChange = vm::updateNewCategoryName,
                hint = "اسم الصنف",
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = vm::saveCategory,
                enabled = !vm.isLoading && vm.newCategoryName.isNotBlank(),
                modifier = Modifier.height(44.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = EmployeeTokens.AccentGreen, contentColor = EmployeeTokens.TextPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إضافة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CategoryCard(vm: AdminViewModel, cwt: CategoryWithTypes) {
    val cat = cwt.category
    var expanded by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (hovered) EmployeeTokens.BgSurface else EmployeeTokens.BgBase)
            .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(10.dp))
    ) {
        // ── Category Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interaction, indication = null) { expanded = !expanded }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null, tint = EmployeeTokens.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))

            if (vm.editingCategoryId == cat.id) {
                AdminStyledField(
                    value = vm.editingCategoryName,
                    onValueChange = vm::updateEditingCategoryName,
                    hint = "تعديل الاسم",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { vm.renameCategory(cat.id) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Check, null, tint = EmployeeTokens.AccentGreen, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = vm::cancelRenameCategory, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, tint = EmployeeTokens.AccentRed, modifier = Modifier.size(16.dp))
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(cat.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmployeeTokens.TextPrimary)
                    Text("${cwt.products.size} نوع", fontSize = 10.sp, color = EmployeeTokens.TextMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = { vm.startRenameCategory(cat) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, null, tint = EmployeeTokens.AccentAmber, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { vm.deleteCategory(cat.id) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = EmployeeTokens.AccentRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ── Expanded Content ──
        AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Products of this category
                cwt.products.forEach { product ->
                    ProductTypeCard(vm, product, cat)
                }

                if (cwt.products.isEmpty()) {
                    Text("لا توجد أنواع في هذا الصنف", color = EmployeeTokens.TextMuted, fontSize = 11.sp)
                }

                // ── Add Type Form ──
                if (vm.activeCategoryId == cat.id) {
                    Divider(color = EmployeeTokens.BorderColor, thickness = 0.5.dp)
                    AddProductTypeForm(vm)
                } else {
                    OutlinedButton(
                        onClick = { vm.setActiveCategory(cat.id, cat.name) },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmployeeTokens.AccentIndigo),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmployeeTokens.AccentIndigo.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("إضافة نوع", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductTypeCard(vm: AdminViewModel, product: Product, cat: SupabaseCategory) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (hovered) EmployeeTokens.BgSurface else EmployeeTokens.BgElevated)
            .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(8.dp))
            .clickable(interactionSource = interaction, indication = null) { vm.loadProductForEdit(product, cat.id, cat.name) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmployeeTokens.TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (product.barcode.isNotEmpty()) Text("باركود: ${product.barcode}", fontSize = 10.sp, color = EmployeeTokens.TextMuted)
                Text("مخزون: ${product.stock}", fontSize = 10.sp, color = if (product.stock > 0) EmployeeTokens.AccentGreen else EmployeeTokens.AccentRed)
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("%.2f ج.م".format(product.price), fontSize = 13.sp, fontWeight = FontWeight.Black, color = EmployeeTokens.AccentGreen)
            Icon(Icons.Default.Delete, null, tint = EmployeeTokens.AccentRed, modifier = Modifier.size(16.dp).clickable { vm.deleteProduct(product.code) })
        }
    }
}

@Composable
private fun AddProductTypeForm(vm: AdminViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            if (vm.isEditingProduct) "✏️ تعديل النوع" else "➕ إضافة نوع",
            fontSize = 11.sp, fontWeight = FontWeight.Bold,
            color = EmployeeTokens.TextSecondary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            AdminStyledField(vm.productCode, vm::updateProductCode, "الكود (مثل fl-1257)", Modifier.weight(1f), fontSize = 12)
            AdminStyledField(vm.productName, vm::updateProductName, "الاسم *", Modifier.weight(1f), fontSize = 12)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            AdminStyledField(vm.productPrice, vm::updateProductPrice, "السعر *", Modifier.weight(1f), KeyboardType.Decimal, fontSize = 12)
            AdminStyledField(vm.productStock, vm::updateProductStock, "المخزون", Modifier.weight(1f), KeyboardType.Number, fontSize = 12)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            AdminStyledField(vm.productBarcode, vm::updateProductBarcode, "الباركود (أو اتركه لو استخدمت الكود)", Modifier.weight(1f), fontSize = 12)
            Spacer(Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = vm::saveProduct,
                enabled = !vm.isLoading,
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = EmployeeTokens.AccentGreen, contentColor = EmployeeTokens.TextPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (vm.isEditingProduct) "تحديث" else "إضافة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { vm.setActiveCategory(null, "") },
                modifier = Modifier.weight(1f).height(38.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmployeeTokens.TextSecondary),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmployeeTokens.BorderColor)
            ) {
                Text("إلغاء", fontSize = 12.sp)
            }
        }
    }
}

private val fieldModifier = Modifier
    .fillMaxWidth()
    .heightIn(min = 44.dp)
    .clip(RoundedCornerShape(8.dp))
    .background(EmployeeTokens.BgBase)
    .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(8.dp))

@Composable
private fun TextFieldStyle() = TextFieldDefaults.textFieldColors(
    backgroundColor = Color.Transparent,
    cursorColor = EmployeeTokens.AccentIndigo,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent
)

@Composable
private fun AdminStyledField(
    value: String, onValueChange: (String) -> Unit, hint: String,
    modifier: Modifier = Modifier, keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
    fontSize: Int = 13
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint, color = EmployeeTokens.TextMuted, fontSize = 11.sp) },
        textStyle = MaterialTheme.typography.body2.copy(color = EmployeeTokens.TextPrimary, fontSize = fontSize.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = true,
        modifier = modifier.then(fieldModifier).padding(horizontal = 10.dp),
        colors = TextFieldStyle()
    )
}

// ── Employees Tab ──────────────────────────────────────

@Composable
private fun EmployeesTab(vm: AdminViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("👥 الموظفون (${vm.employees.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmployeeTokens.TextPrimary)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(EmployeeTokens.BgElevated)
                .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("إضافة موظف جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmployeeTokens.TextSecondary)

            AdminStyledField(vm.employeeName, vm::updateEmployeeName, "اسم الموظف")
            AdminStyledField(vm.employeePassword, vm::updateEmployeePassword, "كلمة السر", password = true)
            Button(
                onClick = vm::saveEmployee,
                enabled = !vm.isLoading,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = EmployeeTokens.AccentIndigo, contentColor = EmployeeTokens.TextPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إضافة الموظف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (vm.employees.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                Text("لا يوجد موظفون", color = EmployeeTokens.TextMuted)
            }
        }

        vm.employees.forEach { emp ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmployeeTokens.BgBase)
                    .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(emp.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmployeeTokens.TextPrimary)
                    Text("ID: ${emp.id}", fontSize = 10.sp, color = EmployeeTokens.TextMuted)
                }
                Icon(Icons.Default.Close, null, tint = EmployeeTokens.AccentRed, modifier = Modifier.size(20.dp).clickable { vm.deleteEmployee(emp.id) })
            }
        }
    }
}
