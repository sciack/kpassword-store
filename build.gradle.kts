import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.compose") version "1.4.1"
    id("com.adarshr.test-logger") version "3.2.0"
}

group = "com.github.sciack"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}


testlogger {
    theme = MOCHA
    slowThreshold = 5000
    logLevel = LogLevel.WARN
}


dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.2")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation(group = "ch.qos.logback", name = "logback-classic", version = "1.4.7")
    implementation("org.jasypt:jasypt:1.9.3")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.h2database:h2:2.2.220")
    implementation("org.flywaydb:flyway-core:9.21.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.kodein.di:kodein-di-framework-compose:7.19.0")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("reflect"))
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "kpassword-store"
            packageVersion = "1.0.0"
        }
    }
}
