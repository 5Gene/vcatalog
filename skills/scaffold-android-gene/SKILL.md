---
name: scaffold-android-gene
description: >-
  Scaffold a gene/vcl Android Gradle repo (app, library, or both) with the VCL
  settings plugin, Gradle wrapper, and GitHub Actions. Use when creating an
  initial Android project.
---

# Scaffold Android Gene Repo

Create a new Android Gradle repository that uses `io.github.5hmlA.vcl` for
version catalog and convention plugins.

Work only in the **user-specified new directory**. Do not modify the directory
that hosts this skill file.

## Parameters (ask before writing files)

1. **Shape**: `app` | `library` | `app+library`  
   Default: `app+library` (demo app builds; library publishes).
2. **Names**:
    - `rootProject.name`
    - app `namespace` / `applicationId` (if app exists)
    - library module name and `namespace` (if library exists)
    - `publishAndroidMavenCentral("<description>")` description (if library exists)
3. **Target directory**: user-specified; default create a new empty directory.
4. **Git**: `git init` / remote / push only when the user explicitly asks.

## Version resolution (never hardcode)

Resolve at creation time. If lookup fails, **stop and ask the user**. Do not
copy versions from other local projects.

1. **VCL plugin**  
   Open https://plugins.gradle.org/plugin/io.github.5hmlA.vcl and take the
   latest version for `settings.gradle.kts`. Keep a comment with that URL.

2. **Gradle**  
   Choose the newest Gradle compatible with the AGP version shipped by that
   VCL release. Write the Tencent Cloud mirror URL with lowercase `https`:

   ```properties
   distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-<ver>-all.zip
   ```

3. **Wrapper keys** (exact):

   ```properties
   distributionBase=GRADLE_USER_HOME
   distributionPath=wrapper/dists
   distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-<ver>-all.zip
   networkTimeout=10000
   validateDistributionUrl=true
   zipStoreBase=GRADLE_USER_HOME
   zipStorePath=wrapper/dists
   ```

4. **`gradlew` / `gradle-wrapper.jar`**  
   After writing `gradle-wrapper.properties`, run `gradle wrapper` with the
   resolved Gradle version. If `gradle` is not installed, stop and tell the
   user. Do not copy wrapper binaries from other projects.

## Execution checklist

```
- [ ] Collect parameters
- [ ] Resolve VCL + Gradle versions
- [ ] Write settings / root build / modules / properties / gitignore
- [ ] Write minimal source (manifest + Activity or placeholder)
- [ ] Write Gradle wrapper via `gradle wrapper`
- [ ] Write GitHub Actions
- [ ] Verify with assemble
- [ ] Report versions, modules, and skipped git/push
```

## File layout by shape

| Shape         | Modules           | CI build                               | publish.yml |
|---------------|-------------------|----------------------------------------|-------------|
| `app`         | `:app`            | `./gradlew assembleDebug` + APK upload | no          |
| `library`     | `:<lib>`          | `./gradlew :<lib>:assemble`            | yes         |
| `app+library` | `:app` + `:<lib>` | `./gradlew assembleDebug` + APK upload | yes         |

## Templates

Replace placeholders: `<ROOT_NAME>`, `<VCL_VERSION>`, `<APP_NAMESPACE>`,
`<APP_ID>`, `<LIB_NAME>`, `<LIB_NAMESPACE>`, `<PUBLISH_DESC>`,
`<GRADLE_VERSION>`, `<LIB_PACKAGE_PATH>` (namespace with `.` → `/`).

`plugins {}` must be at the top of each Gradle script. Place `group` /
`version` / `publishAndroidMavenCentral` after `plugins`.

### settings.gradle.kts

```kotlin
rootProject.name = "<ROOT_NAME>"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // https://plugins.gradle.org/plugin/io.github.5hmlA.vcl
    id("io.github.5hmlA.vcl") version "<VCL_VERSION>"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// include only modules for the chosen shape:
// include(":app")
// include(":<LIB_NAME>")
```

Do not add composite builds or extra local plugins.

