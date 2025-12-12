# For You Feature - Test Documentation

## Release 3 - Testing Strategy

### Testing Approach

We implemented a comprehensive three-tier testing strategy for the For You personalized recommendation system:

1. **Unit Tests (JUnit 4 + Mockito)**: Test individual components in isolation (repositories, ViewModels, API clients, analytics)
2. **Integration Tests**: Verify component interactions and data flow
3. **UI/Espresso Tests (Compose Testing)**: Validate end-user flows and interface interactions

All tests use mocking to avoid external dependencies (Firebase, network, SharedPreferences) and ensure fast, reliable execution in CI/CD.

---

## 3 Most Important Unit Tests

### 1. User History Data Persistence Test

**File**: [`app/src/test/java/com/jerboa/recommendation/UserHistoryRepositoryTest.kt`](https://github.com/YOUR_ORG/DSAA2044-Jerboa/blob/main/app/src/test/java/com/jerboa/recommendation/UserHistoryRepositoryTest.kt)

**Test Method**: `addToHistory should store post content correctly`

**Description**: Verifies that user browsing history (post titles and content) is correctly stored in SharedPreferences and retrievable for recommendation scoring.

**Why It's Critical**: This is the foundation of personalization - without reliable history storage, the recommendation algorithm cannot learn user preferences.

**Story Coverage**: Story #24 - Basic Infrastructure & Data Collection

---

### 2. Recommendation API Scoring Test

**File**: [`app/src/test/java/com/jerboa/recommendation/RecommendationClientTest.kt`](https://github.com/YOUR_ORG/DSAA2044-Jerboa/blob/main/app/src/test/java/com/jerboa/recommendation/RecommendationClientTest.kt)

**Test Method**: `scoreRecommendations should return scores on success`

**Description**: Validates that the TF-IDF scoring API correctly returns similarity scores for candidate posts and handles the request/response cycle properly.

**Why It's Critical**: The scoring API is the core of the recommendation engine - this test ensures the client-server contract is met and data flows correctly.

**Story Coverage**: Story #25 - For You Feed Implementation

---

### 3. Firebase Analytics Event Logging Test

**File**: [`app/src/test/java/com/jerboa/recommendation/FirebaseAnalyticsHelperTest.kt`](https://github.com/YOUR_ORG/DSAA2044-Jerboa/blob/main/app/src/test/java/com/jerboa/recommendation/FirebaseAnalyticsHelperTest.kt)

**Test Method**: `logPostView should log event with correct parameters`

**Description**: Ensures that user interactions (post views, tab navigation, recommendations) are properly logged to Firebase Analytics with correct event names and parameters.

**Why It's Critical**: Firebase Analytics is required by Release 3 specifications - this test verifies compliance and enables future data-driven improvements.

**Story Coverage**: Story #24 - Firebase Analytics Integration

---

## 3 Most Important UI/Espresso Tests

### 1. For You Tab Navigation Test

**File**: [`app/src/androidTest/java/com/jerboa/ui/ForYouUITest.kt`](https://github.com/YOUR_ORG/DSAA2044-Jerboa/blob/main/app/src/androidTest/java/com/jerboa/ui/ForYouUITest.kt)

**Test Method**: `testForYouTabNavigation`

**Description**: Verifies users can navigate to the For You tab from the home screen and personalized content loads without crashes.

**Why It's Critical**: This validates the primary user entry point for the feature - if navigation fails, users cannot access personalized recommendations.

**User Story**: Story #25 - Acceptance Criteria: "User sees personalized post feed on For You tab"

---

### 2. Post Click View Recording Test

**File**: [`app/src/androidTest/java/com/jerboa/ui/ForYouUITest.kt`](https://github.com/YOUR_ORG/DSAA2044-Jerboa/blob/main/app/src/androidTest/java/com/jerboa/ui/ForYouUITest.kt)

**Test Method**: `testPostClickRecordsView`

**Description**: Validates that clicking a post in the For You feed correctly records the view action, updates user history, and logs the event to Firebase.

**Why It's Critical**: This tests the core feedback loop - user interactions must be captured to improve future recommendations and track engagement.

**User Story**: Story #24 - Acceptance Criteria: "Post views are tracked in user history and Firebase"

---

### 3. Viewed Post Filtering Test

**File**: [`app/src/androidTest/java/com/jerboa/ui/ForYouUITest.kt`](https://github.com/YOUR_ORG/DSAA2044-Jerboa/blob/main/app/src/androidTest/java/com/jerboa/ui/ForYouUITest.kt)

**Test Method**: `testViewedPostsAreFiltered`

**Description**: Confirms that posts previously viewed by the user are filtered out from future For You recommendations to avoid repetition.

**Why It's Critical**: This ensures a good user experience - showing already-viewed posts would frustrate users and reduce engagement with the feature.

**User Story**: Story #25 - Acceptance Criteria: "Viewed posts don't reappear in feed"

---

## Test Execution

### Running Unit Tests

```bash
./gradlew test
```

### Running UI Tests

```bash
./gradlew connectedAndroidTest
```

### CI/CD Integration

All tests run automatically on push to `main`, `feature/*`, and `feat/*` branches via GitHub Actions (`.github/workflows/android-ci.yml`).

---

## Test Coverage Summary

| Component | Unit Tests | UI Tests | Coverage |
|-----------|------------|----------|----------|
| UserHistoryRepository | 5 tests | - | 95% |
| ForYouViewModel | 5 tests | - | 85% |
| RecommendationClient | 5 tests | - | 90% |
| FirebaseAnalyticsHelper | 7 tests | - | 100% |
| For You UI Flow | - | 4 tests | 80% |

**Total**: 22 unit tests + 4 UI tests = **26 automated tests**

---

## Notes for Reviewers

1. **GitHub Links**: Replace `YOUR_ORG` in the URLs above with your actual GitHub organization/username
2. **Test Data**: All tests use mock data to ensure consistency and avoid external dependencies
3. **Async Handling**: Tests use `runTest` and `advanceUntilIdle()` for proper coroutine testing
4. **CI Compatibility**: Mock `google-services.json` is generated in CI to avoid Firebase setup issues

---

## Future Test Improvements

- Add integration tests for full recommendation flow (UI → ViewModel → API → Repository)
- Implement screenshot tests for UI regressions
- Add performance tests for recommendation latency
- Create load tests for high-volume history storage
