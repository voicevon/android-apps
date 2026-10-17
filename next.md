2. Chat 显示发送状态，断线时先排队（Chat + MQTT）
现状：

ChatViewModel.sendMessage
 先存数据库再发布，不管发没发成功。断线时消息看起来发出去了，其实丢了。
目标：在 ChatMessageEntity 里加一个状态字段（发送中 / 已发送 / 失败）。重新连上后自动补发失败的消息，界面上用小图标显示状态。




5. 补上第一批单元测试（基础建设）
现状：项目里还没有任何测试，而且 3 个 Room 数据库都开了 fallbackToDestructiveMigration。以后一旦改表结构（比如目标 1、2），旧数据会被直接清空。
目标：
给 AlarmEntity 计算下次响铃时间的逻辑写纯 JVM 测试。
给 MQTT 报文的序列化 / 反序列化写纯 JVM 测试。
决定数据库是否改成显式的 Migration。



