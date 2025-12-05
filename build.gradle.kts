import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.21"
    application
}

group = "de.zordid"
version = "1.0-SNAPSHOT"

val kotest = "6.0.5"
val junit = "6.0.1"
val arrow = "2.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.github.ajalt.mordant:mordant:3.0.2")

    implementation("io.arrow-kt:arrow-core:$arrow")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrow")

//    implementation("org.jetbrains.kotlinx:multik-core:0.2.3")
//    implementation("org.jetbrains.kotlinx:multik-default:0.2.3")

//    implementation("org.choco-solver:choco:4.10.14")

    implementation("guru.nidi:graphviz-kotlin:0.18.1")
    implementation("org.slf4j:slf4j-nop:2.0.17")
//    implementation("ch.qos.logback:logback-classic:1.5.21")

    implementation("com.toldoven.aoc:aoc-kotlin-notebook:1.1.2")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junit")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotest")
    testImplementation("io.kotest:kotest-runner-junit5:$kotest")
    testImplementation("io.kotest:kotest-property:$kotest")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

java.sourceCompatibility = JavaVersion.VERSION_21

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xconsistent-data-class-copy-visibility",
            "-Xwhen-guards",
            "-Xmulti-dollar-interpolation",
        )
    }
}

application {
    mainClass.set("AdventOfCodeKt")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}

kotlin {
    jvmToolchain(21)
}
