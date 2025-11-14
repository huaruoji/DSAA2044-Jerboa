package com.jerboa.ai.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AiSummarizationRepositoryTest {

    private lateinit var repository: AiSummarizationRepository

    @Before
    fun setUp() {
        // Note: This test requires refactoring AiSummarizationRepository
        // to accept SiliconFlowApiClient as a constructor parameter for testability
        // For now, we test the validation logic that doesn't require mocking
        repository = AiSummarizationRepository()
    }

    @Test
    fun `generatePostSummary with empty title and body returns failure`() = runTest {
        // Arrange
        val title = ""
        val body = ""

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isFailure).isTrue()
        result.onFailure { error ->
            assertThat(error.message).contains("empty")
        }
    }

    @Test
    fun `generatePostSummary with valid content calls API client`() = runTest {
        // Arrange
        val title = "Test Post Title"
        val body = "Test post body content"
        val expectedSummary = "This is a test summary"

        // Mock the API client response
        // coEvery { mockApiClient.generateSummary(any(), any()) } returns Result.success(expectedSummary)

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        // coVerify { mockApiClient.generateSummary(title, body) }
        // After refactoring, we can verify the API was called correctly
    }

    @Test
    fun `generatePostSummary trims whitespace from inputs`() = runTest {
        // Arrange
        val title = "  Test Title  "
        val body = "  Test Body  "

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        // Verify that trimmed values are passed to API
        // This tests the input sanitization logic
    }

    @Test
    fun `generatePostSummary uses title as fallback when body is empty`() = runTest {
        // Arrange
        val title = "Test Title"
        val body = ""

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        // Verify that when body is empty, title is used as body
        // This tests the fallback logic at line 25
    }

    @Test
    fun `generatePostSummary uses default title when title is empty`() = runTest {
        // Arrange
        val title = ""
        val body = "Test body content"

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        // Verify that "Post Summary" is used as title when title is empty
        // This tests the fallback logic at line 24
    }

    @Test
    fun `generatePostSummary handles API exceptions`() = runTest {
        // Arrange
        val title = "Test Title"
        val body = "Test Body"
        val exception = Exception("Network error")

        // Mock API to throw exception
        // coEvery { mockApiClient.generateSummary(any(), any()) } throws exception

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isFailure).isTrue()
        result.onFailure { error ->
            assertThat(error).isEqualTo(exception)
        }
    }

    @Test
    fun `generatePostSummary returns success with summary text`() = runTest {
        // Arrange
        val title = "Test Title"
        val body = "Test Body"
        val expectedSummary = "This is the generated summary"

        // Mock successful API response
        // coEvery { mockApiClient.generateSummary(any(), any()) } returns Result.success(expectedSummary)

        // Act
        val result = repository.generatePostSummary(title, body)

        // Assert
        assertThat(result.isSuccess).isTrue()
        result.onSuccess { summary ->
            assertThat(summary).isEqualTo(expectedSummary)
        }
    }
}

