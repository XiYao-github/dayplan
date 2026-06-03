package com.xiyao.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 测试数据生成工具类
 * <p>
 * 提供各种测试数据的随机生成功能，用于单元测试、压力测试、演示数据等场景。
 *
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *     <li>基础随机：字符串、数字、布尔值</li>
 *     <li>个人数据：姓名、手机号、身份证、邮箱</li>
 *     <li>企业数据：公司名、税号、银行账号</li>
 *     <li>地址数据：省市区、详细地址</li>
 *     <li>日期数据：生日、日期范围</li>
 *     <li>批量生成：生成指定数量的测试数据</li>
 * </ul>
 *
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 生成随机字符串
 * String randomStr = TestDataUtils.randomString(16);
 *
 * // 生成手机号
 * String phone = TestDataUtils.randomPhone();
 *
 * // 生成身份证号
 * String idCard = TestDataUtils.randomIdCard();
 *
 * // 生成姓名
 * String name = TestDataUtils.randomName();
 *
 * // 生成邮箱
 * String email = TestDataUtils.randomEmail("user");
 *
 * // 批量生成用户数据
 * List<Map<String, Object>> users = TestDataUtils.generateUsers(100);
 * }</pre>
 *
 * @author xiyao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataUtils {

    /**
     * 姓氏列表
     */
    private static final String[] SURNAMES = {
            "王", "李", "张", "刘", "陈", "杨", "赵", "黄", "周", "吴",
            "徐", "孙", "胡", "朱", "高", "林", "何", "郭", "马", "罗",
            "梁", "宋", "郑", "谢", "韩", "唐", "冯", "于", "董", "萧",
            "程", "曹", "袁", "邓", "许", "傅", "沈", "曾", "彭", "吕"
    };

    /**
     * 名字中间字列表
     */
    private static final String[] MIDDLE_NAMES = {
            "伟", "芳", "娜", "秀", "敏", "静", "丽", "强", "磊", "军",
            "洋", "勇", "艳", "杰", "涛", "明", "超", "秀", "霞", "平",
            "刚", "桂", "英", "华", "建", "云", "海", "雪", "梅", "小"
    };

    /**
     * 名字结尾字列表
     */
    private static final String[] END_NAMES = {
            "英", "华", "琳", "军", "涛", "明", "秀", "兰", "梅", "芳",
            "杰", "娟", "丽", "敏", "静", "超", "勇", "强", "磊", "伟",
            "霞", "平", "红", "云", "飞", "龙", "凤", "香", "素", "珍"
    };

    /**
     * 省份列表
     */
    private static final String[] PROVINCES = {
            "北京市", "天津市", "上海市", "重庆市", "河北省", "山西省",
            "辽宁省", "吉林省", "黑龙江省", "江苏省", "浙江省", "安徽省",
            "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省",
            "广东省", "海南省", "四川省", "贵州省", "云南省", "陕西省",
            "甘肃省", "青海省", "台湾省", "内蒙古自治区", "广西壮族自治区",
            "西藏自治区", "宁夏回族自治区", "新疆维吾尔自治区"
    };

    /**
     * 城市列表（简化版）
     */
    private static final String[] CITIES = {
            "北京市", "上海市", "天津市", "重庆市",
            "南京市", "杭州市", "苏州市", "无锡市", "常州市", "南通市",
            "成都市", "绵阳市", "德阳市", "宜宾市", "南充市",
            "广州市", "深圳市", "佛山市", "东莞市", "珠海市",
            "武汉市", "长沙市", "郑州市", "石家庄市", "保定市",
            "西安市", "咸阳市", "宝鸡市", "安康市", "铜川市"
    };

    /**
     * 随机数生成器
     */
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==================== 基础随机方法 ====================

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        return randomString(length, chars);
    }

    /**
     * 生成随机字符串（指定字符集）
     *
     * @param length 长度
     * @param chars  字符集
     * @return 随机字符串
     */
    public static String randomString(int length, String chars) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成随机数字字符串
     *
     * @param length 长度
     * @return 数字字符串
     */
    public static String randomNumeric(int length) {
        String chars = "0123456789";
        return randomString(length, chars);
    }

    /**
     * 生成随机整数
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机整数
     */
    public static int randomInt(int min, int max) {
        return RANDOM.nextInt(min, max + 1);
    }

    /**
     * 生成随机长整数
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机长整数
     */
    public static long randomLong(long min, long max) {
        return RANDOM.nextLong(min, max + 1);
    }

    /**
     * 生成随机布尔值
     *
     * @return true 或 false
     */
    public static boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }

    /**
     * 从数组中随机选择一个元素
     *
     * @param array 数组
     * @param <T>   元素类型
     * @return 随机元素
     */
    @SafeVarargs
    public static <T> T randomOne(T... array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[RANDOM.nextInt(array.length)];
    }

    // ==================== 个人数据方法 ====================

    /**
     * 生成随机姓名
     *
     * @return 姓名（如 "张三"、"李四"）
     */
    public static String randomName() {
        return randomOne(SURNAMES) + randomOne(MIDDLE_NAMES) + randomOne(END_NAMES);
    }

    /**
     * 生成随机手机号
     *
     * @return 手机号（如 "13812345678"）
     */
    public static String randomPhone() {
        // 中国手机号段（简化版）
        String[] prefixes = {
                "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
                "147", "150", "151", "152", "153", "155", "156", "157", "158", "159",
                "166", "170", "171", "172", "173", "175", "176", "177", "178", "179",
                "180", "181", "182", "183", "184", "185", "186", "187", "188", "189"
        };
        return randomOne(prefixes) + randomNumeric(8);
    }

    /**
     * 生成随机身份证号
     *
     * @return 身份证号（18位）
     */
    public static String randomIdCard() {
        // 随机生成生日
        int year = randomInt(1960, 2000);
        int month = randomInt(1, 12);
        int day = randomInt(1, 28); // 简化处理
        LocalDate birthday = LocalDate.of(year, month, day);

        // 随机生成地区码（使用武汉市某区）
        String areaCode = "420106";

        // 顺序码（3位数字）
        String sequence = randomNumeric(3);

        // 计算校验位
        String base17 = areaCode + birthday.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + sequence;
        char checkCode = calculateIdCardCheckCode(base17);

        return base17 + checkCode;
    }

    /**
     * 计算身份证校验位
     *
     * @param base17 17位基础码
     * @return 校验位字符
     */
    private static char calculateIdCardCheckCode(String base17) {
        int[] weight = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodes = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (base17.charAt(i) - '0') * weight[i];
        }

        return checkCodes[sum % 11];
    }

    /**
     * 生成随机邮箱
     *
     * @param prefix 用户名前缀（可选）
     * @return 邮箱地址
     */
    public static String randomEmail(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = randomString(randomInt(4, 10));
        }

        String[] domains = {
                "qq.com", "163.com", "126.com", "gmail.com", "outlook.com",
                "hotmail.com", "sina.com", "sohu.com", "139.com", "189.com"
        };

        return prefix + "@" + randomOne(domains);
    }

    /**
     * 生成随机邮箱（自动生成前缀）
     *
     * @return 邮箱地址
     */
    public static String randomEmail() {
        return randomEmail(null);
    }

    // ==================== 地址数据方法 ====================

    /**
     * 生成随机省份
     *
     * @return 省份名称
     */
    public static String randomProvince() {
        return randomOne(PROVINCES);
    }

    /**
     * 生成随机城市
     *
     * @return 城市名称
     */
    public static String randomCity() {
        return randomOne(CITIES);
    }

    /**
     * 生成随机地址
     *
     * @return 完整地址
     */
    public static String randomAddress() {
        String province = randomProvince();
        String city = randomCity();
        String district = randomOne("江岸区", "江汉区", "武昌区", "汉阳区", "青山区",
                "洪山区", "东新区", "经开区");
        String street = randomOne("解放大道", "中山大道", "长江大道", "汉正街", "光谷街");
        int number = randomInt(1, 999);
        return province + city + district + street + number + "号";
    }

    // ==================== 日期数据方法 ====================

    /**
     * 生成随机日期
     *
     * @param startYear 起始年份
     * @param endYear   结束年份
     * @return 日期字符串（yyyy-MM-dd）
     */
    public static String randomDate(int startYear, int endYear) {
        int year = randomInt(startYear, endYear);
        int month = randomInt(1, 12);
        int day = randomInt(1, 28);
        return LocalDate.of(year, month, day).format(DATE_FORMATTER);
    }

    /**
     * 生成随机生日
     *
     * @return 生日日期（yyyy-MM-dd）
     */
    public static String randomBirthday() {
        return randomDate(1960, 2000);
    }

    // ==================== 企业数据方法 ====================

    /**
     * 生成随机公司名
     *
     * @return 公司名
     */
    public static String randomCompany() {
        String[] prefixes = {"北京", "上海", "深圳", "广州", "杭州", "武汉", "成都", "南京"};
        String[] names = {"华", "中", "金", "银", "祥", "龙", "凤", "宇"};
        String[] types = {"科技有限公司", "实业有限公司", "贸易有限公司", "网络科技有限公司"};
        return randomOne(prefixes) + randomOne(names) + randomOne(names) + randomOne(types);
    }

    /**
     * 生成随机税号
     *
     * @return 税号（18位）
     */
    public static String randomTaxId() {
        return randomNumeric(17) + Arrays.toString(randomOne("0123456789X".toCharArray()));
    }

    /**
     * 生成随机银行卡号
     *
     * @return 银行卡号（19位）
     */
    public static String randomBankCard() {
        // 生成 16 位卡号
        String prefix = randomOne("4", "5", "6", "62"); // Visa, MasterCard, etc.
        String card16 = prefix + randomNumeric(15);

        // Luhn 算法校验位
        int checkDigit = calculateLuhnCheckDigit(card16);
        return card16 + checkDigit;
    }

    /**
     * 计算 Luhn 校验位
     *
     * @param number 16 位数字串
     * @return 校验位
     */
    private static int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    // ==================== 批量生成方法 ====================

    /**
     * 生成批量用户数据
     *
     * @param count 数量
     * @return 用户数据列表
     */
    public static List<Map<String, Object>> generateUsers(int count) {
        List<Map<String, Object>> users = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", (long) (i + 1));
            user.put("username", "user" + randomNumeric(6));
            user.put("password", "123456"); // 默认密码
            user.put("nickName", randomName());
            user.put("mobile", randomPhone());
            user.put("email", randomEmail());
            user.put("sex", randomOne(0, 1, 2));
            user.put("status", 1);
            user.put("createTime", randomDate(2020, 2024));
            users.add(user);
        }
        return users;
    }

    /**
     * 生成批量订单数据
     *
     * @param count 数量
     * @return 订单数据列表
     */
    public static List<Map<String, Object>> generateOrders(int count) {
        List<Map<String, Object>> orders = new ArrayList<>(count);
        String[] statuses = {"待支付", "已支付", "已发货", "已完成", "已取消"};

        for (int i = 0; i < count; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("id", (long) (i + 1));
            order.put("orderNo", "ORD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + randomNumeric(6));
            order.put("userId", (long) randomInt(1, 1000));
            order.put("amount", randomInt(10, 10000));
            order.put("status", randomOne(statuses));
            order.put("createTime", randomDate(2024, 2024));
            orders.add(order);
        }
        return orders;
    }

    /**
     * 生成手机号列表
     *
     * @param count 数量
     * @return 手机号列表
     */
    public static List<String> generatePhones(int count) {
        List<String> phones = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            phones.add(randomPhone());
        }
        return phones;
    }
}