# vcatalog

面向 5gene / 5hmlA Android/Kotlin 生态的**集中式 Gradle Version Catalog**，将 200+ 依赖声明（库、插件、Bundle）统一管理，通过 Maven 发布 + Gradle Settings Plugin 自动导入，让下游项目
**零配置**获得完整的版本管理能力。

## 功能特性

- **统一依赖版本管理** — 覆盖 AndroidX、Compose、Kotlin、Media3、OkHttp/Retrofit、Ktor、Koin、Hilt、KMP 等主流技术栈
- **预设依赖组合 (Bundles)** — `compose`、`android-basic`、`okhttp`、`koin`、`ktor` 等 20+ 组合，一行引入整套技术栈
- **自动配置镜像仓库** — 内置腾讯云 Maven 镜像加速，自动优化 Google/MavenCentral 仓库内容过滤
- **版本覆盖机制** — 通过 `vcl { doOverride {} }` 在下游项目中覆盖任意依赖版本
- **一键版本升级** — 提供 Python 脚本批量更新所有子项目的 Gradle 和 VCL 版本号
- **CI/CD 自动发布** — 推送 tag 后自动发布到 Maven Central 和 Gradle Plugin Portal

## 快速开始

### 方式一：通过插件引入（推荐）

在 `settings.gradle.kts` 中：

```kotlin
pluginManagement {
    plugins {
        id("io.github.5hmlA.vcl") version "<version>"
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
}

// 可选：覆盖特定依赖版本
vcl {
    doOverride {
        version("google-ksp", "2.2.0-2.0.2")
    }
}
```

在 `build.gradle.kts` 中使用：

```kotlin
plugins {
    alias(vcl.plugins.android.application)
    alias(vcl.plugins.kotlin.android)
    alias(vcl.plugins.ksp)
}

dependencies {
    // 直接引用预定义的依赖别名
    implementation(vcl.androidx.core.ktx)
    implementation(vcl.androidx.lifecycle.viewmodel.ktx)

    // BOM 统一版本
    implementation(platform(vcl.androidx.compose.bom))
    implementation(vcl.androidx.compose.material3)

    // 预设 Bundle 一键引入整套依赖
    implementation(platform(vcl.bundles.compose))

    // KSP 注解处理器
    ksp(vcl.androidx.room.compiler)
    ksp(vcl.google.dagger.hilt.compiler)
}
```

### 方式二：直接引用 TOML 文件

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    versionCatalogs {
        create("vcl") {
            from(files("../wings.versions.toml"))
        }
    }
}
```

## 依赖分类与使用

### Gene 生态库

| 别名                      | 用途                       |
|-------------------------|--------------------------|
| `vcl.gene.cubic`        | Gene 内部轻量工具              |
| `vcl.gene.cartoon`      | 卡通/帧动画封装                 |
| `vcl.gene.adapter`      | 多类型 RecyclerView 适配器 DSL |
| `vcl.gene.view.dsl`     | XML/View 声明式 DSL         |
| `vcl.gene.ksp.poe`      | KSP 生成 Kotlin 代码         |
| `vcl.gene.retrofit.ksp` | Retrofit KSP 处理器         |

### 预设 Bundles

| Bundle                    | 说明                                                    |
|---------------------------|-------------------------------------------------------|
| `bundles.compose`         | Compose 核心：UI + M3 + Navigation3 + Activity/Lifecycle |
| `bundles.compose.more`    | 扩展：动画、图标、测试、分页、Google Fonts                           |
| `bundles.compose.image`   | 图片加载：Coil + Lottie Compose                            |
| `bundles.android.basic`   | Android 基础：AppCompat + DataStore + 协程 + Lifecycle     |
| `bundles.android.view`    | View 控件：Material + RecyclerView + ConstraintLayout    |
| `bundles.android.project` | 传统 Android 一站式依赖                                      |
| `bundles.okhttp`          | 网络请求：OkHttp + Retrofit + 日志                           |
| `bundles.koin`            | Koin 依赖注入全家桶                                          |
| `bundles.ktor`            | Ktor 客户端核心 + 日志 + 序列化                                 |
| `bundles.hilt`            | Hilt 依赖注入                                             |

### 版本覆盖

在 `settings.gradle.kts` 中可以覆盖任何依赖版本：

```kotlin
vcl {
    // 在 Catalog 创建前覆盖
    onCreate {
        version("kotlin", "2.1.0")
    }
    // 在 Catalog 创建后覆盖
    doOverride {
        version("androidx-lifecycle", "2.8.0")
    }
}
```

## 项目结构

```
vcatalog/
├── wings.versions.toml      # 核心：所有依赖版本声明
├── vcatalog/                 # Gradle Settings Plugin 实现
│   └── src/main/kotlin/june/VCatalogPlugin.kt
├── check/                    # 集成测试/示例项目
│   ├── app/build.gradle.kts  # 验证所有 catalog 别名可用
│   └── lib/                  # 占位库模块
├── update_vcl_gradle_versions.py  # 批量升级 Gradle/VCL 版本
├── clean_all_projects.py          # 递归清理所有子项目
└── .github/workflows/        # CI/CD 自动发布
    ├── android.yml           # PR/push 时运行构建和测试
    └── publish.yml           # tag 时发布到 Maven Central + Plugin Portal
```

## 辅助脚本

### 批量升级版本

```bash
python update_vcl_gradle_versions.py
```

交互式工具，扫描当前目录下所有子项目，批量更新 `gradle-wrapper.properties` 中的 Gradle 版本和 `settings.gradle(.kts)` 中的 VCL 插件版本，可选自动 commit + push。

### 递归清理构建产物

```bash
python clean_all_projects.py
```

遍历所有子项目执行 `./gradlew clean`，跳过 `.gradle`、`build`、`.git` 目录。

## 版本号策略

- 版本号采用**日期格式** `yyyy.MM.dd`（如 `2026.08.29`），每次发布自动递增
- `gene-vcl` 版本号在 `settings.gradle.kts` 中通过 `beijingTimeVersion()` 自动生成，无需手动修改
- AndroidX Release Train 库（lifecycle、room、paging、media3 等）强制使用同一版本号
- BOM 管理的库（compose、okhttp、ktor、koin）子模块不写 version

## License

[Apache License 2.0](LICENSE)
