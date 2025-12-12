package com.jerboa.ui.components.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jerboa.JerboaAppState
import com.jerboa.PostViewMode
import com.jerboa.datatypes.BanFromCommunityData
import com.jerboa.datatypes.PostFeatureData
import com.jerboa.db.entity.Account
import com.jerboa.feat.BlurNSFW
import com.jerboa.feat.PostActionBarMode
import com.jerboa.feat.SwipeToActionPreset
import com.jerboa.feat.default
import com.jerboa.model.ForYouViewModel
import com.jerboa.rememberJerboaAppState
import com.jerboa.ui.components.post.PostListings
import it.vercruysse.lemmyapi.datatypes.Community
import it.vercruysse.lemmyapi.datatypes.LocalUserVoteDisplayMode
import it.vercruysse.lemmyapi.datatypes.Person
import it.vercruysse.lemmyapi.datatypes.PersonId
import it.vercruysse.lemmyapi.datatypes.PostId
import it.vercruysse.lemmyapi.datatypes.PostView

/**
 * For You personalized feed screen.
 * Displays Lemmy posts ranked by similarity to user's reading history.
 */
@Composable
fun ForYouFeedScreen(
    viewModel: ForYouViewModel,
    modifier: Modifier = Modifier,
    account: Account,
    postViewMode: PostViewMode,
    showVotingArrowsInListView: Boolean,
    enableDownVotes: Boolean,
    showAvatar: Boolean,
    useCustomTabs: Boolean,
    usePrivateTabs: Boolean,
    blurNSFW: BlurNSFW,
    showPostLinkPreviews: Boolean,
    postActionBarMode: PostActionBarMode,
    swipeToActionPreset: SwipeToActionPreset,
    onUpvoteClick: (PostView) -> Unit,
    onDownvoteClick: (PostView) -> Unit,
    onPostClick: (PostView) -> Unit,
    onSaveClick: (PostView) -> Unit,
    onReplyClick: (PostView) -> Unit,
    onCommunityClick: (Community) -> Unit,
    onPersonClick: (PersonId) -> Unit,
    onEditPostClick: (PostView) -> Unit,
    onDeletePostClick: (PostView) -> Unit,
    onHidePostClick: (PostView) -> Unit,
    onReportClick: (PostView) -> Unit,
    onRemoveClick: (PostView) -> Unit,
    onBanPersonClick: (Person) -> Unit,
    onBanFromCommunityClick: (BanFromCommunityData) -> Unit,
    onLockPostClick: (PostView) -> Unit,
    onFeaturePostClick: (PostFeatureData) -> Unit,
    onViewPostVotesClick: (PostId) -> Unit,
    onMarkAsRead: (PostView) -> Unit,
    appState: JerboaAppState,
    listState: LazyListState = rememberLazyListState(),
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Button(onClick = { viewModel.loadRecommendations() }) {
                        Text(text = "Retry")
                    }
                }
            }
        }

        uiState.posts.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No posts available",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Browse Global/Local tabs to build your personalized feed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        else -> {
            Column(modifier = modifier.fillMaxSize()) {
                // Algorithm info header
                if (uiState.algorithm != null) {
                    Text(
                        text = if (uiState.isUsingHistory) {
                            "📊 Personalized with ${uiState.algorithm?.uppercase()} • Based on your browsing history"
                        } else {
                            "📰 Showing Active posts • Browse to get personalized recommendations"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (uiState.isUsingHistory) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Use standard PostListings component for Lemmy posts
                PostListings(
                    posts = uiState.posts,
                    admins = emptyList(),
                    moderators = null,
                    contentAboveListings = {},
                    onUpvoteClick = onUpvoteClick,
                    onDownvoteClick = onDownvoteClick,
                    onPostClick = onPostClick,
                    onSaveClick = onSaveClick,
                    onReplyClick = onReplyClick,
                    onEditPostClick = onEditPostClick,
                    onDeletePostClick = onDeletePostClick,
                    onHidePostClick = onHidePostClick,
                    onReportClick = onReportClick,
                    onRemoveClick = onRemoveClick,
                    onBanPersonClick = onBanPersonClick,
                    onBanFromCommunityClick = onBanFromCommunityClick,
                    onLockPostClick = onLockPostClick,
                    onFeaturePostClick = onFeaturePostClick,
                    onViewPostVotesClick = onViewPostVotesClick,
                    onCommunityClick = onCommunityClick,
                    onPersonClick = onPersonClick,
                    loadMorePosts = {},
                    account = account,
                    showCommunityName = true,
                    listState = listState,
                    postViewMode = postViewMode,
                    showVotingArrowsInListView = showVotingArrowsInListView,
                    enableDownVotes = enableDownVotes,
                    showAvatar = showAvatar,
                    useCustomTabs = useCustomTabs,
                    usePrivateTabs = usePrivateTabs,
                    blurNSFW = blurNSFW,
                    showPostLinkPreviews = showPostLinkPreviews,
                    appState = appState,
                    markAsReadOnScroll = false,
                    onMarkAsRead = onMarkAsRead,
                    showIfRead = true,
                    voteDisplayMode = LocalUserVoteDisplayMode.default(),
                    postActionBarMode = postActionBarMode,
                    showPostAppendRetry = false,
                    swipeToActionPreset = swipeToActionPreset,
                    disableVideoAutoplay = false,
                )
            }
        }
    }
}

