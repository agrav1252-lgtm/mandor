package com.mandor.ui.employee.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun InvoiceTableHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(listOf(EmployeeTokens.BgBase, EmployeeTokens.BgBase.copy(alpha = 0.7f))))
            .border(1.dp, EmployeeTokens.BorderColor, RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        InvoiceTableHeaderCell("إجراء", Modifier.weight(0.7f), TextAlign.Center)
        InvoiceTableHeaderCell("الإجمالي", Modifier.weight(1f), TextAlign.Center)
        InvoiceTableHeaderCell("الكمية", Modifier.weight(2f), TextAlign.Center)
        InvoiceTableHeaderCell("سعر الوحدة", Modifier.weight(1f), TextAlign.Center)
        InvoiceTableHeaderCell("اسم الصنف", Modifier.weight(2.8f), TextAlign.End)
    }
}

@Composable
private fun InvoiceTableHeaderCell(text: String, modifier: Modifier, align: TextAlign) {
    Text(
        text = text,
        color = EmployeeTokens.TextSecondary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        modifier = modifier,
        textAlign = align
    )
}
