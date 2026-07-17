package com.mandor.ui.employee.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mandor.ui.employee.EmployeeTokens

@Composable
fun CompactInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    multiline: Boolean = false,
    minLines: Int = 1,
    onEnterPressed: (() -> Unit)? = null
) {
    val borderColor = if (isError) EmployeeTokens.AccentRed else EmployeeTokens.BorderColor
    val bgColor = if (isError) EmployeeTokens.AccentRed.copy(alpha = 0.05f) else EmployeeTokens.BgBase

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            color = EmployeeTokens.TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (multiline) Modifier.heightIn(min = (34 * minLines).dp)
                    else Modifier.height(32.dp)
                )
                .clip(RoundedCornerShape(7.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(7.dp))
                .padding(horizontal = 8.dp, vertical = if (multiline) 8.dp else 0.dp)
                .onPreviewKeyEvent { keyEvent ->
                    if (onEnterPressed != null &&
                        keyEvent.key == Key.Enter &&
                        keyEvent.type == KeyEventType.KeyDown) {
                        onEnterPressed()
                        true
                    } else {
                        false
                    }
                },
            verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                Box(modifier = Modifier.padding(top = if (multiline) 2.dp else 0.dp)) {
                    leadingIcon()
                }
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = EmployeeTokens.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(EmployeeTokens.AccentIndigo),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = if (onEnterPressed != null) ImeAction.Search else ImeAction.Default
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onEnterPressed?.invoke() }
                ),
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (multiline) Modifier.fillMaxWidth()
                        else Modifier.align(Alignment.CenterVertically)
                    ),
                singleLine = !multiline,
                maxLines = if (multiline) Int.MAX_VALUE else 1,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = if (multiline) Modifier.fillMaxWidth() else Modifier.fillMaxSize(),
                        contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                            text = placeholder,
                            color = EmployeeTokens.TextMuted,
                            fontSize = 11.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
