### 0.2.0

_Released 2021 Nov 13_

#### Improvements

- The JVM artifact is now a multi-release Jar with an explicit module descriptor
  for Java 9 and later.

#### Fixes

- Fixed an index-out-of-bounds error when calling `Profession.valueOf` with a value of 10.