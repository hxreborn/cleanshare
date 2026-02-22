plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "eu.hxreborn.cleanshare"
    compileSdk = 36

    defaultConfig {
        applicationId = "eu.hxreborn.cleanshare"
        minSdk = 30
        targetSdk = 36
        versionCode = 201
        versionName = "2.0.1"
    }

    signingConfigs {
        create("release") {
            fun secret(name: String): String? =
                providers.gradleProperty(name).orElse(providers.environmentVariable(name)).orNull

            val storeFilePath = secret("RELEASE_STORE_FILE")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = secret("RELEASE_STORE_PASSWORD")
                keyAlias = secret("RELEASE_KEY_ALIAS")
                keyPassword = secret("RELEASE_KEY_PASSWORD")
                storeType = secret("RELEASE_STORE_TYPE") ?: "PKCS12"
            } else {
                logger.warn("RELEASE_STORE_FILE not found. Release signing is disabled.")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            pickFirsts += "META-INF/xposed/*"
            excludes += setOf(
                "META-INF/androidx/**",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json",
                "META-INF/version-control-info.textproto",
                "META-INF/*.version",
                "kotlin/**",
            )
        }
    }

    androidResources {
        localeFilters += "en"
    }

    lint {
        abortOnError = true
        disable.addAll(listOf("PrivateApi", "DiscouragedPrivateApi"))
        ignoreTestSources = true
    }
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    version.set("1.8.0")
    android.set(true)
    ignoreFailures.set(false)
}

// Disable baseline/art profile tasks
project.tasks.configureEach {
    if (name.contains("ArtProfile", ignoreCase = true)) {
        enabled = false
    }
}

dependencies {
    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)

    implementation(libs.core.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.compose.preference)
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose)
    implementation(libs.splashscreen)
    implementation(libs.libsu.core)
    implementation(libs.libsu.io)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
}
