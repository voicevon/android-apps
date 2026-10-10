# Todo List - Minimal-Todo 极简本地待办清单

## 1. 项目定位与核心设计原则
本项目以 GitHub 热门开源项目 **[Minimal-Todo](https://github.com/avjinder/Minimal-Todo)** 为参考标杆，在本地从零构建一套轻量、纯粹且现代化的 Android 待办清单应用。

- **极简主义 (Minimalist & Focused)**：去除非必要功能，专注核心任务路径（创建、查看、勾选完成、滑动删除）。
- **纯本地离线 (100% Offline)**：数据直接保存在手机本地（Room / SQLite 本地数据库），无需登录、无云端网络依赖，秒开即用且彻底保障隐私。
- **现代化架构**：采用 Kotlin + 现代 Android 最佳实践开发，结构精炼易维护。

---

## 2. 参考标杆：Minimal-Todo 核心特色
- **仓库地址**：[avjinder/Minimal-Todo](https://github.com/avjinder/Minimal-Todo)（Verified: 2.5k+ Stars）
- **极简卡片与视觉标识**：任务卡片清晰直观，支持颜色主题标识。
- **流畅手势交互**：
  - 支持右滑/左滑快速删除任务（Swipe-to-Dismiss），并附带可撤销（Undo）提示。
  - 点击浮动操作按钮（FAB）快速调出新建待办界面。
- **零干扰体验**：没有复杂的层级菜单和广告，打开即是任务列表。

---

## 3. 功能规划与阶段划分

### Phase 1: 核心基础版本 (MVP)
1. **任务浏览与状态切换**：
   - 任务列表展示，区分未完成与已完成状态。
   - 单击复选框标记完成（附带划线动效与置底处理）。
2. **快速新增与编辑**：
   - 悬浮添加按钮（FAB），快速输入任务标题与详情。
   - 支持设置可选的任务提醒/截止日期时间。
3. **滑动清理与撤销**：
   - 支持滑动删除，底部弹出 Snackbar 提示撤销（Undo）。
4. **本地持久化引擎**：
   - 采用 Android 官方推荐的 Jetpack Room 数据库，完全离线可靠存储。

### Phase 2: 体验润色 (可选增强)
- 任务颜色标签区分。
- 暗色模式（Dark Theme）支持。
- 本地闹钟/通知提醒（基于 Android AlarmManager / WorkManager）。

---

## 4. 技术栈规划
- **开发语言**：Kotlin
- **本地存储**：Jetpack Room (SQLite ORM)
- **架构模式**：MVVM (ViewModel + Flow + Repository)
- **UI 方案**：Material Design 3 (Compose 或 传统 Views)
