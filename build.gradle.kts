/*
 * Copyright (c) 2021 Leon Linhart
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
import com.gw2tb.gw2chatlinks.build.*
import com.gw2tb.gw2chatlinks.build.BuildType
import com.gw2tb.gw2chatlinks.build.tasks.*

import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.jvm.*

plugins {
    kotlin("multiplatform") version "1.6.0"
    id("org.jetbrains.dokka") version "1.6.0"
    signing
    `maven-publish`
}

val nextVersion = "0.3.0"

group = "com.gw2tb.gw2chatlinks"
version = when (deployment.type) {
    BuildType.SNAPSHOT -> "$nextVersion-SNAPSHOT"
    else -> nextVersion
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    explicitApi()

    js(BOTH) {
        browser {
            testTask {
                enabled = false
            }
        }
        nodejs()
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.RequiresOptIn")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    targets.filter { it is KotlinJvmTarget || it is KotlinWithJavaTarget<*> }.forEach { target ->
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

tasks.withType<JavaCompile> {
    options.release.set(8)
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            url = uri(deployment.repo)

            credentials {
                username = deployment.user
                password = deployment.password
            }
        }
    }
    publications {
        publications.withType<MavenPublication> {
            artifact(emptyJavadocJar)

            pom {
                name.set("GW2ChatLinks")
                description.set("A Kotlin Multiplatform library for parsing and generating GW2 chat links.")
                url.set("https://github.com/GW2ToolBelt/GW2ChatLinks")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/GW2ToolBelt/GW2ChatLinks/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("TheMrMilchmann")
                        name.set("Leon Linhart")
                        email.set("themrmilchmann@gmail.com")
                        url.set("https://github.com/TheMrMilchmann")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/GW2ToolBelt/GW2ChatLinks.git")
                    developerConnection.set("scm:git:git://github.com/GW2ToolBelt/GW2ChatLinks.git")
                    url.set("https://github.com/GW2ToolBelt/GW2ChatLinks.git")
                }
            }
        }
    }
}

signing {
    isRequired = (deployment.type === BuildType.RELEASE)
    sign(publishing.publications)
}

repositories {
    mavenCentral()
}