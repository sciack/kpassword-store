import com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA
import org.gradle.internal.impldep.com.google.api.client.json.Json
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.com.google.gson.Gson

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.adarshr.test-logger") version "3.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.2"
    id("com.github.jmongard.git-semver-plugin") version "0.9.0"
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
    val jacocoTestReport by register("jacocoTestReport") {
        dependsOn(koverXmlReport)
    }

    val versionTask = register("writeVersion") {

        doLast {
            val versionObj = mapOf("version" to semver.version)

            val versionJson = Gson().toJson(versionObj)
            logger.warn("Version: $versionJson")
            val generatedResourceDir = sourceSets.main.get().output.resourcesDir!!
            if (!generatedResourceDir.exists()) {
                generatedResourceDir.mkdirs()
            }
            logger.warn("GeneratedResourceDir: $generatedResourceDir")
            val versionFile = generatedResourceDir.resolve("version.json")
            versionFile.writeText(versionJson)
        }
    }

    releaseVersion {
        setNoCommit(false)
        setNoTag(false)
    }

    processResources {
        dependsOn(versionTask)
    }
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(compose.material3)
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.voyager)
    implementation(libs.bundles.log)
    implementation(libs.bundles.security)
    implementation(libs.bundles.db)
    implementation(libs.commons.csv)
    implementation(libs.kodein.compose)
    implementation(libs.dataTable)

    testImplementation(compose.desktop.uiTestJUnit4)

    testImplementation(kotlin("test-junit"))
    testImplementation(kotlin("reflect"))
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.hamkrest)
    testImplementation(libs.test.awaitility)
    testImplementation(libs.test.kotlinx.coroutines)
    testImplementation(libs.test.kotlinx.coroutines.debug)
    testImplementation(libs.test.javafaker)

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
                console = true
                iconFile.set(File("lockoverlay.ico"))
            }
        }
    }
}
