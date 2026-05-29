# Media3 Transformer & Effect 开发指南

> AndroidX Media3 1.4.x · minSdk 21 · Kotlin  
> 官方文档：`developer.android.com/media/media3/transformer`

---

## 两个库一览

| 库                    | artifact                             | 职责                                           |
|----------------------|--------------------------------------|----------------------------------------------|
| `media3-transformer` | `androidx.media3:media3-transformer` | 视频编辑的主控制器。接收原始媒体，执行 Effect 链，导出新文件。          |
| `media3-effect`      | `androidx.media3:media3-effect`      | 视觉特效工具箱：旋转、裁剪、覆盖、滤镜、色彩调整等，配合 Transformer 使用。 |

> **协作关系**：单独使用 transformer 即可完成格式转换；需要视觉/音频处理时才引入 effect。两者通过 `EditedMediaItem.Builder.setEffects()` 连接。

---

## 解决了什么问题

| 痛点          | 引入前                                            | 引入后                                                          |
|-------------|------------------------------------------------|--------------------------------------------------------------|
| 系统 API 碎片化  | MediaRecorder / FFmpeg / MLKit 各自独立，版本兼容维护成本极高 | 统一 API，向下兼容至 API 21，Transformer 自动选择硬件编解码器                   |
| 硬件编解码对接复杂   | 需手写 MediaCodec 状态机，处理 Surface、Buffer、超时等细节     | Transformer 内置 AssetLoader + Codec 管线，无需手动操作 MediaCodec      |
| 多效果组合困难     | 自行维护 OpenGL ES SurfaceTexture 链路，每加一个效果就要改渲染管线 | Effect 实现 GlEffect 接口，Transformer 自动串联 OpenGL 渲染链            |
| 进度 / 错误反馈缺失 | 需自行轮询 MediaMuxer / 计算帧数进度，无统一取消机制              | Transformer.Listener 提供 onCompleted / onError / progress 三回调 |
| 性能优化门槛高     | 需了解 passthrough 转码技巧才能避免不必要的解/编码               | Transformer 自动检测：无效果时直接 mux，不走解编码，速度提升数倍                     |

---

## 何时该用 / 不该用

| 应用场景            | 适合度  | 说明                             |
|-----------------|------|--------------------------------|
| 短视频剪辑 App       | ✅ 高  | 核心导出引擎，替代 FFmpeg               |
| 相机后处理 / 滤镜      | ✅ 高  | Effect + Transformer 实时预览 + 导出 |
| 视频水印 / LOGO 覆盖  | ✅ 高  | OverlayEffect 几行代码搞定           |
| 格式转换 (mp4→webm) | ✅ 高  | passthrough 路径极快               |
| 社交媒体上传压缩        | ✅ 高  | 指定码率 / 分辨率，Transformer 自动编码    |
| 仅音频编辑           | ⚠️ 中 | AudioProcessor 支持，但不如专用音频库灵活   |
| 实时流媒体推流         | ❌ 低  | Transformer 面向文件导出，非实时流        |
| 复杂 3D 特效 / 粒子   | ❌ 低  | 需自实现 GlEffect，SDK 不含粒子系统       |

---

## 内置 Effect 速查表

| Effect 类名                      | 分类       | 用途                                       |
|--------------------------------|----------|------------------------------------------|
| `ScaleAndRotateTransformation` | 几何变换     | 缩放 + 旋转画面                                |
| `Crop`                         | 几何变换     | 裁剪画面区域                                   |
| `Presentation`                 | 几何变换     | 改变输出分辨率 / 宽高比                            |
| `LanczosResample`              | 几何变换     | 高质量 Lanczos 缩放                           |
| `OverlayEffect`                | 叠加       | 添加图片 / 文字水印（BitmapOverlay / TextOverlay） |
| `Contrast`                     | 色彩调整     | 调整对比度                                    |
| `Brightness`                   | 色彩调整     | 调整亮度                                     |
| `Saturation`                   | 色彩调整     | 调整饱和度                                    |
| `HslAdjustment`                | 色彩调整     | 色相 / 饱和度 / 亮度精细调整                        |
| `RgbAdjustment`                | 色彩调整     | 独立 R/G/B 通道调整                            |
| `GlEffect`（自定义）                | 自定义 GLSL | 实现接口注入自己的 OpenGL 着色器                     |
| `AudioProcessor`               | 音频       | PCM 层面处理：混音、声道映射                         |

