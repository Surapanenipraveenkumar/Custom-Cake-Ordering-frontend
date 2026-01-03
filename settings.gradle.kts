// This block configures where Gradle looks for the plugins themselves
// (like the Android Gradle Plugin and the Kotlin plugin).
pluginManagement {
    repositories {
        // We list the standard repositories without any restrictive filters.
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// This block configures where Gradle looks for your app's library dependencies
// (like Retrofit, Glide, Material Components, etc.).
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Standard repositories for app dependencies.
        google()
        mavenCentral()
        // If you ever use a library from a different repository, like JitPack,
        // you would add it here. For example:
        // maven { url = uri("https://jitpack.io") }
    }
}

// Sets the name of your root project.
rootProject.name = "cakeordering"

// Includes the ':app' module in your project build, which is your main application module.
include(":app")
