# JAViewer

质感设计的影片浏览客户端。本仓库为继续维护的 fork：`https://github.com/su-yao/JAViewer`。原项目：`https://github.com/SplashCodes/JAViewer`。

当前版本：`2.0.0-alpha 7`（`versionCode 18`），仅提供 `arm64-v8a` 包。

## 下载

分支内已入库 APK：

| 文件 | 说明 |
| --- | --- |
| [JAViewer-2.0.0-alpha7-arm64-debug.apk](JAViewer-2.0.0-alpha7-arm64-debug.apk) | 当前推荐，debug |
| [JAViewer-2.0.0-alpha6-arm64-debug.apk](JAViewer-2.0.0-alpha6-arm64-debug.apk) | 上一版 debug |
| [JAViewer-2.0.0-alpha1-arm64.apk](JAViewer-2.0.0-alpha1-arm64.apk) | 早期签名包 |

安装要求：Android 4.1+（`minSdk 16`），64 位设备。

## 数据源

启动时读取本地 `app/src/main/assets/properties.json`，默认三个 AVMOO JSON API（HTTP）：

- AVMOO 日本：`http://avmoo.shop/jav/data/api/`
- AVMOO 日本无码：`http://avsox.click/javu/data/api/`
- AVMOO 欧美：`http://avheat.shop/wav/data/api/`

客户端对 avmoo 族 HTTPS TLS 失败会回退 HTTP。片源列表、详情、女优、类别走内部 scheme：`movie:` / `star:` / `genre:` / `search:` / `studio:` / `director:` / `label:` / `series:`。

## 磁力搜索

影片详情右下角 FAB 打开下载页，按识别码搜索。默认 tab：

Sukebei、磁力狗、TorrentKitty、磁力片、磁力蜘蛛、磁力星球、dobt、磁力猫。

列表点击弹出复制 / 打开磁力；长按识别码或影片名只复制。下载页右下角云+放大镜 FAB 用系统浏览器打开当前 tab 站点搜索页。文件名和大小由 whatslink 补全，结果留在列表内。

## 本分支改动

- 片源切到 AVMOO JSON API，启动不再依赖远程 `properties.json`
- 去掉 Fabric / Crashlytics；AGP 7.4.2 + Gradle 7.5.1 + JDK 17
- 侧栏 GitHub 指向本 fork；下载页无打赏弹窗
- 空 extras、空响应、页面销毁后网络回调、无浏览器 / 磁力应用时不再崩溃
- OkHttp 超时：连接 15s、读取 30s、写入 15s

## 构建

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export GRADLE_USER_HOME=/tmp/gradle-home

# 运行单测
./gradlew :app:testDebugUnitTest --no-daemon

# 打包 debug APK
./gradlew :app:assembleDebug --no-daemon
```

产物：`app/build/outputs/apk/debug/app-debug.apk`。每次编译需把 `app/build.gradle` 的 `versionCode` / `versionName` 与 `properties.json` 的 `latest_version` / `latest_version_code` 同步递增。下次应为 `19` / `2.0.0-alpha 8`。

## 问题反馈

Issues：`https://github.com/su-yao/JAViewer/issues`
