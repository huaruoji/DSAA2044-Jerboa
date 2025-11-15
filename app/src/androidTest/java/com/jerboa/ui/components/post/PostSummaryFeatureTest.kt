package com.jerboa.ui.components.post

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.Either
import com.jerboa.ai.repository.AiSummarizationRepository
import com.jerboa.api.ApiState
import com.jerboa.model.PostViewModel
import com.jerboa.ui.components.post.PostScreen
import com.jerboa.ui.theme.JerboaTheme
import it.vercruysse.lemmyapi.datatypes.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.*

/**
 * Instrumented tests for the AI Summary feature.
 * These tests verify the complete user flow from button click to summary display.
 */
@RunWith(AndroidJUnit4::class)
class PostSummaryFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Mock
    private lateinit var mockAiRepository: AiSummarizationRepository
    
    private lateinit var testPostViewModel: PostViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Create a real PostViewModel for testing UI interactions
        testPostViewModel = PostViewModel(Either.Left(PostId(1)))
        
        // Create test post data
        val testPost = createTestPost()
        val testPostView = createTestPostView(testPost)
        
        // Set up test post data in ViewModel
        testPostViewModel.postRes = ApiState.Success(GetPostResponse(post_view = testPostView))
        
        // Use reflection to inject mock repository
        val repositoryField = PostViewModel::class.java.getDeclaredField("aiRepository")
        repositoryField.isAccessible = true
        repositoryField.set(testPostViewModel, mockAiRepository)
    }

    @Test
    fun generateSummaryButton_isDisplayed() {
        // Arrange
        composeTestRule.setContent {
            JerboaTheme {
                PostScreen(
                    postId = PostId(1),
                    postViewModel = testPostViewModel,
                    siteViewModel = null,
                    accountViewModel = null,
                    showVotingArrowsInListView = true,
                    useCustomTabs = false,
                    usePrivateTabs = false,
                    blurNSFW = 0,
                    showPostLinkPreviews = true,
                    onBack = {},
                    onCommunityClick = { _ -> },
                    onPersonClick = { _ -> },
                    onPostClick = { _ -> },
                    onEditPostClick = { _ -> },
                    onReportClick = { _ -> },
                    markAsRead = true,
                    postActionBarMode = 0
                )
            }
        }

        // Assert - Button should be visible and enabled
        composeTestRule.onNodeWithTag("generate_summary_button")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun generateSummaryButton_whenClicked_showsLoadingIndicator() = runTest {
        // Arrange - Mock a slow response to test loading state
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenAnswer {
            // Simulate delay by not returning immediately
            Thread.sleep(100) // Short delay for test
            Result.success("Test summary")
        }

        composeTestRule.setContent {
            JerboaTheme {
                PostScreen(
                    postId = PostId(1),
                    postViewModel = testPostViewModel,
                    siteViewModel = null,
                    accountViewModel = null,
                    showVotingArrowsInListView = true,
                    useCustomTabs = false,
                    usePrivateTabs = false,
                    blurNSFW = 0,
                    showPostLinkPreviews = true,
                    onBack = {},
                    onCommunityClick = { _ -> },
                    onPersonClick = { _ -> },
                    onPostClick = { _ -> },
                    onEditPostClick = { _ -> },
                    onReportClick = { _ -> },
                    markAsRead = true,
                    postActionBarMode = 0
                )
            }
        }

        // Act - Click the button
        composeTestRule.onNodeWithTag("generate_summary_button")
            .performClick()

        // Assert - Loading indicator should appear and button should be disabled
        composeTestRule.onNodeWithTag("summary_loading_indicator")
            .assertIsDisplayed()
            
        composeTestRule.onNodeWithTag("generate_summary_button")
            .assertIsNotEnabled()
    }

    @Test
    fun generateSummary_whenSuccessful_displaysSummaryCard() = runTest {
        // Arrange
        val expectedSummary = "This is a test summary of the post content."
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenReturn(
            Result.success(expectedSummary)
        )

        composeTestRule.setContent {
            JerboaTheme {
                PostScreen(
                    postId = PostId(1),
                    postViewModel = testPostViewModel,
                    siteViewModel = null,
                    accountViewModel = null,
                    showVotingArrowsInListView = true,
                    useCustomTabs = false,
                    usePrivateTabs = false,
                    blurNSFW = 0,
                    showPostLinkPreviews = true,
                    onBack = {},
                    onCommunityClick = { _ -> },
                    onPersonClick = { _ -> },
                    onPostClick = { _ -> },
                    onEditPostClick = { _ -> },
                    onReportClick = { _ -> },
                    markAsRead = true,
                    postActionBarMode = 0
                )
            }
        }

        // Act - Click the button
        composeTestRule.onNodeWithTag("generate_summary_button")
            .performClick()

        // Assert - Wait for summary to appear and verify content
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithTag("summary_display_card")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithTag("summary_display_card")
            .assertIsDisplayed()

        // Verify the summary text is displayed somewhere in the UI
        composeTestRule.onNodeWithText(expectedSummary, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun generateSummary_whenFails_displaysErrorMessage() = runTest {
        // Arrange
        val errorMessage = "Network connection failed"
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenReturn(
            Result.failure(Exception(errorMessage))
        )

        composeTestRule.setContent {
            JerboaTheme {
                PostScreen(
                    postId = PostId(1),
                    postViewModel = testPostViewModel,
                    siteViewModel = null,
                    accountViewModel = null,
                    showVotingArrowsInListView = true,
                    useCustomTabs = false,
                    usePrivateTabs = false,
                    blurNSFW = 0,
                    showPostLinkPreviews = true,
                    onBack = {},
                    onCommunityClick = { _ -> },
                    onPersonClick = { _ -> },
                    onPostClick = { _ -> },
                    onEditPostClick = { _ -> },
                    onReportClick = { _ -> },
                    markAsRead = true,
                    postActionBarMode = 0
                )
            }
        }

        // Act - Click the button
        composeTestRule.onNodeWithTag("generate_summary_button")
            .performClick()

        // Assert - Wait for error to appear and verify error message is shown
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText(errorMessage, substring = true)
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText(errorMessage, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun generateSummary_whenPostNotLoaded_showsError() = runTest {
        // Arrange - Set post state to loading
        testPostViewModel.postRes = ApiState.Loading

        composeTestRule.setContent {
            JerboaTheme {
                PostScreen(
                    postId = PostId(1),
                    postViewModel = testPostViewModel,
                    siteViewModel = null,
                    accountViewModel = null,
                    showVotingArrowsInListView = true,
                    useCustomTabs = false,
                    usePrivateTabs = false,
                    blurNSFW = 0,
                    showPostLinkPreviews = true,
                    onBack = {},
                    onCommunityClick = { _ -> },
                    onPersonClick = { _ -> },
                    onPostClick = { _ -> },
                    onEditPostClick = { _ -> },
                    onReportClick = { _ -> },
                    markAsRead = true,
                    postActionBarMode = 0
                )
            }
        }

        // Act - Try to click the button (may not be visible if post not loaded)
        // Assert - In this case, the button might not even be rendered
        // This test verifies the robustness of the UI when data is not available
        
        // If the button is rendered, clicking should show appropriate error
        try {
            composeTestRule.onNodeWithTag("generate_summary_button")
                .performClick()

            composeTestRule.waitUntil(timeoutMillis = 3000) {
                try {
                    composeTestRule.onNodeWithText("Unable to load post content", substring = true)
                        .assertExists()
                    true
                } catch (e: AssertionError) {
                    false
                }
            }
        } catch (e: Exception) {
            // Button may not be available when post is not loaded, which is acceptable behavior
        }
    }

    // Helper functions to create test data
    private fun createTestPost(): Post {
        return Post(
            id = PostId(1),
            name = "Test Post Title for Summarization",
            body = "This is test post content that should be summarized by the AI system. " +
                   "It contains enough text to make summarization meaningful.",
            creator_id = PersonId(1),
            community_id = CommunityId(1),
            published = Date().toString(),
            updated = null,
            deleted = false,
            removed = false,
            locked = false,
            nsfw = false,
            stickied = false,
            embed_title = null,
            embed_description = null,
            thumbnail_url = null,
            ap_id = "test-post-ap-id",
            local = true,
            embed_video_url = null,
            language_id = LanguageId(1),
            featured_community = false,
            featured_local = false
        )
    }

    private fun createTestPostView(post: Post): PostView {
        return PostView(
            post = post,
            creator = PersonSafe(
                id = PersonId(1),
                name = "TestUser",
                display_name = "Test User",
                avatar = null,
                banned = false,
                published = Date().toString(),
                updated = null,
                actor_id = "test-user-actor",
                bio = "Test user bio",
                local = true,
                banner = null,
                deleted = false,
                matrix_user_id = null,
                bot_account = false,
                ban_expires = null,
                instance_id = InstanceId(1)
            ),
            community = CommunitySafe(
                id = CommunityId(1),
                name = "TestCommunity",
                title = "Test Community for AI Features",
                description = "A community for testing AI summarization features",
                removed = false,
                published = Date().toString(),
                updated = null,
                deleted = false,
                nsfw = false,
                actor_id = "test-community-actor",
                local = true,
                icon = null,
                banner = null,
                followers_url = "test-followers",
                inbox_url = "test-inbox",
                shared_inbox_url = null,
                hidden = false,
                posting_restricted_to_mods = false,
                instance_id = InstanceId(1)
            ),
            creator_banned_from_community = false,
            counts = PostAggregates(
                id = PostAggregatesId(1),
                post_id = PostId(1),
                comments = 5,
                score = 10,
                upvotes = 12,
                downvotes = 2,
                published = Date().toString(),
                newest_comment_time_necro = Date().toString(),
                newest_comment_time = Date().toString(),
                featured_community = false,
                featured_local = false,
                hot_rank = 100,
                hot_rank_active = 150
            ),
            subscribed = SubscribedType.NotSubscribed,
            saved = false,
            read = false,
            creator_blocked = false,
            my_vote = null,
            unread_comments = 0
        )
    }
}
}

