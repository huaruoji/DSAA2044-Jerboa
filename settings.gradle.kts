pluginManagement {
    repositories {
        // 优先使用官方源下载插件（确保最新版本可用）
        gradlePluginPortal()
        google()
        mavenCentral()
        // 使用阿里云镜像加速（国内）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    }
}
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        // 使用阿里云镜像加速（国内）
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://jitpack.io") }
        // 保留官方源作为备份
        google()
        mavenCentral()
    }
}
rootProject.name = "jerboa"
include(":app")
include(":benchmarks")
