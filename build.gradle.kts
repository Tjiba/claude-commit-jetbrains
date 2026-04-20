plugins {
    id("java")
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.serialization") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "fr.tjiba"
version = "0.1.8"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "Claude Commit Message"
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2024.3.7")
        instrumentationTools()
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    testImplementation(kotlin("test"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("243.7")
        untilBuild.set(provider { null })
    }

    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }

    test {
        useJUnitPlatform()
    }
}

