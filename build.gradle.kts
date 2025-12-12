import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
    id("xyz.jpenilla.run-paper") version "3.0.2" // Adds runServer and runMojangMappedServer tasks for testing
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.0" // Generates plugin.yml based on the Gradle config
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "org.jufyer.plugin"
version = "1.0.0-SNAPSHOT"
description = "Turn your Minecraft server into a living, breathing global commodities market powered by real-world price data from TradingEconomics."

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    gradlePluginPortal()
}

// For 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5:
/*
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

tasks.assemble {
  dependsOn(tasks.reobfJar)
}
 */

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
    // paperweight.foliaDevBundle("1.21.10-R0.1-SNAPSHOT")
    // paperweight.devBundle("com.example.paperfork", "1.21.10-R0.1-SNAPSHOT")

    // --- External Libraries ---
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.json:json:20231013")
    implementation("org.htmlunit:htmlunit:4.17.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.2.1.202505142326-r")
    implementation("javax.json:javax.json-api:1.1.4")
    implementation("org.glassfish:javax.json:1.1.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jfree:jfreechart:1.5.4")

    // --- API Dependencies ---
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

    // Only relevant for 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5:
    /*
    reobfJar {
      // This is an example of how you might change the output location for reobfJar. It's recommended not to do this
      // for a variety of reasons, however it's asked frequently enough that an example of how to do it is included here.
      outputJar = layout.buildDirectory.file("libs/PaperweightTestPlugin-${project.version}.jar")
    }
     */

    shadowJar {
        // Konfliktvermeidung bei eingebetteten Bibliotheken
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    build {
        dependsOn(shadowJar)
    }
}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.
bukkitPluginYaml {
    main = "org.jufyer.plugin.stock.Main"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.add("Author")
    apiVersion = "1.21.10"
}
