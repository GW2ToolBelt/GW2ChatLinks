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