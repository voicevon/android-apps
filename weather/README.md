# Weather - 极简现代化天气 (Minimal Weather)

## 1. 项目定位与核心设计原则

本项目以“**极致极简、纯粹无广告、美观直观**”为核心原则，打造一款现代化、轻量级的 Android 天气预报应用。

- **极简纯粹 (Ad-Free & Minimalist)**：坚决杜绝广告弹窗、流氓资讯与冗余社交功能，打开即见核心天气信息。
- **免 Key 开源气象源 (Zero-Config Open API)**：首选接入 **[Open-Meteo](https://open-meteo.com/)** 全球高精度免配置开源 API（无需申请私钥、无需绑定信用卡、全免费、支持全球经纬度与城市查询）。
- **现代美学与高颜值 (Material You / M3)**：动态随天气/昼夜变幻的渐变主题背景、柔和毛玻璃卡片、清晰直观的 24 小时气温折线图与 7 天预报。
- **低功耗与本地缓存**：轻量缓存机制（Room / DataStore），支持离线查看最近一次刷新数据，省电省流量。

---

## 2. 候选开源对标软件对比与推荐

精选 GitHub 上最具代表性与高颜值的开源天气应用：

| 标杆项目 | 仓库地址 | 核心设计风格 | 复杂度 | 优缺点对比与借鉴建议 |
| :--- | :--- | :--- | :--- | :--- |
| **Breezy Weather**<br>(强烈推荐) | [breezy-weather/breezy-weather](https://github.com/breezy-weather/breezy-weather) | Material You 动态设计，当今天气界的顶流开源标杆 | 中高 | **优点**：极度美观，现代动画流畅，支持多源与细致指标卡片。<br>**缺点**：代码量庞大（包含50+数据源）。<br>**借鉴**：沉浸式卡片布局、天气状况图标与 24 小时气温滑动态势。 |
| **Geometric Weather**<br>(几何天气) | [WangDaYeeeeee/GeometricWeather](https://github.com/WangDaYeeeeee/GeometricWeather) | 经典几何卡片风，国内口碑极高 | 中等 | **优点**：视觉极其清爽简约，中文字体与本土城市适配良好。<br>**缺点**：原仓库已暂停大版本迭代。<br>**借鉴**：极简几何图形美学、气象折线趋势图。 |
| **Forecastie** | [martykan/forecastie](https://github.com/martykan/forecastie) | 极致极简单页面 | 极轻量 | **优点**：结构简单，核心代码量小。<br>**缺点**：UI 较为传统，缺乏现代感。<br>**借鉴**：纯粹直给的单屏交互哲学。 |
| **Apple Weather / Overdrop** 风格 | 现代流行极简天气卡片范式 | 大数字温度 + 天气状态动效 + 模块化指标网格 | 现代主流 | **优点**：信息层级极为清晰，单手滑动体验极佳。<br>**借鉴**：首屏大字温标 + 24小时逐小时横滑胶囊 + 7日垂直天气清单。 |

> **建议选型与对标方向**：
> - **视觉与交互对标**：以 **Breezy Weather / 几何天气** 为灵感范式，打造纯净的 Material 3 极简卡片流。
> - **数据底座**：采用 **Open-Meteo REST API** + 城市自动/手动搜索（如北京、上海、深圳、广州等全国及全球城市定位）。

---

## 3. 功能规划与阶段划分

遵循 **YAGNI** 原则，优先落地最实用核心功能：

### Phase 1: 核心基础版本 (MVP)
1. **天气首屏看板**：
   - 城市名称、当前实时气温、天气现象文字描述（晴、多云、阴、小雨等）与对应天气图标。
   - 当天最高/最低温、体感温度、空气湿度、风速风向。
2. **24 小时逐小时趋势 (Hourly Forecast)**：
   - 横向滑动卡片列表或平滑气温走势，直观展示未来 24 小时的气温与天气变化。
3. **未来 7 天天气预报 (Daily Forecast)**：
   - 每日天气图标、星期/日期、最高与最低温柱状条。
4. **多城市管理与切换**：
   - 预设主流城市 + 搜索添加城市，支持顶部左右滑动或下拉切换关注的城市。
5. **离线缓存与下拉刷新**：
   - 下拉即刻刷新最新天气，无网络时自动展示本地缓存数据。

### Phase 2: 体验润色 (可选增强)
- 天气现象动态背景（晴朗日光微尘、雨滴下落动效、微风拂云等粒子背景）。
- 桌面小部件（Android App Widget）。
- 空气质量指数 (AQI) 与生活指数提示（紫外线、穿衣、洗车）。

---

## 4. 技术栈规划

- **开发语言**：Kotlin
- **UI 框架**：Jetpack Compose + Material Design 3
- **网络请求**：Ktor Client 或 OkHttp + Kotlinx.serialization (极轻量)
- **气象数据 API**：Open-Meteo REST API (免 Key、极速响应、高可用)
- **本地持久化**：Jetpack DataStore / Room
- **架构模式**：MVVM (ViewModel + StateFlow + Repository)
