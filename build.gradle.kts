import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
  `java-library`
  // Paperweight 2.0 ist PFLICHT für neuere MC Versionen, benötigt aber Gradle 9
  id("io.papermc.paperweight.userdev") version "2.0.0-SNAPSHOT"
  id("xyz.jpenilla.run-paper") version "2.3.0"
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.0"
  // Shadow Plugin für das saubere Einbinden von Libraries (HTMLUnit, JSoup etc.)
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.jufyer.plugin"
version = "1.0.0-SNAPSHOT"
description = "Test plugin for paperweight-userdev"

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
  gradlePluginPortal()
}

dependencies {
  // Deine Libraries
  implementation("org.jsoup:jsoup:1.17.2")
  implementation("org.json:json:20231013")
  implementation("org.htmlunit:htmlunit:4.17.0")
  implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
  implementation("javax.json:javax.json-api:1.1.4")
  implementation("org.glassfish:javax.json:1.1.4")
  implementation("com.google.code.gson:gson:2.10.1")
  implementation("org.jfree:jfreechart:1.5.4")

  // Hier die gewünschte 1.21.8
  paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
}

tasks {
  compileJava {
    options.release.set(21)
    options.encoding = Charsets.UTF_8.name()
  }

  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }

  // Shadow Jar ersetzt deinen manuellen "jar" Block
  shadowJar {
    // Exclude META-INF Signaturen, damit das Jar gültig bleibt
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    // Optional: Dependencies verlagern (relocate), falls es Konflikte gibt
    // relocate("org.jsoup", "org.jufyer.plugin.libs.jsoup")
  }

  build {
    dependsOn(shadowJar)
  }
}

bukkitPluginYaml {
  main = "org.jufyer.plugin.stock.Main"
  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
  authors.add("Jufyer")
  apiVersion = "1.21" // API Version ist meist nur Major.Minor (also 1.21)
  commands {
    register("stocks")
  }
}