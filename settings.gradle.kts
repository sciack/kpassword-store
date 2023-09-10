val kotlinVersion = extra["kotlin.version"] as String

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        val composeVersion = extra["compose.version"] as String
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("jvm").version(kotlinVersion)
        id("org.jetbrains.compose").version(composeVersion)
    }
}


rootProject.name = "kpassword-store"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val coroutines = "1.7.3"
            val voyager = "1.0.0-rc07"
            val kotlin = "1.9.10"
            library(
                "kotlinx-coroutines-core",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-core"
            ).version(coroutines)
            library(
                "kotlinx-coroutines-swing",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-swing"
            ).version(coroutines)
            library("kotlinx-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.4.0")
// https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-core-jvm
            library(
                "kotlinx-serialization-core-jvm",
                "org.jetbrains.kotlinx",
                "kotlinx-serialization-core-jvm"
            ).version("1.5.1")


            library("kotlin-logging", "io.github.microutils:kotlin-logging-jvm:3.0.5")
            library("logback", "ch.qos.logback:logback-classic:1.4.7")
            library("slf4j-jul", "org.slf4j:jul-to-slf4j:2.0.5")
            library("jaspy", "org.jasypt:jasypt:1.9.3")
            library("commons-codec", "commons-codec:commons-codec:1.15")
            library("h2", "com.h2database:h2:2.2.222")
            library("flyway-core", "org.flywaydb:flyway-core:9.22.0")
            library("hikari", "com.zaxxer:HikariCP:5.0.1")
            library("kodein-compose", "org.kodein.di:kodein-di-framework-compose:7.19.0")
            library("commons-csv", "org.apache.commons:commons-csv:1.10.0")
            library("dataTable", "com.seanproctor:data-table-material3:0.5.1")


            library("voyager-navigator", "cafe.adriel.voyager", "voyager-navigator").version(voyager)
            library("voyager-kodein", "cafe.adriel.voyager", "voyager-kodein").version(voyager)

            bundle(
                "kotlin",
                listOf(
                    "kotlinx-coroutines-core",
                    "kotlinx-coroutines-swing",
                    "kotlinx-datetime",
                    "kotlinx-serialization-core-jvm"
                )
            )
            bundle("log", listOf("kotlin-logging", "logback", "slf4j-jul"))
            bundle("voyager", listOf("voyager-navigator", "voyager-kodein"))
            bundle("security", listOf("jaspy", "commons-codec"))
            bundle("db", listOf("h2", "flyway-core", "hikari"))


            library("test-mockk", "io.mockk:mockk:1.13.4")
            library("test-hamkrest", "com.natpryce:hamkrest:1.8.0.1")
            library("test-awaitility", "org.awaitility:awaitility-kotlin:4.2.0")
            library(
                "test-kotlinx-coroutines",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-test"
            ).version(coroutines)
            library(
                "test-kotlinx-coroutines-debug",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-debug"
            ).version(coroutines)
            library("test-javafaker", "com.github.javafaker:javafaker:1.0.2")
        }
    }
}