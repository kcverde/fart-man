package com.kcverde.fartman.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * The one text field style the game uses.
 *
 * Previously this 12-line `colors(...)` block was pasted verbatim into all four
 * setup fields with the palette hardcoded, so the fields could not follow the
 * theme.
 */
@Composable
fun GameTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  leadingIcon: (@Composable () -> Unit)? = null,
  supportingText: (@Composable () -> Unit)? = null,
  imeAction: ImeAction = ImeAction.Next,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label) },
    leadingIcon = leadingIcon,
    supportingText = supportingText,
    singleLine = true,
    keyboardOptions = KeyboardOptions(imeAction = imeAction),
    shape = RoundedCornerShape(12.dp),
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
      ),
    modifier = modifier.fillMaxWidth(),
  )
}
