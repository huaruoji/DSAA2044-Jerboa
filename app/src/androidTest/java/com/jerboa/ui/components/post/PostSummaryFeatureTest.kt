package com.jerboa.ui.components.post

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import arrow.core.Either
import com.jerboa.ai.repository.AiSummarizationRepository
import com.jerboa.api.ApiState
import com.jerboa.model.PostViewModel
import com.jerboa.ui.theme.JerboaTheme
import com.jerboa.datatypes.samplePost
import it.vercruysse.lemmyapi.datatypes.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

/**
 * Simplified instrumented tests for the AI Summary feature.
 * These tests focus on testing the ViewModel interaction with UI components.
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
        
        // Set up test post data using existing sample data
        testPostViewModel.postRes = ApiState.Success(
            GetPostResponse(post_view = samplePost)
        )
        
        // Use reflection to inject mock repository
        val repositoryField = PostViewModel::class.java.getDeclaredField("aiRepository")
        repositoryField.isAccessible = true
        repositoryField.set(testPostViewModel, mockAiRepository)
    }

    @Test
    fun generateSummaryButton_whenDisplayed_isEnabledAndClickable() {
        // Test basic button functionality without full PostScreen complexity
        composeTestRule.setContent {
            JerboaTheme {
                Button(
                    onClick = { testPostViewModel.onGenerateSummaryClicked() },
                    enabled = !testPostViewModel.isLoadingSummary,
                    modifier = androidx.compose.ui.Modifier.testTag("generate_summary_button")
                ) {
                    Text("Generate Summary")
                }
            }
        }

        // Assert - Button should be visible and enabled
        composeTestRule.onNodeWithTag("generate_summary_button")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun generateSummary_whenClicked_updatesViewModelState() = runTest {
        // Arrange - Mock successful response
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenReturn(
            Result.success("Test summary content")
        )

        var buttonClicked = false
        
        composeTestRule.setContent {
            JerboaTheme {
                Button(
                    onClick = { 
                        buttonClicked = true
                        testPostViewModel.onGenerateSummaryClicked() 
                    },
                    enabled = !testPostViewModel.isLoadingSummary,
                    modifier = androidx.compose.ui.Modifier.testTag("generate_summary_button")
                ) {
                    Text("Generate Summary")
                }
            }
        }

        // Act - Click the button
        composeTestRule.onNodeWithTag("generate_summary_button")
            .performClick()

        // Assert - Button was clicked and ViewModel method was called
        assert(buttonClicked) { "Button click should be registered" }
        
        // Give some time for the coroutine to complete
        composeTestRule.waitForIdle()
        
        // Verify repository was called
        verify(mockAiRepository, timeout(2000)).generatePostSummary(any(), any())
    }

    @Test
    fun loadingState_whenSummaryGenerating_showsCorrectly() = runTest {
        // Arrange - Mock slow response
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenAnswer {
            Thread.sleep(200)
            Result.success("Summary")
        }
        
        composeTestRule.setContent {
            JerboaTheme {
                androidx.compose.foundation.layout.Column {
                    Button(
                        onClick = { testPostViewModel.onGenerateSummaryClicked() },
                        enabled = !testPostViewModel.isLoadingSummary,
                        modifier = androidx.compose.ui.Modifier.testTag("generate_summary_button")
                    ) {
                        Text("Generate Summary")
                    }
                    
                    if (testPostViewModel.isLoadingSummary) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = androidx.compose.ui.Modifier.testTag("loading_indicator")
                        )
                    }
                }
            }
        }

        // Act - Click button to start loading
        composeTestRule.onNodeWithTag("generate_summary_button")
            .performClick()

        // Assert - Loading indicator should appear
        composeTestRule.onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
            
        // Button should be disabled while loading
        composeTestRule.onNodeWithTag("generate_summary_button")
            .assertIsNotEnabled()
    }

    @Test
    fun errorHandling_whenRepositoryFails_updatesErrorState() = runTest {
        // Arrange - Mock error response
        val errorMessage = "Network error"
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenReturn(
            Result.failure(Exception(errorMessage))
        )
        
        composeTestRule.setContent {
            JerboaTheme {
                androidx.compose.foundation.layout.Column {
                    Button(
                        onClick = { testPostViewModel.onGenerateSummaryClicked() },
                        modifier = androidx.compose.ui.Modifier.testTag("generate_summary_button")
                    ) {
                        Text("Generate Summary")
                    }
                    
                    testPostViewModel.summaryError?.let { error ->
                        Text(
                            text = error,
                            modifier = androidx.compose.ui.Modifier.testTag("error_text")
                        )
                    }
                }
            }
        }

        // Act - Click button
        composeTestRule.onNodeWithTag("generate_summary_button")
            .performClick()

        // Wait for async operation
        composeTestRule.waitForIdle()
        
        // Assert - Error message should be displayed
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule.onNodeWithTag("error_text")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        composeTestRule.onNodeWithTag("error_text")
            .assertIsDisplayed()
    }

    @Test
    fun successFlow_whenSummaryGenerated_showsSummary() = runTest {
        // Arrange
        val summaryText = "This is the generated summary content."
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenReturn(
            Result.success(summaryText)
        )
        
        composeTestRule.setContent {
            JerboaTheme {
                androidx.compose.foundation.layout.Column {
                    Button(
                        onClick = { testPostViewModel.onGenerateSummaryClicked() },
                        modifier = androidx.compose.ui.Modifier.testTag("generate_summary_button")
                    ) {
                        Text("Generate Summary")
                    }
                    
                    if (testPostViewModel.isSummaryVisible) {
                        Text(
                            text = testPostViewModel.summaryText,
                            modifier = androidx.compose.ui.Modifier.testTag("summary_text")
                        )
                    }
                }
            }
        }

        // Act - Click button
        composeTestRule.onNodeWithTag("generate_summary_button")
            .performClick()

        // Wait for async operation
        composeTestRule.waitForIdle()
        
        // Assert - Summary should be displayed
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule.onNodeWithTag("summary_text")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        composeTestRule.onNodeWithTag("summary_text")
            .assertIsDisplayed()
            .assertTextEquals(summaryText)
    }
}
}