### Root build.gradle.kts

```kotlin
plugins {
    alias(vcl.plugins.android.application) apply false
    alias(vcl.plugins.android.library) apply false
    alias(vcl.plugins.gene.android) apply false
    alias(vcl.plugins.gene.compose) apply false
    alias(vcl.plugins.compose.compiler) apply false
    alias(vcl.plugins.kotlin.jvm) apply false
}
```

### Library module `<LIB_NAME>/build.gradle.kts`

```kotlin
import june.wing.GroupIdMavenCentral
import june.wing.beijingTimeVersion
import june.wing.publishAndroidMavenCentral

plugins {
    alias(vcl.plugins.android.library)
    alias(vcl.plugins.gene.android)
}

group = GroupIdMavenCentral
version = beijingTimeVersion

android {
    namespace = "<LIB_NAMESPACE>"
}

publishAndroidMavenCentral("<PUBLISH_DESC>")

dependencies {
}
```

Do not enable minify or consumer ProGuard by default.

### App module `app/build.gradle.kts`

```kotlin
plugins {
    alias(vcl.plugins.android.application)
    alias(vcl.plugins.gene.compose)
}

android {
    namespace = "<APP_NAMESPACE>"
    defaultConfig {
        applicationId = "<APP_ID>"
    }
}

dependencies {
    // app+library only:
    // implementation(project(":<LIB_NAME>"))
}
```

No product flavors. No extra plugins. Leave compileSdk / Compose compiler to
`gene.android` / `gene.compose`.

### gradle.properties

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

### .gitignore

List entries explicitly. **Never** use `.*` or `.*/` (they hide `.github/`).

```gitignore
*.iml
.gradle
local.properties
.idea
.DS_Store
/build
/captures
.externalNativeBuild
```

### gradle/wrapper/gradle-wrapper.properties

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-<GRADLE_VERSION>-all.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

Then run `gradle wrapper` so `gradlew`, `gradlew.bat`, and
`gradle/wrapper/gradle-wrapper.jar` exist and are executable where needed.

### Minimal sources

**App** `app/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="<ROOT_NAME>"
        android:supportsRtl="true">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

**App** `app/src/main/java/<APP_PACKAGE_PATH>/MainActivity.kt`:

```kotlin
package <APP_NAMESPACE>

import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity()
```

**Library** `<LIB_NAME>/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```

**Library** `<LIB_NAME>/src/main/java/<LIB_PACKAGE_PATH>/Placeholder.kt`:

```kotlin
package <LIB_NAMESPACE>

object Placeholder
```

### .github/workflows/android.yml (when app exists)

```yaml
name: Android CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Build APK
        run: |
          chmod +x gradlew
          ./gradlew assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

### .github/workflows/android.yml (library-only)

```yaml
name: Android CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Assemble library
        run: |
          chmod +x gradlew
          ./gradlew :<LIB_NAME>:assemble
```

### .github/workflows/publish.yml (when library exists)

```yaml
name: publish

on:
  release:
    types: [published]
  push:
    tags:
      - 'v*'
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Publishing
        run: |
          chmod +x gradlew
          ./gradlew publishToMavenCentral
        env:
          SIGN_GPG_KEY: ${{ secrets.SIGN_GPG_KEY }}
          SIGN_GPG_PASSWORD: ${{ secrets.SIGN_GPG_PASSWORD }}
          mavenCentralUsername: ${{ secrets.MAVEN_NAME }}
          mavenCentralPassword: ${{ secrets.MAVEN_PASSWORD }}
          GITHUB_USER: ${{ secrets.NAME_GITHUB }}
          GITHUB_TOKEN: ${{ secrets.TOKEN_GITHUB }}
```

Omit `publish.yml` for app-only repos.

## Verify

- With app: `./gradlew assembleDebug`
- Library-only: `./gradlew :<LIB_NAME>:assemble`

Fix failures before declaring done.

## Final report

Tell the user:

- Resolved VCL and Gradle versions
- Modules created
- Whether `publish.yml` was added
- That git init / remote / push were skipped unless requested
