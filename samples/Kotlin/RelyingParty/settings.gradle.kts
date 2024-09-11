pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {
            url = uri("https://maven.pkg.github.com/daoninc/sdk-packages/")
            credentials {
                username = "GITHUB_USER"
                password = "GITHUB_TOKEN"
            }
        }
        maven(url = "$rootDir/repository")
        maven(url = "https://nexus.identityx-build.com/repository/sdk-maven/")
        gradlePluginPortal()
    }
}
rootProject.name = "RelyingParty"
include (":app")
