package com.mandor.ui.employee.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun PaidAmountInput(
    paidAmount: String,
    totalAmount: Double,
    isError: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val remaining = (totalAmount - (paidAmount.toDoubleOrNull() ?: 0.0)).coerceAtLeast(0.0)
    val borderColor = if (isError) EmployeeTokens.AccentRed else EmployeeTokens.AccentAmber
    val bgColor = if (isError) EmployeeTokens.AccentRed.copy(alpha = 0.05f) else EmployeeTokens.BgBase

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "المبلغ المدفوع",
                color = EmployeeTokens.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "المتبقي: %.2f ج.م".format(remaining),
                color = if (remaining > 0) EmployeeTokens.AccentAmber else EmployeeTokens.AccentGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AttachMoney,
                contentDescription = null,
                tint = EmployeeTokens.AccentAmber.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            BasicTextField(
                value = paidAmount,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = EmployeeTokens.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(EmployeeTokens.AccentAmber),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (paidAmount.isEmpty()) {
                            Text(
                                text = "0.00",
                                color = EmployeeTokens.TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Text(
                "ج.م",
                color = EmployeeTokens.TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
