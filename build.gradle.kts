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

dependencies {
    intellijPlatform {
        create("IC", "2024.3")
        instrumentationTools()
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    testImplementation(kotlin("test"))
}

tasks {
    buildSearchableOptions {
        enabled = false
    }

    patchPluginXml {
        sinceBuild.set("243")
        untilBuild.set("")
    }

    signPlugin {
        certificateChain.set("")
        privateKey.set("")
        password.set("")
    }

    publishPlugin {
        token.set("")
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

