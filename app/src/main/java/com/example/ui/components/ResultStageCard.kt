package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.SorteiaUiState

@Composable
fun ResultStageCard(
    uiState: SorteiaUiState,
    onDrawClick: () -> Unit,
    onResetRoundClick: () -> Unit,
    onCopyWordClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayedWord = uiState.rollingWord ?: uiState.currentWord
    val hasWinner = !uiState.isDrawing && uiState.currentWord != null

    val glowColor = MaterialTheme.colorScheme.primary.copy(alpha = if (hasWinner) 0.14f else 0.06f)
    val amberGlow = MaterialTheme.colorScheme.tertiary.copy(alpha = if (hasWinner) 0.14f else 0.04f)

    val winnerScale by animateFloatAsState(
        targetValue = if (hasWinner) 1.0f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "winnerScale",
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (hasWinner) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor, glowColor.copy(alpha = 0f)),
                            center = Offset(size.width * 0.2f, size.height * 0.15f),
                            radius = size.maxDimension * 0.7f,
                        ),
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(amberGlow, amberGlow.copy(alpha = 0f)),
                            center = Offset(size.width * 0.85f, size.height * 0.85f),
                            radius = size.maxDimension * 0.65f,
                        ),
                    )
                }
                .padding(24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Status pill
                val badgeText = when {
                    uiState.isDrawing -> stringResource(R.string.result_card_drawing)
                    uiState.currentWord != null -> stringResource(R.string.result_card_winner_label)
                    uiState.allDrawn -> stringResource(R.string.result_card_all_drawn_title)
                    uiState.words.isNotEmpty() -> stringResource(R.string.result_card_ready_title)
                    else -> stringResource(R.string.result_card_empty_title)
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when {
                        hasWinner -> MaterialTheme.colorScheme.primaryContainer
                        uiState.allDrawn -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when {
                                hasWinner -> MaterialTheme.colorScheme.onPrimaryContainer
                                uiState.allDrawn -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                hasWinner -> MaterialTheme.colorScheme.onPrimaryContainer
                                uiState.allDrawn -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Main reveal area
                AnimatedContent(
                    targetState = Triple(displayedWord, uiState.isDrawing, uiState.allDrawn),
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.92f)).togetherWith(fadeOut())
                    },
                    label = "drawnWordTransition",
                ) { (word, isDrawing, allDrawn) ->
                    when {
                        word != null -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        if (!isDrawing) {
                                            scaleX = winnerScale
                                            scaleY = winnerScale
                                        }
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = if (isDrawing) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    textAlign = TextAlign.Center,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .testTag("drawn_word_text"),
                                )
                                if (!isDrawing) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { onCopyWordClick(word) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("copy_word_button"),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = stringResource(R.string.copy_word_cd),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }

                        allDrawn -> {
                            Text(
                                text = stringResource(R.string.result_card_all_drawn_subtitle),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }

                        uiState.words.isNotEmpty() -> {
                            Text(
                                text = stringResource(R.string.result_card_ready_subtitle),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }

                        else -> {
                            Text(
                                text = stringResource(R.string.result_card_empty_subtitle),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Action Button
                if (uiState.allDrawn) {
                    FilledTonalButton(
                        onClick = onResetRoundClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("reset_round_button"),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.reset_round_button),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                } else {
                    Button(
                        onClick = onDrawClick,
                        enabled = uiState.canDraw,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("draw_word_button"),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Casino,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (uiState.currentWord != null) {
                                stringResource(R.string.draw_again_button)
                            } else {
                                stringResource(R.string.draw_button)
                            },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
