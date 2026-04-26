// package com.xiyao.basic.utils;
//
// import cn.hutool.core.util.StrUtil;
// import cn.hutool.http.HttpRequest;
// import cn.hutool.json.JSONObject;
// import cn.hutool.json.JSONUtil;
// import jakarta.servlet.http.HttpServletRequest;
//
// public class IpLocationUtil {
//
//     // 太平洋 IP 查询接口（返回 GBK 编码的 JSON）
//     private static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";
//
//     /**
//      * 获取客户端真实 IP（适配反向代理）
//      */
//     public static String getClientIp(HttpServletRequest request) {
//         String ip = request.getHeader("X-Forwarded-For");
//         if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
//             ip = request.getHeader("Proxy-Client-IP");
//         }
//         if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
//             ip = request.getHeader("WL-Proxy-Client-IP");
//         }
//         if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
//             ip = request.getRemoteAddr();
//         }
//         // 多个代理时取第一个 IP
//         if (ip != null && ip.contains(",")) {
//             ip = ip.split(",")[0];
//         }
//         // 处理本机回环地址
//         if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
//             ip = "127.0.0.1";
//         }
//         return ip;
//     }
//
//     /**
//      * 根据 IP 获取地理位置（省/市）
//      */
//     public static String getLocationByIp(String ip) {
//         if (StrUtil.isBlank(ip) || isInnerIp(ip)) {
//             return "内网IP";
//         }
//         try {
//             // 使用 Hutool 发送 GET 请求，指定超时和编码（太平洋接口返回 GBK）
//             String response = HttpRequest.get(IP_URL)
//                     .timeout(5000)
//                     .header("User-Agent", "Mozilla/5.0")
//                     .execute()
//                     .bodyCharset("GBK");   // 关键：指定字符集为 GBK
//
//             if (StrUtil.isNotBlank(response)) {
//                 // 响应格式：var returnCitySN = {"cip": "xxx", "pro": "xxx", "city": "xxx"};
//                 String jsonStr = response
//                         .replace("var returnCitySN = ", "")
//                         .replace(";", "")
//                         .trim();
//                 JSONObject json = JSONUtil.parseObj(jsonStr);
//                 String province = json.getStr("pro", "");
//                 String city = json.getStr("city", "");
//                 if (StrUtil.isNotBlank(city) && !"null".equals(city)) {
//                     if (province.equals(city)) {
//                         return city;
//                     }
//                     return province + " " + city;
//                 }
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//         return "未知";
//     }
//
//     /**
//      * 组合方法：直接返回客户端 IP + 地址
//      */
//     public static String getClientIpAndLocation(HttpServletRequest request) {
//         String ip = getClientIp(request);
//         String location = getLocationByIp(ip);
//         return ip + " (" + location + ")";
//     }
//
//     // 简单判断内网 IP（可按需扩充）
//     private static boolean isInnerIp(String ip) {
//         return ip.startsWith("10.") || ip.startsWith("192.168.") ||
//                 ip.startsWith("172.") || "127.0.0.1".equals(ip);
//     }
// }