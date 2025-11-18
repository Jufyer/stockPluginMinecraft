pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
  }
}

plugins {
  // Toolchain Resolver ist wichtig für Java 21+ Auto-Provisioning
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "jufyer-stock-plugin"