---

## 代码示例

### 1. 添加依赖

```kotlin
// build.gradle.kts
dependencies {
    implementation("androidx.media3:media3-transformer:1.4.1")
    implementation("androidx.media3:media3-effect:1.4.1")   // 需要特效时加
    implementation("androidx.media3:media3-common:1.4.1")
}
```

### 2. 最简格式转换（无需 Effect）

```kotlin
val transformer = Transformer.Builder(context)
    .addListener(object : Transformer.Listener {
        override fun onCompleted(composition: Composition, result: ExportResult) {
            // 导出成功，result.durationMs 可读取时长
        }
        override fun onError(
            composition: Composition, result: ExportResult,
            exception: ExportException
        ) {
            // 处理错误
        }
    })
    .build()

val inputMedia = MediaItem.fromUri(inputUri)
transformer.start(inputMedia, outputPath)  // 异步，立即返回
```

> ⚠️ `transformer.start()` 异步执行，Listener 回调在主线程。不要在回调中做耗时操作。

### 3. 监控进度 & 取消

```kotlin
// 在协程 / Handler 中轮询
val progressState = ProgressHolder()
val state = transformer.getProgress(progressState)
// state == RESULT_STATE_ESTIMATING 时 progressState.progress 为 0~100

// 随时取消
transformer.cancel()
```

### 4. Effect 组合：旋转 + 裁剪 + 水印

```kotlin
// 1. 构建 Effect 列表（顺序即渲染顺序）
val effects = Effects(
    /* audioProcessors = */ emptyList(),
    /* videoEffects = */ listOf(
        // 旋转 90°
        ScaleAndRotateTransformation.Builder()
            .setRotationDegrees(90f)
            .build(),
        // 裁剪中央 16:9 区域
        Crop(/* left= */ -1f, /* right= */ 1f,
            /* bottom= */ -0.5625f, /* top= */ 0.5625f
        ),
        // 右下角贴图水印
        OverlayEffect(
            ImmutableList.of(
                BitmapOverlay.createStaticBitmapOverlay(
                    watermarkBitmap,
                    OverlaySettings.Builder()
                        .setAlphaScale(0.8f)
                        .build()
                )
            )
        ),
    )
)

// 2. 绑定到 EditedMediaItem
val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(inputUri))
    .setEffects(effects)
    .build()

// 3. 包装成 Composition
val composition = Composition.Builder(
    EditedMediaItemSequence(listOf(editedItem))
).build()

// 4. 启动导出
transformer.start(composition, outputPath)
```

### 5. 自定义 GLSL 着色器（GlEffect）

```kotlin
class GrayscaleEffect : GlEffect {
    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram {
        return SimpleGlShaderProgram(
            context,
            /* vertexShaderFilePath = */ "vertex_shader_default.glsl",
            /* fragmentShaderFilePath = */ "grayscale_fragment.glsl"
        )
    }
}
// grayscale_fragment.glsl 中将 rgb 转为 luminance 即可
```

---

## 内部数据流（简化）

```
输入 (MediaItem/Uri)
  ↓
AssetLoader          — 解封装 + 软/硬解码，输出 PCM + YUV/Surface
  ↓
Effect 链（GPU）      — OpenGL ES 渲染管线，逐帧应用 GlEffect 列表
AudioProcessor 链    — PCM 串行处理（混音、重采样、声道映射）
  ↓
Encoder              — 硬件优先编码（H.264 / H.265 / AV1）
  ↓
Muxer                — 写入 mp4 / webm / ogg，输出到指定路径
```

