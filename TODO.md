# TODO

## CI

- [ ] Add ktlint formatting/lint checks and run them in CI.
  - ktlint is not currently part of the build; CI only runs Android lint (`:app:lintDebug`).
  - Likely needs the `org.jlleitschuh.gradle.ktlint` Gradle plugin (or the ktlint CLI) plus a `.editorconfig`.
  - Decide whether CI should fail on formatting differences (`ktlintCheck`) or only report them.
