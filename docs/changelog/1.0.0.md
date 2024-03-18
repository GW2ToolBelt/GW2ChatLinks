### 1.0.0

_Released 2024 Mar 18_

#### Improvements

- Users who consume the library from Maven (or other build tools that do not
  support) Gradle module metadata, no longer need to depend on the JVM platform
  module. [[GH-18](https://github.com/GW2ToolBelt/GW2ChatLinks/issues/18)]
- Moved experimental unsigned type markers from `ChatLink.User` to its members.
  - In practice, this means that opt-in for experimental unsigned types is no
    longer required when performing type checks (e.g. in `when` blocks).