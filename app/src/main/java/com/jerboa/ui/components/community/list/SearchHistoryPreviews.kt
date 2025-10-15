package com.jerboa.ui.components.community.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jerboa.db.APP_SETTINGS_DEFAULT
import com.jerboa.ui.theme.JerboaTheme
import com.jerboa.ui.theme.MEDIUM_PADDING

/**
 * Full-screen preview showing how the search history UI integrates with the search bar.
 * This demonstrates the complete user experience.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchHistoryIntegrationPreview() {
    JerboaTheme(appSettings = APP_SETTINGS_DEFAULT) {
        var search by remember { mutableStateOf("") }
        var searchFocused by remember { mutableStateOf(true) }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column {
                // Simulated TopAppBar with search
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(MEDIUM_PADDING),
                ) {
                    Text(
                        text = "Community Search",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                
                // Simulated search bar
                CommunityTopBarSearchView(
                    search = search,
                    onSearchChange = { search = it },
                )
                
                // The search history dropdown
                SearchHistoryDropdown(
                    searchHistory = listOf(
                        "Android Development",
                        "Kotlin",
                        "Jetpack Compose",
                        "Material Design 3",
                        "MVVM Architecture",
                    ),
                    visible = searchFocused && search.isEmpty(),
                    onHistoryItemClick = { term ->
                        search = term
                        searchFocused = false
                    },
                    onClearHistory = {
                        // Clear history action
                    },
                    modifier = Modifier.padding(horizontal = MEDIUM_PADDING, vertical = 8.dp),
                )
                
                // Simulated content area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MEDIUM_PADDING),
                ) {
                    Text(
                        text = if (search.isEmpty()) {
                            "Search results will appear here..."
                        } else {
                            "Searching for: $search"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Preview showing the alternative inline chips design integrated with search
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchHistoryChipsIntegrationPreview() {
    JerboaTheme(appSettings = APP_SETTINGS_DEFAULT) {
        var search by remember { mutableStateOf("") }
        var searchFocused by remember { mutableStateOf(true) }
        var history by remember {
            mutableStateOf(
                listOf(
                    "Android",
                    "Kotlin Programming",
                    "Compose UI",
                    "Database",
                ),
            )
        }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column {
                // Simulated TopAppBar with search
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(MEDIUM_PADDING),
                ) {
                    Text(
                        text = "Community Search",
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                
                // Simulated search bar
                CommunityTopBarSearchView(
                    search = search,
                    onSearchChange = { search = it },
                )
                
                // The search history chips (alternative design)
                SearchHistoryChips(
                    searchHistory = history,
                    visible = searchFocused && search.isEmpty(),
                    onHistoryItemClick = { term ->
                        search = term
                        searchFocused = false
                    },
                    onRemoveItem = { term ->
                        history = history.filter { it != term }
                    },
                    onClearAll = {
                        history = emptyList()
                    },
                )
                
                // Simulated content area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(MEDIUM_PADDING),
                ) {
                    Text(
                        text = if (search.isEmpty()) {
                            "Search results will appear here..."
                        } else {
                            "Searching for: $search"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Side-by-side comparison of both designs
 */
@Preview(showBackground = true, widthDp = 800)
@Composable
fun SearchHistoryComparisonPreview() {
    JerboaTheme(appSettings = APP_SETTINGS_DEFAULT) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Design A: Dropdown
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Design A: Dropdown Card",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                SearchHistoryDropdown(
                    searchHistory = listOf(
                        "Android Dev",
                        "Kotlin",
                        "Compose",
                    ),
                    visible = true,
                    onHistoryItemClick = {},
                    onClearHistory = {},
                )
            }
            
            // Design B: Inline Chips
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "Design B: Inline List",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                SearchHistoryChips(
                    searchHistory = listOf(
                        "Android Dev",
                        "Kotlin",
                        "Compose",
                    ),
                    visible = true,
                    onHistoryItemClick = {},
                    onRemoveItem = {},
                    onClearAll = {},
                )
            }
        }
    }
}

/**
 * Dark theme preview
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SearchHistoryDarkThemePreview() {
    JerboaTheme(appSettings = APP_SETTINGS_DEFAULT.copy(theme = 2)) {
        var search by remember { mutableStateOf("") }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column {
                CommunityTopBarSearchView(
                    search = search,
                    onSearchChange = { search = it },
                )
                
                SearchHistoryDropdown(
                    searchHistory = listOf(
                        "Android Development",
                        "Kotlin",
                        "Jetpack Compose",
                    ),
                    visible = true,
                    onHistoryItemClick = {},
                    onClearHistory = {},
                    modifier = Modifier.padding(MEDIUM_PADDING),
                )
            }
        }
    }
}
