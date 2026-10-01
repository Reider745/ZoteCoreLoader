# Android compatibility stubs

This directory intentionally contains only `android.jar`, a generated set of
API-only Android compatibility stubs required to compile and run the legacy
Inner Core API on the standalone server.

The stubs can be regenerated from a locally supplied Android API input with:

```shell
./gradlew rebuildAndroidStubs
```

The input file is expected at `.todo/android.jar` and is excluded from Git.
All other project dependencies are resolved from the remote repositories
declared in `build.gradle` and must not be vendored here. The legacy dex2jar
libraries are extracted into `build/dependencies` from the checksum-pinned
official v2.4 release during the build.
