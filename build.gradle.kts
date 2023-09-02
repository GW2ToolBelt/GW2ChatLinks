/*
 * Copyright (c) 2021-2023 Leon Linhart
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
import com.gw2tb.build.tasks.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.yarn.*
import org.jetbrains.kotlin.gradle.targets.jvm.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binary.compatibility.validator)
    id("com.gw2tb.maven-publish-conventions")
}

yarn.lockFileName = "kotlin-yarn.lock"
yarn.lockFileDirectory = rootDir

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    explicitApi()

    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                apiVersion.set(KotlinVersion.KOTLIN_1_8)
                languageVersion.set(KotlinVersion.KOTLIN_1_8)
                apiVersion.set(KotlinVersion.KOTLIN_1_9)
                languageVersion.set(KotlinVersion.KOTLIN_1_9)
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.default()

    js(IR) {
        browser()
        nodejs()
    }

    jvm {
        compilations.configureEach {
            compilerOptions.options.jvmTarget.set(JvmTarget.JVM_1_8)
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
        val artifactTask = tasks.getByName<Jar>(target.artifactsTaskName) {
            manifest {
                attributes(mapOf(
                    "Name" to project.name,
                    "Specification-Version" to project.version,
                    "Specification-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                    "Multi-Release" to "true"
                ))
            }
        }

        target.compilations.forEach compilation@{ compilation ->
            val defaultSourceSet = compilation.defaultSourceSet

            val inputFile = file("src/${defaultSourceSet.name}/java-jdk9/module-info.java")
            if (!inputFile.isFile) return@compilation

            val compileModuleInfo = tasks.register<CompileModuleInfo>("compile${defaultSourceSet.name}ModuleInfo") {
                source.set(inputFile)
                output.set(File(buildDir, "classes/java-jdk9/${compilation.name}/module-info.class"))
                version.set(project.version.toString())
            }

            artifactTask.dependsOn(compileModuleInfo)
            artifactTask.into("META-INF/versions/9") {
                from(compileModuleInfo.get().output) {
                    includeEmptyDirs = false
                }
            }
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

publishing {
    publications {
        publications.withType<MavenPublication>().configureEach {
            val emptyJavadocJar = tasks.register<Jar>("${name}JavadocJar") {
                archiveBaseName.set("${archiveBaseName.get()}-${name}")
                archiveClassifier.set("javadoc")
            }

            artifact(emptyJavadocJar)
        }
    }
}