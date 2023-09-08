### 0.5.0

_Released 2023 Sep 04_

#### Improvements

- Implemented proper support for SotO build template changes. [[GH-3](https://github.com/GW2ToolBelt/GW2ChatLinks/issues/3)]
  - The experimental functionality added in `0.3.0` and `0.4.0` has been fully
    overhauled to support stored weapon types and skill overrides.
  - The `Weapon` enum can be used to interpret the weapon type ids.
- Improved usability of the library from Java code. [[GH-2](https://github.com/GW2ToolBelt/GW2ChatLinks/issues/2)]
  - Exposed non-mangled variants of functions that use inlined value classes.
  - Tweaked a handful of methods to compile

#### Fixes

- The module descriptor (previously delivered using the multi-release jar
  mechanism) does no longer contain invalid data. [[GH-4](https://github.com/GW2ToolBelt/GW2ChatLinks/issues/4)]

#### Breaking Changes

- The required version of Kotlin was bumped from `1.8` to `1.9`.
- The required version of Java was bumped from `8` to `11`.
- The experimental `weapons` object in build template links is now a list of
  weapon type ids.
- The experimental `relicID` in build template links has been removed.
- A handful of methods had their binary signatures changed in order to improve
  Java compatibility.


---

### 0.4.0

_Released 2023 Jul 09_

#### Improvements

- Added a new experimental `weapons` field for build template chat links.
  - This is subject to change when SotO releases or additional information about
    the semantics of weapon encodings are confirmed.

#### Breaking Changes

- The experimental `relicID` in build template links is now an `UByte` instead
  of an `UShort`.


---

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