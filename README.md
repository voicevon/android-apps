# Feng Suite (Android)

本项目是集 **翻页时钟/闹钟、实时天气、待办便签、MQTT 即时通讯与设备心跳** 于一体的现代化 Android 综合套件应用（All-in-One Suite）。

---

## 架构说明 (Architecture - Single Source of Truth)

本项目遵循**单一真理源 (SSOT)** 与极简模块化原则，唯一主应用工程为 `feng_suite/`，内部按业务特性进行清晰分层：

```text
android-apps/
├── feng_suite/                     # 主工程与唯一真理源 (Single Source of Truth)
│   └── app/src/main/java/com/feng/suite/
│       ├── core/                   # 核心基础设施 (MQTT 通讯、心跳、网络、数据存储)
│       │   └── mqtt/               # MQTT 客户端、配置与 HeartbeatWorker
│       ├── feature/                # 业务特性模块
│       │   ├── clock/              # 翻页时钟与闹钟服务
│       │   ├── weather/            # 多维城市实时天气与预报
│       │   ├── todo/               # 待办清单与分类便签
│       │   └── chat/               # 即时消息收发与 MQTT 历史流
│       ├── ui/                     # 统一界面层
│       │   ├── dashboard/          # 主控看板 (Dashboard)
│       │   ├── navigation/         # 底部导航与页面路由
│       │   └── theme/              # 全局暗黑/发光主题设计系统
│       └── MainActivity.kt         # 主入口
├── wiki/                           # 协议与设计规范文档
│   └── MQTT_PROTOCOL.md            # MQTT 通讯与心跳报文协议规范
└── temp/                           # 唯一临时调试与实验目录 (已配置 Git 忽略)
```

---

## 核心功能模块

1. **时钟与闹钟 (`feature/clock`)**：
   - 翻页全屏时钟显示；
   - 完整的本地闹钟调度与响铃服务，支持多端 MQTT 报文同步控制。
2. **实时天气 (`feature/weather`)**：
   - 城市切换、气温、湿度、风力、空气质量及多日天气走势卡片。
3. **待办清单 (`feature/todo`)**：
   - 优先级分类、状态切换、本地 Room 持久化存储。
4. **即时通讯与设备心跳 (`feature/chat` & `core/mqtt`)**：
   - 基于 MQTT 协议（`voicevon.vicp.io:1883`）的点对点与群组即时消息；
   - 消息固定按时间正序流式呈现；
   - 设备在线心跳定时上报（每 1 小时，前台协程 + 后台 WorkManager 保活）；
   - 内置协议查看 Wiki 弹窗。

---

## 构建与运行

在 Windows 环境下推荐使用项目规范的 JDK (JBR 21) 进行构建：

```powershell
cd feng_suite
cmd /c "set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr&& gradlew.bat assembleDebug"
```

部署至连接的 Android 设备：

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.feng.suite/.MainActivity
```
