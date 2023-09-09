import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.compose") version "1.5.1"
    id("com.adarshr.test-logger") version "3.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.2"
    id("com.github.jmongard.git-semver-plugin") version "0.4.2"
}

group = "com.github.sciack"
version = semver.infoVersion

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


koverReport {
    defaults {
        xml {
            onCheck = true
            this.setReportFile(layout.buildDirectory.file("reports/jacoco/report.xml"))
        }
    }
}

tasks {
    val jacocoTestReport = register("jacocoTestReport") {

    }
    jacocoTestReport.get().dependsOn(koverXmlReport)
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
// https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-core-jvm
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.5.1")


    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("org.jasypt:jasypt:1.9.3")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.h2database:h2:2.2.222")
    implementation("org.flywaydb:flyway-core:9.22.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.kodein.di:kodein-di-framework-compose:7.19.0")
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("org.slf4j:jul-to-slf4j:2.0.5")
    implementation("com.seanproctor:data-table-material3:0.5.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
    testImplementation(compose.desktop.uiTestJUnit4)
    testImplementation(compose.desktop.currentOs)
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.7.3")
    testImplementation("com.github.javafaker:javafaker:1.0.2")

}


tasks.test {
    //useJUnitPlatform()
}

tasks.releaseVersion {
    setNoCommit(false)
    setNoTag(false)
}

compose.desktop {

    application {
        mainClass = "passwordStore.MainKt"
        jvmArgs += listOf("-Dkpassword-store.mode=PROD")

        nativeDistributions {
            targetFormats(TargetFormat.Rpm, TargetFormat.Msi)
            packageName = "kpassword-store"
            modules("java.sql", "java.desktop", "java.naming")
            packageVersion = semver.version.substringBefore('-')
            licenseFile.set(File("LICENSE"))
            vendor = "Mirko Sciachero <m.sciachero@gmail.com>"
            this.description = """
                Program to manage and store credentials, similar to keepass but simpler \
                Implemented in Kotlin and JCompose
            """.trimIndent()



            linux {
                menuGroup = "Password Store"
                rpmLicenseType = "UNLICENSE"
                iconFile.set(File("src/main/resources/icons/lockoverlay.png"))
                this.shortcut = true
            }
            windows {
                upgradeUuid = "89c4e09f-40e5-4542-9396-934cca615a63"
                menuGroup = "Password Store"
                vendor = "Mirko Sciachero"
                console = false
                iconFile.set(File("lockoverlay.ico"))
            }
        }
    }
}
