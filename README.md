# GW2ChatLinks

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat-square&label=License)](https://github.com/GW2Toolbelt/GW2ChatLinks/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.gw2tb.gw2chatlinks/gw2chatlinks.svg?style=flat-square&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.gw2tb.gw2chatlinks/gw2chatlinks)

GW2ChatLinks is a Kotlin multiplatform library for de- and encoding Guild Wars 2
[chat links](https://wiki.guildwars2.com/wiki/Chat_link_format).

The library is fully written in common Kotlin code. Prebuilt binaries are
available for JVM (Java 8 or later) and JS (both, legacy and IR) targets.


## Usage

Return values are wrapped in [Result](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/)
objects for convenient error handling.


### Encoding chat links

A `ChatLink` object may be encoded into a string using the `encodeChatLink` function.

```kotlin
val chatLink: Result<String> = encodeChatLink(ChatLink.Coin(amount = 10203u))
assertEquals("[&AdsnAAA=]", chatLink.getOrThrow())
```


### Decoding chat links

A `ChatLink` object may be decoded from a string using the `decodeChatLink` function.

```kotlin
val chatLink: Result<ChatLink> = decodeChatLink("[&AdsnAAA=]")
assertEquals(ChatLink.Coin(amount = 10203u), chatLink.getOrThrow())
```


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/7.1/userguide/toolchains.html)
to detect and select the JDKs required to run the build. Please refer to the
build scripts to find out which toolchains are requested.

An installed JDK 1.8 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the Java library
- `buildNative<Platform>`   - assemble and test the platform-specific native
                              code
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

#### GW2ChatLinks

```
Copyright (c) 2021 Leon Linhart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

--------------------------------------------------------------------------------

#### Guild Wars 2

> ©2010–2018 ArenaNet, LLC. All rights reserved. Guild Wars, Guild Wars 2, Heart
of Thorns, Guild Wars 2: Path of Fire, ArenaNet, NCSOFT, the Interlocking NC
Logo, and all associated logos and designs are trademarks or registered
trademarks of NCSOFT Corporation. All other trademarks are the property of their
respective owners.

As taken from [Guild Wars 2 Content Terms of Use](https://www.guildwars2.com/en/legal/guild-wars-2-content-terms-of-use/)
on 2021-06-21 11:31 CET.

For further information please refer to [LICENSE](LICENSE).