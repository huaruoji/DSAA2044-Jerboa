package com.jerboa.ui.components.community.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jerboa.R
import com.jerboa.ui.theme.MEDIUM_PADDING
import com.jerboa.ui.theme.SMALL_PADDING

/**
 * A composable that displays a dropdown card with search history items
 * 
 * @param searchHistory List of search history items to display (up to 5 most recent)
 * @param visible Whether the search history should be visible
 * @param onHistoryItemClick Callback when a history item is clicked
 * @param onClearHistory Callback when the clear history button is clicked
 * @param modifier Modifier for the container
 */
@Composable
fun SearchHistoryDropdown(
    searchHistory: List<String>,
    visible: Boolean,
    onHistoryItemClick: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible && searchHistory.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column {
                // Header with "Search History" title and clear button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MEDIUM_PADDING, vertical = SMALL_PADDING),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = stringResource(R.string.search_history_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(R.string.search_history_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    
                    TextButton(
                        onClick = onClearHistory,
                    ) {
                        Text(
                            text = stringResource(R.string.search_history_clear),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Display up to 5 most recent search terms
                searchHistory.take(5).forEach { searchTerm ->
                    SearchHistoryItem(
                        searchTerm = searchTerm,
                        onClick = { onHistoryItemClick(searchTerm) },
                    )
                }
            }
        }
    }
}

/**
 * A single search history item row
 * 
 * @param searchTerm The search term to display
 * @param onClick Callback when the item is clicked
 */
@Composable
private fun SearchHistoryItem(
    searchTerm: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = MEDIUM_PADDING, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(MEDIUM_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = searchTerm,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Alternative design: Inline search history chips below the search bar
 * 
 * @param searchHistory List of search history items to display
 * @param visible Whether the search history should be visible
 * @param onHistoryItemClick Callback when a history item is clicked
 * @param onRemoveItem Callback when remove icon on a chip is clicked
 * @param onClearAll Callback when clear all button is clicked
 */
@Composable
fun SearchHistoryChips(
    searchHistory: List<String>,
    visible: Boolean,
    onHistoryItemClick: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible && searchHistory.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MEDIUM_PADDING, vertical = SMALL_PADDING),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.search_history_recent),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                
                TextButton(
                    onClick = onClearAll,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = stringResource(R.string.search_history_clear),
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = stringResource(R.string.search_history_clear),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            
            // Display search history items as a simple list
            searchHistory.take(5).forEach { searchTerm ->
                SearchHistoryListItem(
                    searchTerm = searchTerm,
                    onClick = { onHistoryItemClick(searchTerm) },
                    onRemove = { onRemoveItem(searchTerm) },
                )
            }
        }
    }
}

/**
 * A search history item with remove button
 */
@Composable
private fun SearchHistoryListItem(
    searchTerm: String,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = SMALL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = searchTerm,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.search_history_remove_item),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// Preview composables for design validation

@Preview(showBackground = true)
@Composable
fun SearchHistoryDropdownPreview() {
    Surface {
        SearchHistoryDropdown(
            searchHistory = listOf(
                "Android Development",
                "Kotlin",
                "Jetpack Compose",
                "Material Design 3",
                "Search History Feature",
            ),
            visible = true,
            onHistoryItemClick = {},
            onClearHistory = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchHistoryDropdownEmptyPreview() {
    Surface {
        SearchHistoryDropdown(
            searchHistory = emptyList(),
            visible = true,
            onHistoryItemClick = {},
            onClearHistory = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchHistoryChipsPreview() {
    Surface {
        SearchHistoryChips(
            searchHistory = listOf(
                "Android",
                "Kotlin Programming",
                "Compose UI",
            ),
            visible = true,
            onHistoryItemClick = {},
            onRemoveItem = {},
            onClearAll = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchHistoryItemPreview() {
    Surface {
        SearchHistoryItem(
            searchTerm = "Example Search Term",
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SearchHistoryListItemPreview() {
    Surface {
        SearchHistoryListItem(
            searchTerm = "Example Search with Remove",
            onClick = {},
            onRemove = {},
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}
