<div align="center">

![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/huaruoji/DSAA2044-Jerboa)
[![CI](https://github.com/huaruoji/DSAA2044-Jerboa/actions/workflows/android-ci.yml/badge.svg)](https://github.com/huaruoji/DSAA2044-Jerboa/actions)
[![GitHub issues](https://img.shields.io/github/issues-raw/huaruoji/DSAA2044-Jerboa.svg)](https://github.com/huaruoji/DSAA2044-Jerboa/issues)
[![License](https://img.shields.io/github/license/huaruoji/DSAA2044-Jerboa.svg)](LICENSE)

</div>

<p align="center">
  <a href="https://github.com/huaruoji/DSAA2044-Jerboa" rel="noopener">
  <img width=200px height=200px src="https://raw.githubusercontent.com/LemmyNet/jerboa/main/app/src/main/res/jerboa.svg"></a>

  <h3 align="center">Jerboa - AI-Powered Edition</h3>
  <p align="center">
    An intelligent Lemmy client with AI-powered features and personalized content recommendations
    <br />
    <i>DSAA2044 Course Project - HKUST(GZ)</i>
    <br />
    <br />
    <a href="https://github.com/huaruoji/DSAA2044-Jerboa/issues">Report Bug</a>
    ·
    <a href="https://github.com/huaruoji/DSAA2044-Jerboa/issues">Request Feature</a>
    ·
    <a href="https://github.com/huaruoji/DSAA2044-Jerboa/releases">Releases</a>
  </p>
</p>

## About This Project

This is an enhanced version of Jerboa developed as part of the **DSAA2044: Data Science Project** course at HKUST(GZ). The project extends the original Lemmy client with advanced AI and data science features.

### 🎯 Course Project Information

- **Course**: DSAA2044 - Data Science Project
- **Institution**: Hong Kong University of Science and Technology (Guangzhou)
- **Team**: Jerboa-Team 5
- **Members**: 
  - Yunuo WANG ([@huaruoji](https://github.com/huaruoji))
  - Guanqian ZENG ([@gzeng260-labixiaoqian](https://github.com/gzeng260-labixiaoqian))

### ✨ Enhanced Features

#### 🤖 AI-Powered Reading Assistance
- **Post Summarization**: One-click AI-generated summaries for long posts using LLM APIs
- **Comment Analysis**: Intelligent analysis of comment sections to identify key discussion points, agreements, and disagreements
- **Smart Insights**: Quickly understand complex discussions without reading every comment

#### 📊 Personalized Content Recommendation
- **For You Feed**: ML-powered personalized content recommendations based on your reading history
- **TF-IDF Algorithm**: Content-based filtering using scikit-learn (10k features, 1-3 grams)
- **Real-time Scoring**: Python Flask backend provides instant similarity scoring
- **Smart Filtering**: Automatically filters out already-viewed posts to show fresh content

#### 📈 Analytics & Insights
- **Firebase Analytics**: Tracks user behavior and engagement metrics
- **Custom Events**: Monitors post views, tab navigation, and recommendation performance
- **Data-Driven**: Continuously improves recommendations based on user interactions

#### 💾 Enhanced User Experience
- **Search History**: Quick access to your last 5 search queries with Room database persistence
- **FIFO Queue Management**: Intelligent history management with automatic cleanup
- **Offline Support**: Local caching for seamless experience

### 📱 Screenshots

| For You Feed                                                                   | AI Summary                                                          |
| -------------------------------------------------------------------------- | ------------------------------------------------------------------------ |
| ![img_1](./fastlane/metadata/android/en-US/images/phoneScreenshots/01.png) | ![img_2](fastlane/metadata/android/en-US/images/phoneScreenshots/02.png) |

### 🏗️ Architecture

**Frontend (Android)**
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM pattern with ViewModel + Repository layers
- **Networking**: Retrofit 2.11.0 + OkHttp
- **Database**: Room for local persistence
- **Analytics**: Firebase Analytics BOM 33.7.0

**Backend (Python)**
- **Framework**: Flask REST API
- **ML Model**: scikit-learn TF-IDF Vectorizer
- **Training Data**: Reddit dataset (200k posts)
- **Model Size**: ~5MB pickled vectorizer

### 🧪 Testing & CI/CD

- **Unit Tests**: 11 tests (JUnit + Mockito)
- **UI Tests**: 4 instrumented tests (Compose Testing)
- **CI/CD**: GitHub Actions with automatic APK builds
- **Code Quality**: Automated testing on every push

## About Jerboa

Jerboa is an Android client for Lemmy, built using the native Android toolkit and Jetpack Compose. This enhanced version maintains full compatibility with the Lemmy ecosystem while adding powerful AI and ML features.

**Original Project**: This project is based on [Jerboa by LemmyNet](https://github.com/LemmyNet/jerboa), licensed under AGPL-3.0.

### Built With

- [Android Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern declarative UI
- [Kotlin](https://kotlinlang.org/) - Primary programming language
- [Retrofit](https://square.github.io/retrofit/) - Type-safe HTTP client
- [Room](https://developer.android.com/training/data-storage/room) - Local database
- [Firebase Analytics](https://firebase.google.com/docs/analytics) - User behavior tracking
- [scikit-learn](https://scikit-learn.org/) - Machine learning (TF-IDF)
- [Flask](https://flask.palletsprojects.com/) - Python backend API

## 🚀 Getting Started

### Prerequisites

- Android Studio (latest version)
- JDK 17
- Python 3.8+ (for recommendation backend)
- Git

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/huaruoji/DSAA2044-Jerboa.git
   cd DSAA2044-Jerboa
   ```

2. **Set up Firebase** (Optional, for analytics)
   - Create a Firebase project
   - Download `google-services.json` 
   - Place it in `app/` directory
   - Or use the auto-generated mock config in CI

3. **Build the Android app**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run the recommendation backend** (Optional)
   ```bash
   cd recommendation-system
   pip install -r requirements.txt
   python app.py
   ```

5. **Run tests**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

## 📦 Releases

- [Latest Release (v3.0.0)](https://github.com/huaruoji/DSAA2044-Jerboa/releases/tag/v3.0.0)
- [All Releases](https://github.com/huaruoji/DSAA2044-Jerboa/releases)

### Release History

- **v3.0.0** (2025-12-12): For You personalized feed with TF-IDF recommendations
- **v2.0.0**: AI-powered post summarization and comment analysis
- **v1.0.0**: Search history feature with Room database

## 🧪 Development Process

### Agile Methodology

This project followed Agile/Scrum practices across 6 sprints:

- **Sprint 1**: CI/CD Pipeline Setup
- **Sprint 2** (Release 1): Search History Feature
- **Sprint 3**: AI Summary UI Prototype
- **Sprint 4** (Release 2): LLM API Integration
- **Sprint 5**: Recommendation System Infrastructure
- **Sprint 6** (Release 3): For You Feed Implementation

### Project Milestones

- [Sprint 1](https://github.com/huaruoji/DSAA2044-Jerboa/milestone/1) - 1 story, 2 points
- [Sprint 2](https://github.com/huaruoji/DSAA2044-Jerboa/milestone/2) - 1 story, 3 points
- [Sprint 3](https://github.com/huaruoji/DSAA2044-Jerboa/milestone/3) - 1 story, 3 points
- [Sprint 4](https://github.com/huaruoji/DSAA2044-Jerboa/milestone/4) - 2 stories, 13 points
- [Sprint 5](https://github.com/huaruoji/DSAA2044-Jerboa/milestone/5) - 1 story, 8 points
- [Sprint 6](https://github.com/huaruoji/DSAA2044-Jerboa/milestone/6) - 2 stories, 7 points

**Total**: 8 user stories, 36 story points

## 📄 Documentation

- [Release 3 Summary](documents/release3.md) - Comprehensive project documentation
- [Test Documentation](documents/Sprint%202/TEST_DOCUMENTATION.md) - Testing strategy and coverage
- [Sprint 2 Integration](documents/Sprint%202/INTEGRATION_COMPLETE.md) - Search history implementation

## Features

- Open-source, [AGPL License](/LICENSE)
- Full Lemmy API compatibility
- AI-powered reading assistance
- Personalized content recommendations
- Analytics and insights

## Installation / Releases

- [Releases](https://github.com/LemmyNet/jerboa/releases)
- [IzzyOnDroid](https://apt.izzysoft.de/fdroid/index/apk/com.jerboa)
- [F-Droid](https://f-droid.org/en/packages/com.jerboa/)
- [Google Play](https://play.google.com/store/apps/details?id=com.jerboa)

## Support / Donate

Jerboa is made by Lemmy's developers, and is free, open-source software, meaning no advertising, monetizing, or venture capital, ever. Your donations directly support full-time development of the project.

Jerboa and Lemmy are made possible by a generous grant from the [NLnet foundation](https://nlnet.nl/).

- [Support on Liberapay](https://liberapay.com/Lemmy).
- [Support on Ko-fi](https://ko-fi.com/lemmynet).
- [Support on OpenCollective](https://opencollective.com/lemmy).
- [Support on Patreon](https://www.patreon.com/dessalines).

### Crypto

- bitcoin: `1Hefs7miXS5ff5Ck5xvmjKjXf5242KzRtK`
- ethereum: `0x400c96c96acbC6E7B3B43B1dc1BB446540a88A01`
- monero: `41taVyY6e1xApqKyMVDRVxJ76sPkfZhALLTjRvVKpaAh2pBd4wv9RgYj1tSPrx8wc6iE1uWUfjtQdTmTy2FGMeChGVKPQuV`

## Contact

- [GitHub Issues](https://github.com/huaruoji/DSAA2044-Jerboa/issues)
- Project Team:
  - [@huaruoji](https://github.com/huaruoji)
  - [@gzeng260-labixiaoqian](https://github.com/gzeng260-labixiaoqian)

## 🙏 Acknowledgments

- **Original Project**: [Jerboa by LemmyNet](https://github.com/LemmyNet/jerboa) - Base Android Lemmy client
- **Course**: DSAA2044 Mobile Application Development at HKUST(GZ)
- **Dataset**: [Reddit Data on Kaggle](https://www.kaggle.com/datasets/prakharrathi25/reddit-data-huge) for ML training
- **Icons**: Made by [Freepik](https://www.freepik.com) from [www.flaticon.com](https://www.flaticon.com)

## 📝 License

This project inherits the AGPL-3.0 license from the original Jerboa project. See [LICENSE](LICENSE) for details.

---

**Note**: This is a course project and not affiliated with the official Lemmy or Jerboa development teams. For the official Jerboa app, visit [github.com/LemmyNet/jerboa](https://github.com/LemmyNet/jerboa).
