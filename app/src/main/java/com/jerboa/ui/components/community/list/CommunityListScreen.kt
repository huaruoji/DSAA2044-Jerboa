package com.jerboa.ui.components.community.list

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jerboa.JerboaAppState
import com.jerboa.JerboaApplication
import com.jerboa.api.ApiState
import com.jerboa.feat.BlurNSFW
import com.jerboa.model.CommunityListViewModel
import com.jerboa.ui.components.common.ApiEmptyText
import com.jerboa.ui.components.common.ApiErrorText
import com.jerboa.ui.components.common.LoadingBar
import it.vercruysse.lemmyapi.datatypes.CommunityFollowerView
import it.vercruysse.lemmyapi.datatypes.Search
import it.vercruysse.lemmyapi.dto.SearchType
import it.vercruysse.lemmyapi.dto.SortType
import kotlinx.coroutines.launch

object CommunityListReturn {
    const val COMMUNITY = "community-list::return(community)"
}

@Composable
fun CommunityListScreen(
    appState: JerboaAppState,
    selectMode: Boolean = false,
    followList: List<CommunityFollowerView>,
    blurNSFW: BlurNSFW,
    drawerState: DrawerState,
    showAvatar: Boolean,
    padding: PaddingValues? = null,
) {
    Log.d("jerboa", "got to community list screen")

    // Get the search history repository from the application container
    val context = LocalContext.current
    val searchHistoryRepository = (context.applicationContext as JerboaApplication).container.searchHistoryRepository

    val communityListViewModel: CommunityListViewModel =
        viewModel(
            factory = CommunityListViewModel.Companion.Factory(
                followList,
                searchHistoryRepository,
            ),
        )

    var search by rememberSaveable { mutableStateOf("") }
    var searchFocused by remember { mutableStateOf(false) }
    
    // Collect search history from the database
    val searchHistory by communityListViewModel.searchHistory.collectAsState(initial = emptyList())

    // Upon launch from process death
    LaunchedEffect(Unit) {
        if (search.isNotEmpty()) {
            communityListViewModel.searchCommunities(
                form =
                    Search(
                        q = search,
                        type_ = SearchType.Communities,
                        sort = SortType.TopAll,
                    ),
            )
        }
    }

    val scope = rememberCoroutineScope()

    val baseModifier = if (padding == null) {
        Modifier
    } else {
        // https://issuetracker.google.com/issues/249727298
        // Else it also applies the padding above the ime (keyboard)
        Modifier
            .padding(padding)
            .consumeWindowInsets(padding)
            .systemBarsPadding()
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = baseModifier,
            topBar = {
                CommunityListHeader(
                    openDrawer = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    search = search,
                    onSearchChange = {
                        search = it
                        communityListViewModel.searchCommunities(
                            form =
                                Search(
                                    q = search,
                                    type_ = SearchType.Communities,
                                    sort = SortType.TopAll,
                                ),
                        )
                    },
                    onSearchFocusChange = { isFocused ->
                        searchFocused = isFocused
                    }
                )
            },
            content = { padding ->
                Column {
                    // Search History Dropdown - shows when search bar is focused and empty
                    SearchHistoryDropdown(
                        searchHistory = searchHistory,
                        visible = searchFocused && search.isEmpty(),
                        onHistoryItemClick = { term ->
                            search = term
                            searchFocused = false
                            // Execute search with selected term
                            communityListViewModel.searchCommunities(
                                form = Search(
                                    q = term,
                                    type_ = SearchType.Communities,
                                    sort = SortType.TopAll,
                                ),
                            )
                        },
                        onClearHistory = {
                            // Clear all search history from database
                            communityListViewModel.clearSearchHistory()
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    // Original search results content
                    when (val communitiesRes = communityListViewModel.searchRes) {
                        ApiState.Empty -> ApiEmptyText()
                        is ApiState.Failure -> ApiErrorText(communitiesRes.msg)
                        ApiState.Loading -> {
                            LoadingBar(padding)
                        }

                        is ApiState.Success -> {
                            CommunityListings(
                                communities = communitiesRes.data.communities,
                                onClickCommunity = { cs ->
                                    if (selectMode) {
                                        appState.apply {
                                            addReturn(CommunityListReturn.COMMUNITY, cs)
                                            navigateUp()
                                        }
                                    } else {
                                        appState.toCommunity(id = cs.id)
                                    }
                                },
                                modifier =
                                    Modifier
                                        .padding(padding)
                                        .imePadding(),
                                blurNSFW = blurNSFW,
                                showAvatar = showAvatar,
                            )
                        }

                        else -> {}
                    }
                }
            },
        )
    }
}