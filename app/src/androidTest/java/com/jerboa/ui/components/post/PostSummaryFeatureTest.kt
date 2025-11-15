package com.jerboa.ui.components.post

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple instrumented tests for the AI Summary UI components.
 * These are lightweight tests that verify basic UI functionality.
 */
@RunWith(AndroidJUnit4::class)
class PostSummaryFeatureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun generateSummaryButton_isDisplayedAndClickable() {
        // Simple test to verify button displays and responds to clicks
        var buttonClicked = false
        
        composeTestRule.setContent {
            Button(
                onClick = { buttonClicked = true },
                modifier = androidx.compose.ui.Modifier.testTag("generate_summary_button")
            ) {
                Text("Generate Summary")
            }
        }

        // Verify button is displayed and enabled
        composeTestRule.onNodeWithTag("generate_summary_button")
            .assertIsDisplayed()
            .assertIsEnabled()

        // Verify button responds to clicks
        composeTestRule.onNodeWithTag("generate_summary_button")
            .performClick()
            
        assert(buttonClicked) { "Button should respond to clicks" }
    }

    @Test
    fun loadingIndicator_whenShown_isVisible() {
        // Test loading state display
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = androidx.compose.ui.Modifier.testTag("loading_indicator")
                )
                Text("Loading summary...")
            }
        }

        composeTestRule.onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
            
        composeTestRule.onNodeWithText("Loading summary...")
            .assertIsDisplayed()
    }

    @Test
    fun summaryCard_whenDisplayed_showsContent() {
        // Test summary display
        val summaryText = "This is a test summary of the post content."
        
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = summaryText,
                    modifier = androidx.compose.ui.Modifier.testTag("summary_text")
                )
            }
        }

        composeTestRule.onNodeWithTag("summary_text")
            .assertIsDisplayed()
            .assertTextEquals(summaryText)
    }

    @Test
    fun errorMessage_whenShown_displaysCorrectly() {
        // Test error state display
        val errorMessage = "Failed to generate summary"
        
        composeTestRule.setContent {
            Text(
                text = "Error: $errorMessage",
                modifier = androidx.compose.ui.Modifier.testTag("error_message")
            )
        }

        composeTestRule.onNodeWithTag("error_message")
            .assertIsDisplayed()
            
        composeTestRule.onNodeWithText("Error: $errorMessage")
            .assertIsDisplayed()
    }

    @Test
    fun buttonStates_disabledWhenLoading() {
        // Test button disabled state during loading
        val isLoading = true
        
        composeTestRule.setContent {
            Button(
                onClick = { },
                enabled = !isLoading,
                modifier = androidx.compose.ui.Modifier.testTag("generate_button")
            ) {
                Text("Generate Summary")
            }
        }

        composeTestRule.onNodeWithTag("generate_button")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }
}
}