> **passthrough 优化**：若输入格式 = 输出格式且无 Effect，Transformer 跳过 AssetLoader → Encoder，直接 mux，速度可比全解码编码快 5–10 倍。

---

## HDR 视频支持 & HdrToSdr

Media3 Transformer 对 HDR 视频有完整支持，通过 `Composition.setHdrMode()` 控制输出策略。

### 支持的 HDR 格式

| 格式                     | 支持情况         | 备注                        |
|------------------------|--------------|---------------------------|
| HLG (Hybrid Log-Gamma) | 解码 + 转换      | 广播标准，支持最好                 |
| HDR10 (ST 2084 / PQ)   | 解码 + 转换      | 流媒体主流格式                   |
| HDR10+                 | 解码（动态元数据不传递） | 元数据层不处理，色调映射按静态 MaxCLL 处理 |
| Dolby Vision           | 解码（部分设备）     | 需设备硬件支持，转换路径受限            |

### 四种 HdrMode 对比

| 常量                                                 | 值 | 描述                               | 最低 API | 速度              |
|----------------------------------------------------|---|----------------------------------|--------|-----------------|
| `HDR_MODE_KEEP_HDR`                                | 0 | 保留 HDR 全程不转换，输出仍为 HDR 文件         | 21+    | 最快（passthrough） |
| `HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL`       | 2 | OpenGL ES 软件色调映射（BT.2390），兼容性最广  | 21+    | 中（GPU 渲染）       |
| `HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_MEDIACODEC`    | 1 | MediaCodec 硬件色调映射，速度最快，但并非所有设备支持 | 31+    | 快（硬件）           |
| `HDR_MODE_EXPERIMENTAL_FORCE_INTERPRET_HDR_AS_SDR` | 3 | 强制将 HDR 数据当 SDR 读取，颜色会过曝/偏色      | 21+    | 最快（但颜色错误）       |

> **生产环境推荐**：优先用 `HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL`（兼容 API 21+）；在 API 31+ 设备上用 `isToneMappingSupported()` 检测后再升级为 MediaCodec 模式以换取速度。

### HdrToSdr 核心用法

**方案 A：OpenGL 软件映射（兼容性最广，推荐首选）**

```kotlin
val transformer = Transformer.Builder(context)
    .addListener(listener)
    .build()

val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(hdrVideoUri))
    // HDR→SDR 由 hdrMode 控制，无需手动加 Effect
    .setEffects(Effects(emptyList(), emptyList()))
    .build()

val composition = Composition.Builder(
    EditedMediaItemSequence(listOf(editedItem))
)
    // OpenGL BT.2390 色调映射，输出 SDR H.264 + BT.709
    .setHdrMode(Composition.HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL)
    .build()

transformer.start(composition, outputPath)
```

**方案 B：运行时自适应（优先 MediaCodec，回退 OpenGL）**

```kotlin
fun exportHdrToSdr(
    context: Context,
    transformer: Transformer,
    hdrUri: Uri,
    outputPath: String,
) {
    // 检查设备是否支持 MediaCodec 硬件色调映射 (API 31+)
    val hdrMode = if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        transformer.isToneMappingSupported(
            MediaItem.fromUri(hdrUri),
            Composition.HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_MEDIACODEC
        )
    ) {
        Composition.HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_MEDIACODEC  // 硬件加速
    } else {
        Composition.HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL     // 软件回退
    }

    val composition = Composition.Builder(
        EditedMediaItemSequence(
            listOf(EditedMediaItem.Builder(MediaItem.fromUri(hdrUri)).build())
        )
    ).setHdrMode(hdrMode).build()

    transformer.start(composition, outputPath)
}
```

**方案 C：HdrToSdr + 叠加其他 Effect（如旋转、水印）**

