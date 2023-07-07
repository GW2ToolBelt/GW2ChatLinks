### 0.3.0

_Released 2023 Jul 07_

#### Improvements

- Added prebuilt artifacts for all native targets currently supported (and not
  deprecated) by Kotlin.
- Updated encoding and decoding of build template chat links to support the game
  following the Guild Wars 2: Secrets of the Obscure combat beta weekend.
  - Added a new experimental `relicID` property to build templates.
  - The encoder was updated to output the new format.
  - The decoder was updated to accept the old and new formats.

#### Breaking Changes

- The required version of Kotlin was bumped from `1.5` to `1.8`.
- Dropped support for legacy Kotlin/JS targets.


---

### 0.2.0

_Released 2021 Nov 13_

#### Improvements

- The JVM artifact is now a multi-release Jar with an explicit module descriptor
  for Java 9 and later.

#### Fixes

- Fixed an index-out-of-bounds error when calling `Profession.valueOf` with a value of 10.


---

### 0.1.0

_Released 2021 Jun 21_

#### Overview

GW2ChatLinks is a Kotlin multiplatform library for de- and encoding Guild Wars 2
[chat links](https://wiki.guildwars2.com/wiki/Chat_link_format).

The library is fully written in common Kotlin code. Prebuilt binaries are
available for JVM (Java 8 or later) and JS (both, legacy and IR) targets.