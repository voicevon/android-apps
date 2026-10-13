# 冯氏套件 MQTT 通讯协议规范 (MQTT Protocol Specification)

本规范为 **冯氏套件 (Feng Suite)** 多端协同、即时消息、设备心跳及闹钟跨端控制的单一真理源（SSOT）。所有端侧设备（Android 手机、平板、桌面端及嵌入式客户端）必须严格遵循此协议规范收发数据。

---

## 1. Broker 连接基准配置

| 配置项 | 规范值 / 默认值 | 说明 |
| :--- | :--- | :--- |
| **服务器地址 (Host)** | `voicevon.vicp.io` | 公网/局域网 MQTT Broker 地址 |
| **端口 (Port)** | `1883` | 标准 TCP 端口 |
| **传输协议** | TCP / MQTT v3.1.1 | 支持自动重连 (`isAutomaticReconnect = true`) |
| **客户端 Client ID** | `feng_suite_{8位随机UUID}` | 保证多端 Client ID 唯一不冲突 |
| **服务质量等级 (QoS)** | `QoS 1` | 至少送达一次，保障多端指令可靠到达 |
| **编码格式** | `UTF-8` | Payload 统一为 UTF-8 编码的 JSON 字符串 |

---

## 2. Topic 架构与命名空间

所有 Topic 统一以 `feng_suite/` 作为根命名空间：

| 业务分类 | Topic 路径 | 方向 | 作用 |
| :--- | :--- | :--- | :--- |
| **设备心跳** | `feng_suite/heartbeat` | 发布 / 订阅 | 设备定期在线心跳上报 |
| **即时消息广播** | `feng_suite/chat/broadcast` | 发布 / 订阅 | 全员群聊与多端便签广播 |
| **即时消息定向** | `feng_suite/chat/device/{deviceName}` | 发布 / 订阅 | 指定发送给某台特定设备 |
| **闹钟协同广播** | `feng_suite/alarm/broadcast` | 发布 / 订阅 | 闹钟增删改查跨设备全网同步 |
| **闹钟协同定向** | `feng_suite/alarm/device/{deviceName}` | 发布 / 订阅 | 指定发送给某台特定设备响铃/控制 |

---

## 3. 业务报文定义 (Payload JSON 字段)

### 3.1 设备在线心跳报文 (`feng_suite/heartbeat`)
- **触发频率**：每 1 小时定期上报一次（以及连接成功时立即上报一次首发心跳）。
- **Payload 示例**：
```json
{
  "msgType": "HEARTBEAT",
  "device": "设备-AL80",
  "timestamp": 1728812345000,
  "status": "ONLINE"
}
```
- **字段规范**：
  - `msgType` (String, 必须): 固定为 `"HEARTBEAT"`。
  - `device` (String, 必须): 发送心跳的本机设备名称（如 `客厅平板`、`设备-AL80`）。
  - `timestamp` (Long, 必须): 心跳生成毫秒时间戳。
  - `status` (String, 必须): `"ONLINE"`（在线）。

---

### 3.2 即时消息报文 (`feng_suite/chat/broadcast` 或 `feng_suite/chat/device/{target}`)
- **触发时机**：用户在 Dashboard 快捷单行框或 Chat 全屏页面点击发送时。
- **Payload 示例**：
```json
{
  "msgType": "CHAT",
  "id": "e9b5f8c2-3a81-4b72-a0c3-f61b782b1234",
  "sender": "客厅平板",
  "target": "全部设备",
  "content": "水烧开了，记得关一下。",
  "timestamp": 1728812350000
}
```
- **字段规范**：
  - `msgType` (String, 必须): 固定为 `"CHAT"`。
  - `id` (String, 必须): 消息唯一标识符（UUID）。
  - `sender` (String, 必须): 发送方设备名称。
  - `target` (String, 必须): 接收方目标设备名（广播为 `"全部设备"`，定向为目标设备名）。
  - `content` (String, 必须): 聊天消息正文。
  - `timestamp` (Long, 必须): 发送毫秒时间戳。
- **回环处理**：接收端校验若 `sender == 本机设备名`，直接静默丢弃，避免本端重复回显。

---

### 3.3 闹钟跨端同步报文 (`feng_suite/alarm/broadcast` 或 `feng_suite/alarm/device/{target}`)
- **触发时机**：新建闹钟、编辑时间/重复周期、开关切换或删除闹钟时。
- **Payload 示例**：
```json
{
  "action": "CREATE_OR_UPDATE",
  "sourceDevice": "客厅平板",
  "targetDevice": "全部设备",
  "id": 1001,
  "hour": 7,
  "minute": 30,
  "isEnabled": true,
  "daysBitmask": 62,
  "label": "晨跑起床",
  "isVibrate": true,
  "snoozeMinutes": 10,
  "timestamp": 1728812360000
}
```
- **字段规范**：
  - `action` (String, 必须): 操作动作：
    - `"CREATE_OR_UPDATE"`: 新增或修改闹钟
    - `"TOGGLE"`: 开启/关闭闹钟状态
    - `"DELETE"`: 远端删除闹钟
  - `sourceDevice` (String, 必须): 发送控制指令的来源设备名。
  - `targetDevice` (String, 必须): 目标设备名（`"全部设备"` 或 指定设备）。
  - `hour` (Int, 必须): 闹钟小时 (0..23)。
  - `minute` (Int, 必须): 闹钟分钟 (0..59)。
  - `isEnabled` (Boolean, 必须): 闹钟当前是否启用。
  - `daysBitmask` (Int, 必须): 星期重复位掩码（0为仅一次，127为每天）。
  - `label` (String, 可选): 闹钟备注标签文本。
  - `isVibrate` (Boolean, 必须): 是否振动。
  - `snoozeMinutes` (Int, 必须): 贪睡间隔分钟数。
  - `timestamp` (Long, 必须): 操作毫秒时间戳。

---

## 4. 架构统一说明

在整个代码库中，`feng_suite` 统一以 `feature` 分层组织所有核心业务：
- `com.feng.suite.feature.clock`：时钟、精准响铃、计时器、秒表
- `com.feng.suite.feature.weather`：气象、城市定位与多时区
- `com.feng.suite.feature.todo`：四色待办、拖拽排序
- `com.feng.suite.feature.chat`：多端即时消息与本地历史存储
- `com.feng.suite.core.mqtt`：MQTT 通信管理器、心跳 Worker 与连接设置
