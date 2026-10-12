package com.feng.suite.ui.dashboard

import android.content.Context
import com.feng.suite.feature.weather.data.City

data class WorldZone(
    val name: String,         // 中文城市名
    val nameEn: String,       // 英文城市名
    val country: String,      // 国家/地区
    val zoneId: String,       // 时区 ID
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

fun WorldZone.toCity(): City = City(
    name = name,
    latitude = latitude,
    longitude = longitude,
    province = country
)

fun getWorldClockCity(context: Context, index: Int): City {
    val prefs = context.getSharedPreferences("feng_dashboard_prefs", Context.MODE_PRIVATE)
    val defaultId = if (index == 1) "America/Los_Angeles" else "Europe/Berlin"
    val defaultName = if (index == 1) "旧金山" else "柏林"
    val zoneId = prefs.getString("world_clock_${index}_id", defaultId) ?: defaultId
    val zoneName = prefs.getString("world_clock_${index}_name", defaultName) ?: defaultName

    val matched = SELECTABLE_WORLD_ZONES.find { it.zoneId == zoneId || it.name == zoneName }
    return matched?.toCity() ?: City(
        name = zoneName,
        latitude = if (index == 1) 37.7749 else 52.5200,
        longitude = if (index == 1) -122.4194 else 13.4050,
        province = "外地"
    )
}

val SELECTABLE_WORLD_ZONES = listOf(
    // 亚太地区 - 东亚 & 港澳台
    WorldZone("北京", "Beijing", "中国", "Asia/Shanghai", 39.9042, 116.4074),
    WorldZone("上海", "Shanghai", "中国", "Asia/Shanghai", 31.2304, 121.4737),
    WorldZone("香港", "Hong Kong", "中国", "Asia/Hong_Kong", 22.3193, 114.1694),
    WorldZone("台北", "Taipei", "中国台湾", "Asia/Taipei", 25.0330, 121.5654),
    WorldZone("东京", "Tokyo", "日本", "Asia/Tokyo", 35.6762, 139.6503),
    WorldZone("大阪", "Osaka", "日本", "Asia/Tokyo", 34.6937, 135.5023),
    WorldZone("首尔", "Seoul", "韩国", "Asia/Seoul", 37.5665, 126.9780),
    WorldZone("乌兰巴托", "Ulaanbaatar", "蒙古", "Asia/Ulaanbaatar", 47.8864, 106.9057),

    // 亚太地区 - 东南亚 & 南亚
    WorldZone("新加坡", "Singapore", "新加坡", "Asia/Singapore", 1.3521, 103.8198),
    WorldZone("曼谷", "Bangkok", "泰国", "Asia/Bangkok", 13.7563, 100.5018),
    WorldZone("吉隆坡", "Kuala Lumpur", "马来西亚", "Asia/Kuala_Lumpur", 3.1390, 101.6869),
    WorldZone("雅加达", "Jakarta", "印度尼西亚", "Asia/Jakarta", -6.2088, 106.8456),
    WorldZone("马尼拉", "Manila", "菲律宾", "Asia/Manila", 14.5995, 120.9842),
    WorldZone("河内", "Hanoi", "越南", "Asia/Ho_Chi_Minh", 21.0285, 105.8542),
    WorldZone("胡志明市", "Ho Chi Minh City", "越南", "Asia/Ho_Chi_Minh", 10.8231, 106.6297),
    WorldZone("新德里", "New Delhi", "印度", "Asia/Kolkata", 28.6139, 77.2090),
    WorldZone("孟买", "Mumbai", "印度", "Asia/Kolkata", 19.0760, 72.8777),
    WorldZone("达卡", "Dhaka", "孟加拉国", "Asia/Dhaka", 23.8103, 90.4125),
    WorldZone("科伦坡", "Colombo", "斯里兰卡", "Asia/Colombo", 6.9271, 79.8612),
    WorldZone("仰光", "Yangon", "缅甸", "Asia/Yangon", 16.8661, 96.1951),

    // 中东 & 中亚
    WorldZone("迪拜", "Dubai", "阿联酋", "Asia/Dubai", 25.2048, 55.2708),
    WorldZone("阿布扎比", "Abu Dhabi", "阿联酋", "Asia/Dubai", 24.4539, 54.3773),
    WorldZone("多哈", "Doha", "卡塔尔", "Asia/Qatar", 25.2854, 51.5310),
    WorldZone("利雅得", "Riyadh", "沙特阿拉伯", "Asia/Riyadh", 24.7136, 46.6753),
    WorldZone("伊斯坦布尔", "Istanbul", "土耳其", "Europe/Istanbul", 41.0082, 28.9784),
    WorldZone("耶路撒冷", "Jerusalem", "以色列", "Asia/Jerusalem", 31.7683, 35.2137),
    WorldZone("德黑兰", "Tehran", "伊朗", "Asia/Tehran", 35.6892, 51.3890),
    WorldZone("塔什干", "Tashkent", "乌兹别克斯坦", "Asia/Tashkent", 41.2995, 69.2401),
    WorldZone("阿拉木图", "Almaty", "哈萨克斯坦", "Asia/Almaty", 43.2220, 76.8512),

    // 欧洲主要都市
    WorldZone("伦敦", "London", "英国", "Europe/London", 51.5074, -0.1278),
    WorldZone("巴黎", "Paris", "法国", "Europe/Paris", 48.8566, 2.3522),
    WorldZone("柏林", "Berlin", "德国", "Europe/Berlin", 52.5200, 13.4050),
    WorldZone("法兰克福", "Frankfurt", "德国", "Europe/Berlin", 50.1109, 8.6821),
    WorldZone("慕尼黑", "Munich", "德国", "Europe/Berlin", 48.1351, 11.5820),
    WorldZone("罗马", "Rome", "意大利", "Europe/Rome", 41.9028, 12.4964),
    WorldZone("米兰", "Milan", "意大利", "Europe/Rome", 45.4642, 9.1900),
    WorldZone("马德里", "Madrid", "西班牙", "Europe/Madrid", 40.4168, -3.7038),
    WorldZone("巴塞罗那", "Barcelona", "西班牙", "Europe/Madrid", 41.3851, 2.1734),
    WorldZone("阿姆斯特丹", "Amsterdam", "荷兰", "Europe/Amsterdam", 52.3676, 4.9041),
    WorldZone("苏黎世", "Zurich", "瑞士", "Europe/Zurich", 47.3769, 8.5417),
    WorldZone("日内瓦", "Geneva", "瑞士", "Europe/Zurich", 46.2044, 6.1432),
    WorldZone("维也纳", "Vienna", "奥地利", "Europe/Vienna", 48.2082, 16.3738),
    WorldZone("布鲁塞尔", "Brussels", "比利时", "Europe/Brussels", 50.8503, 4.3517),
    WorldZone("斯德哥尔摩", "Stockholm", "瑞典", "Europe/Stockholm", 59.3293, 18.0686),
    WorldZone("奥斯陆", "Oslo", "挪威", "Europe/Oslo", 59.9139, 10.7522),
    WorldZone("哥本哈根", "Copenhagen", "丹麦", "Europe/Copenhagen", 55.6761, 12.5683),
    WorldZone("赫尔辛基", "Helsinki", "芬兰", "Europe/Helsinki", 60.1699, 24.9384),
    WorldZone("雅典", "Athens", "希腊", "Europe/Athens", 37.9838, 23.7275),
    WorldZone("莫斯科", "Moscow", "俄罗斯", "Europe/Moscow", 55.7558, 37.6173),
    WorldZone("圣彼得堡", "St. Petersburg", "俄罗斯", "Europe/Moscow", 59.9311, 30.3609),
    WorldZone("华沙", "Warsaw", "波兰", "Europe/Warsaw", 52.2297, 21.0122),
    WorldZone("布拉格", "Prague", "捷克", "Europe/Prague", 50.0755, 14.4378),
    WorldZone("布达佩斯", "Budapest", "匈牙利", "Europe/Budapest", 47.4979, 19.0402),
    WorldZone("里斯本", "Lisbon", "葡萄牙", "Europe/Lisbon", 38.7223, -9.1393),
    WorldZone("都柏林", "Dublin", "爱尔兰", "Europe/Dublin", 53.3498, -6.2603),
    WorldZone("雷克雅未克", "Reykjavik", "冰岛", "Atlantic/Reykjavik", 64.1466, -21.9426),
    WorldZone("基辅", "Kyiv", "乌克兰", "Europe/Kiev", 50.4501, 30.5234),

    // 北美洲主要城市
    WorldZone("纽约", "New York", "美国", "America/New_York", 40.7128, -74.0060),
    WorldZone("旧金山", "San Francisco", "美国", "America/Los_Angeles", 37.7749, -122.4194),
    WorldZone("洛杉矶", "Los Angeles", "美国", "America/Los_Angeles", 34.0522, -118.2437),
    WorldZone("西雅图", "Seattle", "美国", "America/Los_Angeles", 47.6062, -122.3321),
    WorldZone("芝加哥", "Chicago", "美国", "America/Chicago", 41.8781, -87.6298),
    WorldZone("华盛顿", "Washington, D.C.", "美国", "America/New_York", 38.9072, -77.0369),
    WorldZone("波士顿", "Boston", "美国", "America/New_York", 42.3601, -71.0589),
    WorldZone("休斯顿", "Houston", "美国", "America/Chicago", 29.7604, -95.3698),
    WorldZone("达拉斯", "Dallas", "美国", "America/Chicago", 32.7767, -96.7970),
    WorldZone("丹佛", "Denver", "美国", "America/Denver", 39.7392, -104.9903),
    WorldZone("凤凰城", "Phoenix", "美国", "America/Phoenix", 33.4484, -112.0740),
    WorldZone("迈阿密", "Miami", "美国", "America/New_York", 25.7617, -80.1918),
    WorldZone("亚特兰大", "Atlanta", "美国", "America/New_York", 33.7490, -84.3880),
    WorldZone("拉斯维加斯", "Las Vegas", "美国", "America/Los_Angeles", 36.1699, -115.1398),
    WorldZone("多伦多", "Toronto", "加拿大", "America/Toronto", 43.6532, -79.3832),
    WorldZone("温哥华", "Vancouver", "加拿大", "America/Vancouver", 49.2827, -123.1207),
    WorldZone("蒙特利尔", "Montreal", "加拿大", "America/Toronto", 45.5017, -73.5673),
    WorldZone("墨西哥城", "Mexico City", "墨西哥", "America/Mexico_City", 19.4326, -99.1332),
    WorldZone("檀香山", "Honolulu", "美国夏威夷", "Pacific/Honolulu", 21.3069, -157.8583),
    WorldZone("安克雷奇", "Anchorage", "美国阿拉斯加", "America/Anchorage", 61.2181, -149.9003),

    // 南美洲
    WorldZone("圣保罗", "Sao Paulo", "巴西", "America/Sao_Paulo", -23.5505, -46.6333),
    WorldZone("里约热内卢", "Rio de Janeiro", "巴西", "America/Sao_Paulo", -22.9068, -43.1729),
    WorldZone("布宜诺斯艾利斯", "Buenos Aires", "阿根廷", "America/Argentina/Buenos_Aires", -34.6037, -58.3816),
    WorldZone("圣地亚哥", "Santiago", "智利", "America/Santiago", -33.4489, -70.6693),
    WorldZone("利马", "Lima", "秘鲁", "America/Lima", -12.0464, -77.0428),
    WorldZone("波哥大", "Bogota", "哥伦比亚", "America/Bogota", 4.7110, -74.0721),

    // 大洋洲
    WorldZone("悉尼", "Sydney", "澳大利亚", "Australia/Sydney", -33.8688, 151.2093),
    WorldZone("墨尔本", "Melbourne", "澳大利亚", "Australia/Melbourne", -37.8136, 144.9631),
    WorldZone("布里斯班", "Brisbane", "澳大利亚", "Australia/Brisbane", -27.4698, 153.0251),
    WorldZone("珀斯", "Perth", "澳大利亚", "Australia/Perth", -31.9505, 115.8605),
    WorldZone("阿德莱德", "Adelaide", "澳大利亚", "Australia/Adelaide", -34.9285, 138.6007),
    WorldZone("奥克兰", "Auckland", "新西兰", "Pacific/Auckland", -36.8485, 174.7633),
    WorldZone("惠灵顿", "Wellington", "新西兰", "Pacific/Auckland", -41.2865, 174.7762),
    WorldZone("斐济", "Suva", "斐济", "Pacific/Fiji", -18.1416, 178.4419),

    // 非洲
    WorldZone("开罗", "Cairo", "埃及", "Africa/Cairo", 30.0444, 31.2357),
    WorldZone("约翰内斯堡", "Johannesburg", "南非", "Africa/Johannesburg", -26.2041, 28.0473),
    WorldZone("开普敦", "Cape Town", "南非", "Africa/Johannesburg", -33.9249, 18.4241),
    WorldZone("内罗毕", "Nairobi", "肯尼亚", "Africa/Nairobi", -1.2921, 36.8219),
    WorldZone("卡萨布兰卡", "Casablanca", "摩洛哥", "Africa/Casablanca", 33.5731, -7.5898),
    WorldZone("拉各斯", "Lagos", "尼日利亚", "Africa/Lagos", 6.5244, 3.3792),
    WorldZone("亚的斯亚贝巴", "Addis Ababa", "埃塞俄比亚", "Africa/Addis_Ababa", 9.0300, 38.7400)
)