```kotlin
// Effect 列表照常写；HDR→SDR 的色调映射由 hdrMode 在 Composition 层控制
// Transformer 内部会在 Effect 链执行前先完成线性光空间的色调映射
val effects = Effects(
    emptyList(),
    listOf(
        ScaleAndRotateTransformation.Builder().setRotationDegrees(90f).build(),
        OverlayEffect(ImmutableList.of(BitmapOverlay.createStaticBitmapOverlay(logo))),
    )
)

val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(hdrUri))
    .setEffects(effects)
    .build()

val composition = Composition.Builder(
    EditedMediaItemSequence(listOf(editedItem))
)
    .setHdrMode(Composition.HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL)
    .build()

transformer.start(composition, outputPath)
```

### 注意的坑

#### 坑 1：自定义 GlEffect 遇到 HDR 内容

- `hdrMode = HDR_MODE_KEEP_HDR` 时，`toGlShaderProgram(context, useHdr = true)` 被调用，着色器接收的纹理是**线性光空间（linear light）**，**不可做 sRGB gamma 运算**
  ，否则颜色会严重失真。
- `hdrMode = HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_OPEN_GL` 时，Transformer 会在你的 Effect **之前**插入内置的色调映射 pass，你的着色器拿到的已是 SDR 的 sRGB 值，
  `useHdr = false`，无需特殊处理。

#### 坑 2：MTK 芯片 + Android 12 MediaCodec 颜色偏差

部分 Android 12 设备（联发科芯片）在 `HDR_MODE_TONE_MAP_HDR_TO_SDR_USING_MEDIACODEC` 下存在颜色偏差 bug，建议对此类设备强制回退到 OpenGL 方案，测试覆盖后再决定是否启用硬件映射。

#### 坑 3：HDR10+ 动态元数据丢失

HDR10+ 的逐帧亮度曲线（动态元数据）在转换中会丢失，色调映射退化为静态 MaxCLL 处理，高光细节还原不如原始内容精确。如需保留动态元数据，目前无法通过 Media3 实现，需借助专业编码工具。

#### 坑 4：Dolby Vision 转换不可靠

非杜比授权设备上 DV 转换结果不保证正确。建议检测到 DV 内容时提示用户，或降级为 HDR10 处理路径。

#### 坑 5：视频旋转元数据默认不应用

原始视频含 rotation metadata 时，Transformer 默认不自动旋转。需显式添加 `ScaleAndRotateTransformation`，或在 `EditedMediaItem` 上调用 `.setRotationDegrees()` 强制处理。

#### 坑 6：ExportException 替代了旧的 TransformationException

Media3 1.2.0 起，`TransformationException` 已废弃，统一使用 `ExportException`，通过 `errorCode` 区分硬件 / 软件 / IO 错误类型。

---

## 常见问题

**Q：导出后视频方向不对（旋转 90°）？**  
A：原始视频含 rotation metadata，Transformer 默认不应用。需显式添加 `ScaleAndRotateTransformation` 或在 Composition 里关闭 transmux 强制解码后处理。

**Q：水印/覆盖层坐标系是什么？**  
A：`OverlaySettings` 使用 `[-1, 1]` 归一化坐标，原点在画面中心，Y 轴向上。右下角 ≈ `(0.8, -0.8)`。

**Q：TransformationException vs ExportException？**  
A：1.2.0 起 `TransformationException` 已废弃，统一使用 `ExportException`，`errorCode` 区分硬件/软件/IO 错误。

**Q：能否在 Compose 中预览 Effect 效果？**  
A：可以。用 `media3-exoplayer + PlayerView` 实时应用 Effect 预览，导出时再用 Transformer，共享同一套 Effect 列表。

**Q：如何保留 HDR 不降级？**  
A：`Composition.Builder` 设置 `setHdrMode(HDR_MODE_KEEP_HDR)`，确保输出编解码器支持 HDR，Effect 着色器在线性光空间操作（`useHdr = true`）。
