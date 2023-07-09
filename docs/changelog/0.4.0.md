### 0.4.0

_Released 2023 Jul 09_

#### Improvements

- Added a new experimental `weapons` field for build template chat links.
  - This is subject to change when SotO releases or additional information about
    the semantics of weapon encodings are confirmed.

#### Breaking Changes

- The experimental `relicID` in build template links is now an `UByte` instead
  of an `UShort`.