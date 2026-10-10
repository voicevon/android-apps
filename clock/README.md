# Clock - 极简时钟与闹钟 (Minimal Clock & Alarm)

## 1. 项目定位与核心设计原则

本项目以“**极致极简、纯粹无扰、纯本地离线**”为基石，从零构建一款轻量、现代化的 Android 时钟与闹钟应用。

- **极简主义 (Ultra-Minimalist)**：杜绝冗杂功能与臃肿后台，专注最核心的时间感知与定时提醒路径（设置闹钟、定时响铃、贪睡/关闭）。
- **零打扰与纯本地 (100% Offline & No Ads)**：无网络请求、无需权限滥用、无广告与弹窗，完全依赖 Android 系统底层精确告警调度机制（`AlarmManager`）。
- **现代 Android 架构**：采用 Kotlin + Jetpack Compose + Material Design 3 构建，界面轻盈灵动，逻辑清晰可测。

---

## 2. 候选对标开源软件分析与推荐

为选定最合适的技术与交互参考，我们精选了 GitHub 上活跃且声誉优良的开源时钟项目：

| 标杆项目 | 开源地址 | 核心特点 | 复杂度 | 优缺点对比与借鉴建议 |
| :--- | :--- | :--- | :--- | :--- |
| **Fossify Clock**<br>(原 Simple Clock) | [FossifyOrg/Clock](https://github.com/FossifyOrg/Clock) | 功能完整（闹钟/世界时钟/秒表/倒计时），纯离线、隐私优先 | 中等 | **优点**：成熟稳定，权限与前台服务处理非常完善。<br>**缺点**：多 Tab 结构，功能较全面。<br>**借鉴**：底层 `AlarmManager` 和音频振动唤醒逻辑。 |
| **Alarmio** | [fennifith/Alarmio](https://github.com/fennifith/Alarmio) | 专注于闹钟与计时，界面极其简洁，强调视觉美感与极简动效 | 轻量 | **优点**：界面干净扁平，去掉了所有冗余功能，代码纯粹。<br>**缺点**：部分 UI 仍使用传统 Views。<br>**借鉴**：极简交互流与优雅的响铃交互。 |
| **AOSP DeskClock** | [AOSP / LineageOS DeskClock](https://github.com/LineageOS/android_packages_apps_DeskClock) | Google 官方原生标准时钟架构 | 中高 | **优点**：官方最佳实践，系统兼容性极佳。<br>**缺点**：历史代码包袱重，包含大量老版本适配逻辑。<br>**借鉴**：闹钟广播与锁屏全屏 Intent 触发方案。 |
| **Klock / Minimal Clock** | 社区轻量级 Jetpack Compose 时钟原型 | 纯 Compose 声明式 UI，代码规模 < 500 行 | 极轻量 | **优点**：架构最新，最易阅读与二次定制。<br>**缺点**：工业级保活与定制铃声较简易。<br>**借鉴**：现代 Compose UI 快速搭建与状态管理。 |

> **建议选型结论**：
> - **UI/交互层**：对标 **Alarmio** 与 现代极简设计，专注单一列表与即刻开关，不堆砌繁复的二级菜单。
> - **核心告警与调度层**：参考 **Fossify Clock** 扎实的 Android 12+ 精确闹钟权限管理与全屏通知（Full-Screen Intent）实现。

---

## 3. 功能规划与阶段划分

遵循敏捷迭代与 YAGNI（不编写过度设计的代码）原则：

### Phase 1: 核心基础版本 (MVP)
1. **闹钟列表展示与一键开关**：
   - 主界面直观展示已配置的闹钟卡片（时间、重复日、标签）。
   - 简洁的 Switch 开关，快速启用/停用。
2. **快速新建与编辑**：
   - 现代时间选择器（Time Picker），滑动或点选分钟小时。
   - 重复周期设置（仅响一次 / 工作日 / 周末 / 每天 / 自定义周几）。
   - 闹钟备注标签（如“早起打卡”、“晨间锻炼”）。
3. **高可靠性定时调度与响铃**：
   - 基于 `AlarmManager.setExactAndAllowWhileIdle()`，兼容 Doze 待机深度休眠模式。
   - 锁屏弹窗与全屏唤醒页面（Full-Screen Intent），大按钮支持**关闭（Dismiss）**与**贪睡（Snooze 5/10分钟）**。
4. **本地持久化存储**：
   - 基于 Room 或 DataStore 本地存储闹钟配置，开机自启（`BOOT_COMPLETED`）自动恢复闹钟。

### Phase 2: 轻量扩展功能 (可选增强)
- 倒计时器（Timer）/ 番茄钟模式。
- 极简全屏翻页/数字时钟夜间模式（Night Clock）。
- 振动强弱与淡入渐响铃声。

---

## 4. 技术栈规划

- **开发语言**：Kotlin
- **UI 框架**：Jetpack Compose + Material Design 3 (Material You)
- **核心组件**：
  - `AlarmManager`（底层精确定时唤醒）
  - `NotificationCompat`（前台通知与锁屏悬浮唤醒）
  - `Room Database` 或 `DataStore Preferences`（极简本地配置持久化）
- **架构模式**：MVVM (ViewModel + StateFlow + Repository)
