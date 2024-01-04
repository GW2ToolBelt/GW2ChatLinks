# GW2ChatLinks

GW2ChatLinks is a Kotlin multiplatform library for de- and encoding Guild Wars 2
[chat links](https://wiki.guildwars2.com/wiki/Chat_link_format).

The library is fully written in common Kotlin code. Prebuilt binaries are
available for JVM (Java 11 or later), JS, Wasm, and all native targets.[^1]

[^1]: Since this library does not rely on any platform-specific APIs, we aim to
provide prebuilt libraries for all native targets supported by Kotlin.
Despite that, some targets may be missing as target support may change
over time. In case something is missing, please make sure to let us know.

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