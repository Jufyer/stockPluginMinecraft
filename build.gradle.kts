import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
  id("xyz.jpenilla.run-paper") version "3.0.0"
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.0"
}

group = "org.jufyer.plugin"
version = "1.0.0-SNAPSHOT"
description = "Test plugin for paperweight-userdev"

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.jsoup:jsoup:1.17.2")
  implementation("org.json:json:20231013")
  implementation("org.htmlunit:htmlunit:4.17.0")
  implementation("org.eclipse.jgit:org.eclipse.jgit:6.10.0.202406032230-r")
  paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
}

tasks {
  compileJava {
    options.release.set(21)
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name()
  }

  // Fat-JAR erstellen
  jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
      configurations.runtimeClasspath.get()
        .filter { it.name.endsWith("jar") }
        .map { zipTree(it) }
    })
    // Entferne META-INF Signaturdateien
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    manifest {
      attributes["Main-Class"] = "org.jufyer.plugin.stock.Main"
    }
  }
}

// Plugin Yaml automatisch generieren
bukkitPluginYaml {
  main = "org.jufyer.plugin.stock.Main"
  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
  authors.add("Jufyer")
  apiVersion = "1.21.8"
  commands {
    register("stocks")
  }
}
