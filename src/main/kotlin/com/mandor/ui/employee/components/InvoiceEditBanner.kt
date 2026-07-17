package com.mandor.ui.employee.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun InvoiceEditBanner(
    editingInvoiceId: String?,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = false,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(EmployeeTokens.AccentAmber.copy(alpha = 0.12f))
                .border(1.dp, EmployeeTokens.AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancelEdit) {
                Text("إلغاء التعديل", color = EmployeeTokens.AccentRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Edit, null, tint = EmployeeTokens.AccentAmber, modifier = Modifier.size(14.dp))
                Text(
                    "وضع تعديل الفاتورة: $editingInvoiceId",
                    color = EmployeeTokens.AccentAmber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
