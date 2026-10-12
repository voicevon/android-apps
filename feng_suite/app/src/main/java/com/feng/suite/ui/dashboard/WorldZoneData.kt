package com.feng.suite.ui.dashboard

data class WorldZone(
    val name: String,         // 中文城市名
    val nameEn: String,       // 英文城市名
    val country: String,      // 国家/地区
    val zoneId: String        // 时区 ID
)

val SELECTABLE_WORLD_ZONES = listOf(
    // 亚太地区 - 东亚 & 港澳台
    WorldZone("北京", "Beijing", "中国", "Asia/Shanghai"),
    WorldZone("上海", "Shanghai", "中国", "Asia/Shanghai"),
    WorldZone("香港", "Hong Kong", "中国", "Asia/Hong_Kong"),
    WorldZone("台北", "Taipei", "中国台湾", "Asia/Taipei"),
    WorldZone("东京", "Tokyo", "日本", "Asia/Tokyo"),
    WorldZone("大阪", "Osaka", "日本", "Asia/Tokyo"),
    WorldZone("首尔", "Seoul", "韩国", "Asia/Seoul"),
    WorldZone("乌兰巴托", "Ulaanbaatar", "蒙古", "Asia/Ulaanbaatar"),

    // 亚太地区 - 东南亚 & 南亚
    WorldZone("新加坡", "Singapore", "新加坡", "Asia/Singapore"),
    WorldZone("曼谷", "Bangkok", "泰国", "Asia/Bangkok"),
    WorldZone("吉隆坡", "Kuala Lumpur", "马来西亚", "Asia/Kuala_Lumpur"),
    WorldZone("雅加达", "Jakarta", "印度尼西亚", "Asia/Jakarta"),
    WorldZone("马尼拉", "Manila", "菲律宾", "Asia/Manila"),
    WorldZone("河内", "Hanoi", "越南", "Asia/Ho_Chi_Minh"),
    WorldZone("胡志明市", "Ho Chi Minh City", "越南", "Asia/Ho_Chi_Minh"),
    WorldZone("新德里", "New Delhi", "印度", "Asia/Kolkata"),
    WorldZone("孟买", "Mumbai", "印度", "Asia/Kolkata"),
    WorldZone("达卡", "Dhaka", "孟加拉国", "Asia/Dhaka"),
    WorldZone("科伦坡", "Colombo", "斯里兰卡", "Asia/Colombo"),
    WorldZone("仰光", "Yangon", "缅甸", "Asia/Yangon"),

    // 中东 & 中亚
    WorldZone("迪拜", "Dubai", "阿联酋", "Asia/Dubai"),
    WorldZone("阿布扎比", "Abu Dhabi", "阿联酋", "Asia/Dubai"),
    WorldZone("多哈", "Doha", "卡塔尔", "Asia/Qatar"),
    WorldZone("利雅得", "Riyadh", "沙特阿拉伯", "Asia/Riyadh"),
    WorldZone("伊斯坦布尔", "Istanbul", "土耳其", "Europe/Istanbul"),
    WorldZone("耶路撒冷", "Jerusalem", "以色列", "Asia/Jerusalem"),
    WorldZone("德黑兰", "Tehran", "伊朗", "Asia/Tehran"),
    WorldZone("塔什干", "Tashkent", "乌兹别克斯坦", "Asia/Tashkent"),
    WorldZone("阿拉木图", "Almaty", "哈萨克斯坦", "Asia/Almaty"),

    // 欧洲主要都市
    WorldZone("伦敦", "London", "英国", "Europe/London"),
    WorldZone("巴黎", "Paris", "法国", "Europe/Paris"),
    WorldZone("柏林", "Berlin", "德国", "Europe/Berlin"),
    WorldZone("法兰克福", "Frankfurt", "德国", "Europe/Berlin"),
    WorldZone("慕尼黑", "Munich", "德国", "Europe/Berlin"),
    WorldZone("罗马", "Rome", "意大利", "Europe/Rome"),
    WorldZone("米兰", "Milan", "意大利", "Europe/Rome"),
    WorldZone("马德里", "Madrid", "西班牙", "Europe/Madrid"),
    WorldZone("巴塞罗那", "Barcelona", "西班牙", "Europe/Madrid"),
    WorldZone("阿姆斯特丹", "Amsterdam", "荷兰", "Europe/Amsterdam"),
    WorldZone("苏黎世", "Zurich", "瑞士", "Europe/Zurich"),
    WorldZone("日内瓦", "Geneva", "瑞士", "Europe/Zurich"),
    WorldZone("维也纳", "Vienna", "奥地利", "Europe/Vienna"),
    WorldZone("布鲁塞尔", "Brussels", "比利时", "Europe/Brussels"),
    WorldZone("斯德哥尔摩", "Stockholm", "瑞典", "Europe/Stockholm"),
    WorldZone("奥斯陆", "Oslo", "挪威", "Europe/Oslo"),
    WorldZone("哥本哈根", "Copenhagen", "丹麦", "Europe/Copenhagen"),
    WorldZone("赫尔辛基", "Helsinki", "芬兰", "Europe/Helsinki"),
    WorldZone("雅典", "Athens", "希腊", "Europe/Athens"),
    WorldZone("莫斯科", "Moscow", "俄罗斯", "Europe/Moscow"),
    WorldZone("圣彼得堡", "St. Petersburg", "俄罗斯", "Europe/Moscow"),
    WorldZone("华沙", "Warsaw", "波兰", "Europe/Warsaw"),
    WorldZone("布拉格", "Prague", "捷克", "Europe/Prague"),
    WorldZone("布达佩斯", "Budapest", "匈牙利", "Europe/Budapest"),
    WorldZone("里斯本", "Lisbon", "葡萄牙", "Europe/Lisbon"),
    WorldZone("都柏林", "Dublin", "爱尔兰", "Europe/Dublin"),
    WorldZone("雷克雅未克", "Reykjavik", "冰岛", "Atlantic/Reykjavik"),
    WorldZone("基辅", "Kyiv", "乌克兰", "Europe/Kiev"), // Europe/Kyiv 在 API 26-28 旧 tzdata 中不存在，会静默变为 GMT

    // 北美洲主要城市
    WorldZone("纽约", "New York", "美国", "America/New_York"),
    WorldZone("旧金山", "San Francisco", "美国", "America/Los_Angeles"),
    WorldZone("洛杉矶", "Los Angeles", "美国", "America/Los_Angeles"),
    WorldZone("西雅图", "Seattle", "美国", "America/Los_Angeles"),
    WorldZone("芝加哥", "Chicago", "美国", "America/Chicago"),
    WorldZone("华盛顿", "Washington, D.C.", "美国", "America/New_York"),
    WorldZone("波士顿", "Boston", "美国", "America/New_York"),
    WorldZone("休斯顿", "Houston", "美国", "America/Chicago"),
    WorldZone("达拉斯", "Dallas", "美国", "America/Chicago"),
    WorldZone("丹佛", "Denver", "美国", "America/Denver"),
    WorldZone("凤凰城", "Phoenix", "美国", "America/Phoenix"),
    WorldZone("迈阿密", "Miami", "美国", "America/New_York"),
    WorldZone("亚特兰大", "Atlanta", "美国", "America/New_York"),
    WorldZone("拉斯维加斯", "Las Vegas", "美国", "America/Los_Angeles"),
    WorldZone("多伦多", "Toronto", "加拿大", "America/Toronto"),
    WorldZone("温哥华", "Vancouver", "加拿大", "America/Vancouver"),
    WorldZone("蒙特利尔", "Montreal", "加拿大", "America/Toronto"),
    WorldZone("墨西哥城", "Mexico City", "墨西哥", "America/Mexico_City"),
    WorldZone("檀香山", "Honolulu", "美国夏威夷", "Pacific/Honolulu"),
    WorldZone("安克雷奇", "Anchorage", "美国阿拉斯加", "America/Anchorage"),

    // 南美洲
    WorldZone("圣保罗", "Sao Paulo", "巴西", "America/Sao_Paulo"),
    WorldZone("里约热内卢", "Rio de Janeiro", "巴西", "America/Sao_Paulo"),
    WorldZone("布宜诺斯艾利斯", "Buenos Aires", "阿根廷", "America/Argentina/Buenos_Aires"),
    WorldZone("圣地亚哥", "Santiago", "智利", "America/Santiago"),
    WorldZone("利马", "Lima", "秘鲁", "America/Lima"),
    WorldZone("波哥大", "Bogota", "哥伦比亚", "America/Bogota"),

    // 大洋洲
    WorldZone("悉尼", "Sydney", "澳大利亚", "Australia/Sydney"),
    WorldZone("墨尔本", "Melbourne", "澳大利亚", "Australia/Melbourne"),
    WorldZone("布里斯班", "Brisbane", "澳大利亚", "Australia/Brisbane"),
    WorldZone("珀斯", "Perth", "澳大利亚", "Australia/Perth"),
    WorldZone("阿德莱德", "Adelaide", "澳大利亚", "Australia/Adelaide"),
    WorldZone("奥克兰", "Auckland", "新西兰", "Pacific/Auckland"),
    WorldZone("惠灵顿", "Wellington", "新西兰", "Pacific/Auckland"),
    WorldZone("斐济", "Suva", "斐济", "Pacific/Fiji"),

    // 非洲
    WorldZone("开罗", "Cairo", "埃及", "Africa/Cairo"),
    WorldZone("约翰内斯堡", "Johannesburg", "南非", "Africa/Johannesburg"),
    WorldZone("开普敦", "Cape Town", "南非", "Africa/Johannesburg"),
    WorldZone("内罗毕", "Nairobi", "肯尼亚", "Africa/Nairobi"),
    WorldZone("卡萨布兰卡", "Casablanca", "摩洛哥", "Africa/Casablanca"),
    WorldZone("拉各斯", "Lagos", "尼日利亚", "Africa/Lagos"),
    WorldZone("亚的斯亚贝巴", "Addis Ababa", "埃塞俄比亚", "Africa/Addis_Ababa")
)
