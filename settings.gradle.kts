pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal {
            content {
                includeGroup("io.github.libxposed")
            }
        }
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.topjohnwu.libsu")
            }
        }
    }
    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "CleanShare"

include(":app")
