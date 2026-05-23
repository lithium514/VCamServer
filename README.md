# VCam Server

一个轻量级的 Android HTTP 服务器应用，用于在局域网内接收并保存相机/图像文件到设备相册。

## 功能

- 启动本地 HTTP 服务器（端口 **5000**）
- 自动检测并显示设备 IP 地址
- `POST /upload` — 接收 multipart 图片上传并保存到 `Pictures/VCam/` 目录
- `GET /` — 健康检查

## 使用方法

1. 在 Android 设备上安装并打开 VCam Server
2. 点击 **Start Server** 启动服务器
3. 记下界面显示的 IP 地址（如 `192.168.1.100`）
4. 在局域网内的其他设备上发送图片：

```bash
curl -X POST http://<设备IP>:5000/upload \
  -F "image=@/path/to/photo.jpg"
```

图片将保存到设备的 `Pictures/VCam/` 文件夹中。

## 技术栈

- **语言：** Kotlin 2.0.21
- **UI：** Jetpack Compose + Material 3
- **HTTP 服务器：** NanoHTTPD 2.3.1
- **最低 SDK：** Android 8.0 (API 26)
- **构建：** Gradle 8.13 + AGP 8.12.3

## 构建

```bash
./gradlew assembleDebug
```

## API

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/` | 返回 `"VCam Server is running"` |
| `POST` | `/upload` | 接收 multipart 字段 `image`，保存图片并返回 URI |

## 许可证

MIT
