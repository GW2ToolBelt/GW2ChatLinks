/*
 * Copyright (c) 2021-2024 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.targets.js.yarn.*
import org.jetbrains.kotlin.gradle.targets.jvm.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.dokkatoo.html)
//    alias(libs.plugins.dokkatoo.javadoc)
    alias(libs.plugins.kotlin.multiplatform)
    id("com.gw2tb.maven-publish-conventions")
}

yarn.lockFileName = "kotlin-yarn.lock"
yarn.lockFileDirectory = rootDir

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                apiVersion = KotlinVersion.KOTLIN_1_9
                languageVersion = KotlinVersion.KOTLIN_1_9
            }
        }
    }

    js {
        browser()
        nodejs()
    }

    jvm {
        withJava()

        compilations.configureEach {
            compilerOptions.options.jvmTarget = JvmTarget.JVM_11
        }
    }

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    linuxArm64()
    linuxX64()

    iosArm64()
    iosX64()

    iosSimulatorArm64()

    macosArm64()
    macosX64()

    mingwX64()

    tvosArm64()
    tvosX64()

    tvosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                enabled = !Os.isFamily(Os.FAMILY_MAC)
            }
        }

        nodejs {
            testTask {
                enabled = !Os.isFamily(Os.FAMILY_WINDOWS)
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs {
            testTask {
                enabled = !Os.isFamily(Os.FAMILY_WINDOWS)
            }
        }
    }

    watchosArm32()
    watchosArm64()
    watchosX64()

    watchosDeviceArm64()
    watchosSimulatorArm64()

    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    targets.filter { it is KotlinJvmTarget || it is KotlinWithJavaTarget<*, *> }.forEach { target ->
        tasks.named<Jar>(target.artifactsTaskName) {
            manifest {
                attributes(mapOf(
                    "Name" to project.name,
                    "Specification-Version" to project.version,
                    "Specification-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>"
                ))
            }
        }
    }
}

dokkatoo {
    dokkatooSourceSets.configureEach {
        reportUndocumented = true
        skipEmptyPackages = true
        jdkVersion = 11

        val localKotlinSourceDir = layout.projectDirectory.dir("src/$name/kotlin")
        val version = project.version

        sourceLink {
            localDirectory = localKotlinSourceDir

            remoteUrl("https://github.com/GW2ToolBelt/GW2ChatLinks/tree/v${version}/src/main/kotlin")
            remoteLineSuffix = "#L"
        }
    }

    dokkatooPublications.configureEach {
        moduleName = "GW2ChatLinks"

        // TODO Remaining warnings are silly atm. Reevaluate this flag in the future.
//        failOnWarning = true
    }

    versions {
        jetbrainsDokka = libs.versions.dokka
    }
}

configure<NodeJsRootExtension> {
    // We need canary builds of Node + V8 but there are none for Windows.
    if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
        nodeVersion = "21.0.0-v8-canary202309143a48826a08"
        nodeDownloadBaseUrl = "https://nodejs.org/download/v8-canary"
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.javaModuleVersion = "$version"
        options.release = 11
    }

    named<JavaCompile>("compileJava") {
        options.compilerArgumentProviders += object : CommandLineArgumentProvider {

            @InputFiles
            @PathSensitive(PathSensitivity.RELATIVE)
            val kotlinClasses = this@tasks.named<KotlinCompile>("compileKotlinJvm").flatMap(KotlinCompile::destinationDirectory)

            override fun asArguments() = listOf(
                "--patch-module",
                "com.gw2tb.gw2chatlinks=${kotlinClasses.get().asFile.absolutePath}"
            )

        }
    }

    withType<Jar>().configureEach {
        archiveBaseName = "gw2chatlinks"

        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        includeEmptyDirs = false
    }

    withType<KotlinNpmInstallTask>().configureEach {
        args += "--ignore-engines"
    }

    dokkatooGeneratePublicationHtml {
        outputDirectory = layout.projectDirectory.dir("docs/site/api")
    }
}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            val emptyJavadocJar = tasks.register<Jar>("${name}JavadocJar") {
                archiveBaseName = "${archiveBaseName.get()}-${name}"
                archiveClassifier = "javadoc"
            }

            artifact(emptyJavadocJar)
        }
    }
}