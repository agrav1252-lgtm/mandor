package com.mandor.ui.employee.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun ClientInfoCard(
    phone: String,
    storeName: String,
    governorate: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .padding(top = 2.dp),
            backgroundColor = EmployeeTokens.AccentIndigo.copy(alpha = 0.08f),
            shape = RoundedCornerShape(6.dp),
            elevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .border(1.dp, EmployeeTokens.AccentIndigo.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (phone.isNotEmpty()) {
                    InfoChip(text = phone, icon = "📞")
                }
                if (storeName.isNotEmpty()) {
                    InfoChip(text = storeName, icon = "🏪")
                }
                if (governorate.isNotEmpty()) {
                    InfoChip(text = governorate, icon = "📍")
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, icon: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 10.sp, color = EmployeeTokens.TextPrimary, fontWeight = FontWeight.Medium)
        Text(icon, fontSize = 10.sp)
    }
}
