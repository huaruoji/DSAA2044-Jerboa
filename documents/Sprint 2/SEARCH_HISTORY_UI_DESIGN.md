# Search History UI Design Documentation

## Overview
This document describes the UI design for the Search History Feature (#4) in the Jerboa app. The design provides users with quick access to their recent search queries when using the community search functionality.

## Design Components

### 1. SearchHistoryDropdown Component
**Location:** `app/src/main/java/com/jerboa/ui/components/community/list/SearchHistoryUI.kt`

This is the primary UI component that displays search history as a Material 3 Card dropdown below the search bar.

#### Features:
- **Animated appearance**: Uses fade and expand animations when shown/hidden
- **Maximum 5 items**: Displays only the 5 most recent search terms
- **Material 3 design**: Follows current app design system
- **Clear history action**: Prominent "Clear All" button in the header
- **Click to search**: Clicking any history item executes a new search

#### Visual Structure:
```
┌─────────────────────────────────────────┐
│ 🕐 Search History        [Clear All]    │
├─────────────────────────────────────────┤
│ 🕐 Android Development                  │
│ 🕐 Kotlin                               │
│ 🕐 Jetpack Compose                      │
│ 🕐 Material Design 3                    │
│ 🕐 Search History Feature               │
└─────────────────────────────────────────┘
```

#### Parameters:
- `searchHistory: List<String>` - List of search terms
- `visible: Boolean` - Controls visibility
- `onHistoryItemClick: (String) -> Unit` - Callback for item selection
- `onClearHistory: () -> Unit` - Callback for clearing history
- `modifier: Modifier` - Optional modifier

### 2. SearchHistoryChips Component (Alternative Design)
This is an alternative, more compact design that can be used if the dropdown feels too heavy.

#### Features:
- **Inline display**: Shows directly below search bar
- **Individual removal**: Each item has its own remove button
- **Compact layout**: Takes less vertical space
- **Simple list view**: No card elevation

#### Visual Structure:
```
Recent Searches                  [🗙 Clear]
─────────────────────────────────────────
🕐 Android                              ✕
🕐 Kotlin Programming                   ✕
🕐 Compose UI                           ✕
```

#### Parameters:
- `searchHistory: List<String>` - List of search terms
- `visible: Boolean` - Controls visibility
- `onHistoryItemClick: (String) -> Unit` - Callback for item selection
- `onRemoveItem: (String) -> Unit` - Callback for removing individual items
- `onClearAll: () -> Unit` - Callback for clearing all history
- `modifier: Modifier` - Optional modifier

## String Resources
Added to `app/src/main/res/values/strings.xml`:

```xml
<string name="search_history_title">Search History</string>
<string name="search_history_recent">Recent Searches</string>
<string name="search_history_clear">Clear All</string>
<string name="search_history_remove_item">Remove search item</string>
<string name="search_history_empty">No search history</string>
```

## Integration Guidelines

### Recommended Usage Pattern

The search history UI should be integrated into `CommunityListScreen.kt` as follows:

```kotlin
// In CommunityListScreen composable
var searchFocused by remember { mutableStateOf(false) }
val searchHistory by viewModel.searchHistory.collectAsState()

Scaffold(
    topBar = {
        CommunityListHeader(
            openDrawer = { /* ... */ },
            search = search,
            onSearchChange = { /* ... */ },
            onSearchFocused = { searchFocused = true },
            onSearchUnfocused = { searchFocused = false },
        )
    },
    content = { padding ->
        Column {
            // Show search history when search bar is focused and search is empty
            SearchHistoryDropdown(
                searchHistory = searchHistory,
                visible = searchFocused && search.isEmpty(),
                onHistoryItemClick = { term ->
                    search = term
                    // Trigger search with the selected term
                    communityListViewModel.searchCommunities(
                        form = Search(
                            q = term,
                            type_ = SearchType.Communities,
                            sort = SortType.TopAll,
                        )
                    )
                },
                onClearHistory = {
                    communityListViewModel.clearSearchHistory()
                },
                modifier = Modifier.padding(horizontal = MEDIUM_PADDING)
            )
            
            // Rest of the content...
        }
    }
)
```

### When to Show Search History
1. **User clicks on search bar** - Search history becomes visible
2. **Search bar is focused AND empty** - Shows history dropdown
3. **User starts typing** - History dropdown hides, search results appear
4. **User clicks outside** - History dropdown hides

### User Interactions

#### Click on History Item:
1. Populate search bar with the selected term
2. Execute search with that term
3. Hide the history dropdown
4. Save this term at the top of history (make it most recent)

#### Click Clear All:
1. Show confirmation dialog (optional)
2. Clear all search history from database
3. Hide the history dropdown
4. Show toast message: "Search history cleared"

#### Click Remove Item (Alternative Design):
1. Remove only that specific item from history
2. Update the UI immediately
3. If no items left, hide the dropdown

## Design Decisions

### Why Dropdown Design?
- **Non-intrusive**: Appears only when needed (search bar focused)
- **Familiar pattern**: Users expect this behavior from other apps (Google, YouTube, etc.)
- **Clean separation**: Clearly distinguishes history from search results
- **Material Design**: Uses Card component with proper elevation

### Why Limit to 5 Items?
- **Performance**: Reduces database queries and rendering overhead
- **Relevance**: Recent searches are most useful
- **Screen space**: Prevents overwhelming the UI
- **Industry standard**: Most apps show 5-10 recent items

### Icon Choice
- **History icon**: Universal symbol for recent/past items
- **Clear icon**: Standard "X" or "Clear All" text
- **Consistent sizing**: 20dp for list items, matching app standards

## Accessibility Considerations

1. **Content Descriptions**: All icons have proper content descriptions
2. **Touch Targets**: All clickable areas meet minimum 48dp touch target size
3. **Text Contrast**: Uses theme colors for proper contrast ratios
4. **Screen Readers**: History items are properly announced
5. **Keyboard Navigation**: Fully navigable with keyboard (if applicable)

## Preview Functions

The file includes several preview composables for design validation:
- `SearchHistoryDropdownPreview` - Shows dropdown with 5 items
- `SearchHistoryDropdownEmptyPreview` - Shows behavior with empty list
- `SearchHistoryChipsPreview` - Alternative design preview
- `SearchHistoryItemPreview` - Individual item preview
- `SearchHistoryListItemPreview` - Item with remove button preview

## Next Steps for Implementation

This UI design is ready to be integrated once the following backend tasks are completed:

1. **Database implementation** (Task by @huaruoji)
   - Room database schema for search history
   - DAO methods for CRUD operations

2. **Save search logic** (Task by @gzeng260-labixiaoqian)
   - Implement saving search terms when user performs search
   - Handle duplicates (move to top vs. ignore)

3. **Retrieve search logic** (Task by @gzeng260-labixiaoqian)
   - Fetch last 5 search terms from database
   - Expose as StateFlow in ViewModel

4. **Clear history logic** (Task by @gzeng260-labixiaoqian)
   - Implement clear all functionality
   - Implement remove individual item (if using alternative design)

5. **Unit tests** (Task by @huaruoji)
   - Test database operations
   - Test ViewModel search history methods

## Files Created/Modified

### Created:
- `app/src/main/java/com/jerboa/ui/components/community/list/SearchHistoryUI.kt`

### Modified:
- `app/src/main/res/values/strings.xml` - Added 5 new string resources

## Design Variations

Two complete designs are provided:

### Design A: Dropdown Card (Recommended)
- More polished appearance
- Better visual separation
- Follows Material Design patterns
- Recommended for primary implementation

### Design B: Inline List
- More compact
- Allows individual item removal
- Takes less visual space
- Good alternative if space is constrained

The team can choose which design fits better with the overall app aesthetic.

## Screenshots

Preview functions can be run in Android Studio to see the designs:
1. Open `SearchHistoryUI.kt`
2. Click on any `@Preview` function
3. View in the Preview pane (Design or Interactive mode)

## Code Quality

- ✅ Follows Jetpack Compose best practices
- ✅ Uses Material 3 components
- ✅ Properly animated transitions
- ✅ Type-safe with Kotlin
- ✅ Documented with KDoc comments
- ✅ Includes preview functions
- ✅ Accessible with content descriptions
- ✅ Follows existing app code style

## Questions for Team Discussion

1. Should we show a confirmation dialog before clearing all history?
2. Should we limit character length for displayed search terms?
3. Should we show a "No search history" empty state?
4. Do we want to add a timestamp (e.g., "2 hours ago")?
5. Should clicking a history item automatically hide the keyboard?

## References

- Material Design 3 Guidelines: https://m3.material.io/
- Compose Animation Documentation: https://developer.android.com/jetpack/compose/animation
- Existing app patterns in: `app/src/main/java/com/jerboa/ui/components/community/list/CommunityList.kt`
