package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.HistorySection
import com.example.ui.components.PasteListBottomSheet
import com.example.ui.components.ResultStageCard
import com.example.ui.components.WordInputCard
import com.example.ui.components.WordPoolSection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SorteiaScreen(
    viewModel: SorteiaViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var showPasteSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            snackbarHostState.currentSnackbarData?.dismiss()
            when (event) {
                is SorteiaEvent.DuplicateWord -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.duplicate_word_snackbar, event.word),
                        duration = SnackbarDuration.Short,
                    )
                }

                is SorteiaEvent.ImportedWords -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.imported_words_snackbar, event.count),
                        duration = SnackbarDuration.Short,
                    )
                }

                SorteiaEvent.ListCleared -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.list_cleared_snackbar),
                        actionLabel = context.getString(R.string.undo_action),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoClear()
                    }
                }
            }
        }
    }

    val onDrawClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        viewModel.drawRandomWord()
    }

    val onCopyWord: (String) -> Unit = { word ->
        clipboardManager.setText(AnnotatedString(word))
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.copied_snackbar, word),
                duration = SnackbarDuration.Short,
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = stringResource(R.string.app_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            val isWide = maxWidth >= 680.dp

            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        ResultStageCard(
                            uiState = uiState,
                            onDrawClick = onDrawClick,
                            onResetRoundClick = viewModel::resetRound,
                            onCopyWordClick = onCopyWord,
                        )
                        HistorySection(
                            drawnHistory = uiState.drawnHistory,
                            onResetRound = viewModel::resetRound,
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        WordInputCard(
                            avoidRepeats = uiState.avoidRepeats,
                            onAddWord = viewModel::addWord,
                            onOpenPasteList = { showPasteSheet = true },
                            onToggleAvoidRepeats = viewModel::toggleAvoidRepeats,
                        )
                        WordPoolSection(
                            uiState = uiState,
                            onRemoveWord = viewModel::removeWord,
                            onClearAll = viewModel::clearAll,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        ResultStageCard(
                            uiState = uiState,
                            onDrawClick = onDrawClick,
                            onResetRoundClick = viewModel::resetRound,
                            onCopyWordClick = onCopyWord,
                        )
                        HistorySection(
                            drawnHistory = uiState.drawnHistory,
                            onResetRound = viewModel::resetRound,
                        )
                        WordInputCard(
                            avoidRepeats = uiState.avoidRepeats,
                            onAddWord = viewModel::addWord,
                            onOpenPasteList = { showPasteSheet = true },
                            onToggleAvoidRepeats = viewModel::toggleAvoidRepeats,
                        )
                        WordPoolSection(
                            uiState = uiState,
                            onRemoveWord = viewModel::removeWord,
                            onClearAll = viewModel::clearAll,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (showPasteSheet) {
        PasteListBottomSheet(
            hasExistingWords = uiState.words.isNotEmpty(),
            onDismiss = { showPasteSheet = false },
            onImport = { rawText, replaceExisting ->
                viewModel.importList(rawText, replaceExisting)
                showPasteSheet = false
            },
        )
    }
}
