pluginManagement {
    val localProxy = providers.gradleProperty("soniclab.repoProxy").orNull
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" ->
                    useModule("com.android.tools.build:gradle:${requested.version}")
                "org.jetbrains.kotlin.android" ->
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                "org.jetbrains.kotlin.plugin.compose" ->
                    useModule("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${requested.version}")
            }
        }
    }
    repositories {
        if (localProxy != null) {
            maven { url = uri("$localProxy/unified"); isAllowInsecureProtocol = true }
        } else {
            google()
            mavenCentral()
        }
    }
}

dependencyResolutionManagement {
    val localProxy = providers.gradleProperty("soniclab.repoProxy").orNull
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (localProxy != null) {
            maven { url = uri("$localProxy/unified"); isAllowInsecureProtocol = true }
        } else {
            google()
            mavenCentral()
        }
    }
}

rootProject.name = "SonicLab3D"
include(":app")
