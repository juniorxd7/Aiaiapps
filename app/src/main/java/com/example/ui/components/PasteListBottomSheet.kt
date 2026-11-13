package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.WordListParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasteListBottomSheet(
    hasExistingWords: Boolean,
    onDismiss: () -> Unit,
    onImport: (rawText: String, replaceExisting: Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var rawText by rememberSaveable { mutableStateOf("") }
    var replaceExisting by rememberSaveable { mutableStateOf(false) }

    val detectedCount by remember(rawText) {
        derivedStateOf { WordListParser.parseWords(rawText).size }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("paste_list_sheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.paste_list_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = stringResource(R.string.paste_list_sheet_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .testTag("paste_list_input"),
                placeholder = { Text(stringResource(R.string.paste_list_placeholder)) },
                shape = MaterialTheme.shapes.medium,
            )

            Text(
                text = stringResource(R.string.paste_list_detected_words, detectedCount),
                style = MaterialTheme.typography.labelMedium,
                color = if (detectedCount > 0) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )

            if (hasExistingWords) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !replaceExisting,
                        onClick = { replaceExisting = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("import_mode_append"),
                    ) {
                        Text(
                            text = stringResource(R.string.paste_list_mode_append),
                            maxLines = 1,
                        )
                    }
                    SegmentedButton(
                        selected = replaceExisting,
                        onClick = { replaceExisting = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("import_mode_replace"),
                    ) {
                        Text(
                            text = stringResource(R.string.paste_list_mode_replace),
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("cancel_import_button"),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.cancel_button))
                }
                Button(
                    onClick = { onImport(rawText, replaceExisting) },
                    enabled = detectedCount > 0,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(50.dp)
                        .testTag("confirm_import_button"),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.paste_list_confirm))
                }
            }
        }
    }
}
