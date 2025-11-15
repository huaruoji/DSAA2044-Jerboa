# Testing Guide for AI Summary Feature

This document describes the testing strategy and implementation for the AI Summary feature (Release 2).

## Overview

We have implemented comprehensive tests following the Lab 5 PPT guidelines:
- **Unit Tests**: Test business logic in ViewModel and Repository layers
- **Instrumented Tests**: Test UI interactions and user flows

## Test Structure

### Unit Tests (`app/src/test/java/`)

#### 1. `PostViewModelTest.kt`
Tests the ViewModel logic for summary generation:
- ✅ Verifies error handling when post data is not available
- ✅ Tests loading state management
- ✅ Validates error state clearing
- ✅ Tests summary visibility state

**Note**: To fully test the async behavior with mocked repository, `PostViewModel` should be refactored to accept `AiSummarizationRepository` as a constructor parameter (dependency injection).

#### 2. `AiSummarizationRepositoryTest.kt`
Tests the repository layer:
- ✅ Validates input validation (empty title/body)
- ✅ Tests input sanitization (trimming whitespace)
- ✅ Verifies fallback logic (using title when body is empty)
- ✅ Tests exception handling

**Note**: To fully test API interactions, `AiSummarizationRepository` should accept `SiliconFlowApiClient` as a constructor parameter for dependency injection.

### Instrumented Tests (`app/src/androidTest/java/`)

#### `PostSummaryFeatureTest.kt`
UI tests using Compose Test framework:
- ✅ Button visibility and interaction
- ✅ Loading indicator display
- ✅ Summary card display after successful generation
- ✅ Error card display on failure
- ✅ Error handling when post is not loaded

**Note**: These tests require proper setup with mocked ViewModels and navigation. The current implementation provides the test structure and can be completed once the test infrastructure is in place.

## Test Tags Added to UI

To enable UI testing, we've added test tags to key components:

- `generate_summary_button`: The "Generate Summary" button
- `summary_loading_indicator`: Loading spinner
- `summary_display_card`: Summary card container
- `summary_card_content`: Summary text content
- `summary_error_card`: Error message card

## Running Tests

### Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Run Instrumented Tests
```bash
./gradlew connectedDebugAndroidTest
```

### Run All Tests
```bash
./gradlew test connectedCheck
```

## CI/CD Integration

The CI workflow (`.github/workflows/android-ci.yml`) is already configured to:
1. Run unit tests on every push/PR
2. Run instrumented tests on an emulator
3. Only build release artifacts if tests pass

## Refactoring Recommendations

For better testability, consider these refactorings:

### 1. Dependency Injection for PostViewModel
```kotlin
class PostViewModel(
    val id: Either<PostId, CommentId>,
    private val aiRepository: AiSummarizationRepository = AiSummarizationRepository(),
) : ViewModel() {
    // ...
}
```

### 2. Dependency Injection for AiSummarizationRepository
```kotlin
class AiSummarizationRepository(
    private val apiClient: SiliconFlowApiClient = SiliconFlowApiClient(),
) {
    // ...
}
```

This allows easy mocking in tests:
```kotlin
val mockRepository = mock<AiSummarizationRepository>()
val viewModel = PostViewModel(id, mockRepository)
```

## Test Coverage Goals

- [x] ViewModel state management
- [x] Repository input validation
- [x] Error handling paths
- [x] UI component visibility
- [ ] Full async flow with mocked dependencies (requires refactoring)
- [ ] End-to-end user flow (requires test infrastructure setup)

## Next Steps

1. **Refactor for testability**: Add dependency injection to ViewModel and Repository
2. **Complete UI tests**: Set up test infrastructure with mocked ViewModels
3. **Add more edge cases**: Test network failures, timeout scenarios
4. **Performance tests**: Verify summary generation doesn't block UI
5. **Accessibility tests**: Ensure summary is accessible to screen readers

## References

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

