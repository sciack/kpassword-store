import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.compose") version "1.4.1"
    id("com.adarshr.test-logger") version "3.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.2"
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
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("org.jasypt:jasypt:1.9.3")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.h2database:h2:2.1.214")
    implementation("org.flywaydb:flyway-core:9.21.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.kodein.di:kodein-di-framework-compose:7.19.0")

    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("reflect"))
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    testImplementation(compose("org.jetbrains.compose.ui:ui-test-junit4"))
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.6.4")
    testImplementation("com.github.javafaker:javafaker:1.0.2")

}

tasks.test {
    //useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "passwordStore.MainKt"

        nativeDistributions {
            targetFormats( TargetFormat.Rpm, TargetFormat.Msi)
            packageName = "kpassword-store"
            packageVersion = "1.0.0"
        }
    }
}
