pluginManagement {
    repositories {
        // CI 环境优先使用官方源（速度更快）
        // 本地开发可以通过环境变量切换到 Aliyun 镜像
        val useAliyunMirror = System.getenv("CI") != "true"
        
        if (useAliyunMirror) {
            // 使用阿里云镜像加速（国内）
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        }
        
        // 官方源
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        val useAliyunMirror = System.getenv("CI") != "true"
        
        if (useAliyunMirror) {
            // 使用阿里云镜像加速（国内）
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
        }
        
        maven { url = uri("https://jitpack.io") }
        // 官方源
        google()
        mavenCentral()
    }
}
rootProject.name = "jerboa"
include(":app")
include(":benchmarks")
