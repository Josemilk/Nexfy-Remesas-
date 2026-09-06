pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    // ── OsmAnd Precompiled Binaries ──
    ivy {
      name = "OsmAndBinariesIvy"
      url = uri("https://builder.osmand.net")
      patternLayout {
        artifact("ivy/[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]")
      }
      metadataSources { artifact() }
      content {
        includeGroup("net.osmand")
        includeGroup("net.osmand.shared")
      }
    }

    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
  }
}

rootProject.name = "NexFy Remesas"

include(":app")
