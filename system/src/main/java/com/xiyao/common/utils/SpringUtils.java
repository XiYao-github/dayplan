package com.xiyao.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Spring Context 工具类
 * <p>
 * 提供对 Spring 容器的统一静态访问，避免在非 Spring 管理的组件中无法依赖注入的问题。
 * 实现 ApplicationContextAware 接口，在 Spring 启动时自动注入 ApplicationContext。
 *
 * @author xiyao
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * Spring 容器启动时自动注入 ApplicationContext
     * <p>
     * 实现 ApplicationContextAware 接口需要重写此方法，Spring 容器启动时会自动调用。
     * 将传入的 ApplicationContext 赋值给静态变量，供后续静态方法使用。
     *
     * @param applicationContext Spring 应用上下文
     * @throws BeansException 如果注入失败
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }

    /**
     * 获取 Spring 容器实例
     * <p>
     * 返回静态存储的 ApplicationContext 实例，如果容器尚未初始化会抛出异常。
     * 注意：此方法仅在 Spring 容器启动完成后才能正常调用。
     *
     * @return ApplicationContext Spring 应用上下文
     * @throws NullPointerException 如果容器尚未初始化
     */
    public static ApplicationContext getApplicationContext() {
        return Objects.requireNonNull(applicationContext, "applicationContext 尚未初始化，请确保 Spring 容器已启动");
    }

    // ==================== 获取 Bean ====================

    /**
     * 根据 Bean 名称获取实例
     * <p>
     * 通过 Bean 名称获取，返回 Object 类型，需要手动进行类型转换。
     * 建议使用 getBean(String, Class) 方法替代，更加类型安全。
     *
     * @param name Bean 名称（通常为首字母小写的类名）
     * @return Bean 实例（Object 类型，需自行转换）
     * @throws BeansException 如果 Bean 不存在
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 根据 Bean 类型获取实例
     * <p>
     * 最常用的获取 Bean 的方式，通过类型自动匹配，无需类型转换。
     * 如果存在多个同类型的 Bean 会抛出异常，此时需要使用 getBean(String, Class) 按名称获取。
     *
     * @param requiredType Bean 的类型 Class
     * @param <T>          Bean 类型
     * @return Bean 实例
     * @throws BeansException 如果 Bean 不存在或类型不唯一
     */
    public static <T> T getBean(Class<T> requiredType) {
        return getApplicationContext().getBean(requiredType);
    }

    /**
     * 根据 Bean 名称和类型获取实例
     * <p>
     * 最安全的获取 Bean 的方式，同时指定名称和类型，避免类型转换错误。
     * 适用于存在多个同类型 Bean 需要按名称区分的情况。
     *
     * @param name         Bean 名称
     * @param requiredType Bean 的类型 Class
     * @param <T>          Bean 类型
     * @return Bean 实例
     * @throws BeansException 如果 Bean 不存在或类型不匹配
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        return getApplicationContext().getBean(name, requiredType);
    }

    /**
     * 判断容器中是否包含指定名称的 Bean
     * <p>
     * 在获取 Bean 之前可以先调用此方法进行判断，避免 Bean 不存在时抛出异常。
     *
     * @param name Bean 名称
     * @return true 表示存在，false 表示不存在
     */
    public static boolean containsBean(String name) {
        return getApplicationContext().containsBean(name);
    }

    // ==================== 获取环境配置 ====================

    /**
     * 获取 Spring 环境配置实例
     * <p>
     * 返回 Environment 对象，可用于获取配置文件中的属性值、Profile 等信息。
     *
     * @return Environment 对象
     */
    public static Environment getEnvironment() {
        return getApplicationContext().getEnvironment();
    }

    /**
     * 获取配置属性值（字符串类型）
     * <p>
     * 从 Spring 配置文件中读取指定 key 的值，支持 application.yml、application.properties、
     * 命令行参数、系统环境变量等多种配置来源，值不存在时返回 null。
     *
     * @param key 配置键，如 "server.port"、"app.name"
     * @return 配置值，不存在返回 null
     */
    public static String getProperty(String key) {
        return getEnvironment().getProperty(key);
    }

    /**
     * 获取配置属性值，带默认值
     * <p>
     * 当配置项不存在时，返回指定的默认值，避免空指针异常。
     *
     * @param key          配置键
     * @param defaultValue 默认值（配置不存在时返回）
     * @return 配置值或默认值
     */
    public static String getProperty(String key, String defaultValue) {
        return getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 获取配置属性值，并转换为指定类型
     * <p>
     * Spring 会自动进行类型转换，支持 Integer、Boolean、Long、Double 等常见类型。
     *
     * @param key        配置键
     * @param targetType 目标类型（如 Integer.class、Boolean.class）
     * @param <T>        目标类型
     * @return 转换后的配置值，不存在返回 null
     */
    public static <T> T getProperty(String key, Class<T> targetType) {
        return getEnvironment().getProperty(key, targetType);
    }

    /**
     * 获取配置属性值，并转换为指定类型，带默认值
     * <p>
     * 结合了类型转换和默认值功能，最灵活的配置获取方式。
     *
     * @param key          配置键
     * @param targetType   目标类型
     * @param defaultValue 默认值
     * @param <T>          目标类型
     * @return 转换后的配置值或默认值
     */
    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return getEnvironment().getProperty(key, targetType, defaultValue);
    }

    /**
     * 判断是否包含某个配置键
     * <p>
     * 可用于判断某个配置项是否存在，避免获取到 null 值。
     *
     * @param key 配置键
     * @return true 表示存在，false 表示不存在
     */
    public static boolean containsProperty(String key) {
        return getEnvironment().containsProperty(key);
    }

    /**
     * 解析占位符
     * <p>
     * 将文本中的 ${...} 占位符替换为实际的配置值。
     * 例如：输入 "应用 ${app.name} 运行在 ${server.port} 端口"
     * 输出："应用 我的应用 运行在 8080 端口"
     *
     * @param text 包含占位符的文本
     * @return 解析后的文本
     */
    public static String resolvePlaceholders(String text) {
        return getEnvironment().resolvePlaceholders(text);
    }

    // ==================== 资源加载 ====================

    /**
     * 获取资源对象
     * <p>
     * 支持多种资源前缀：classpath:（类路径）、file:（文件系统）、http:（网络资源）、
     * 无前缀则根据当前 Spring 上下文解析。
     *
     * @param location 资源路径，如 "classpath:config/application.yml"
     * @return Resource 资源对象，可用于进一步操作
     */
    public static Resource getResource(String location) {
        return getApplicationContext().getResource(location);
    }

    /**
     * 获取资源的输入流
     * <p>
     * 用于读取资源文件的二进制内容，使用完毕后需要手动关闭输入流。
     * 建议使用 try-with-resources 语法自动关闭。
     *
     * @param location 资源路径，如 "classpath:config/logo.png"
     * @return InputStream 输入流
     * @throws IOException 如果资源不存在或无法读取
     */
    public static InputStream getResourceAsStream(String location) throws IOException {
        return getResource(location).getInputStream();
    }

    /**
     * 读取资源文件内容为字符串（UTF-8 编码）
     * <p>
     * 直接读取文本文件内容并返回字符串，内部已处理流关闭，
     * 适用于读取配置文件、SQL 脚本等文本资源。
     *
     * @param location 资源路径，如 "classpath:config/init.sql"
     * @return 文件内容字符串（UTF-8 编码）
     * @throws IOException 如果资源不存在或无法读取
     */
    public static String getResourceContent(String location) throws IOException {
        try (InputStream is = getResourceAsStream(location)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * 判断资源是否存在
     * <p>
     * 在获取资源内容前可以先调用此方法进行判断，避免资源不存在时抛出异常。
     *
     * @param location 资源路径
     * @return true 表示资源存在，false 表示不存在
     */
    public static boolean resourceExists(String location) {
        return getResource(location).exists();
    }

    // ==================== 事件发布 ====================

    /**
     * 发布 Spring 应用事件
     * <p>
     * 发布自定义的 ApplicationEvent 事件，被 @EventListener 标注的方法可以监听到。
     * 适用于应用内模块间的解耦通信。
     * <p>
     * <b>工作原理：</b>
     * <ol>
     *     <li>调用此方法发布事件后，Spring 会获取 ApplicationEventMulticaster（事件广播器）</li>
     *     <li>事件广播器会遍历所有已注册的 ApplicationListener 监听器</li>
     *     <li>判断监听器是否支持该事件类型，若支持则调用监听器的处理逻辑</li>
     *     <li>默认情况下监听器是同步执行的，可通过 @Async 注解改为异步执行</li>
     * </ol>
     * <p>
     * <b>事件类定义示例：</b>
     * <pre>{@code
     * public class UserRegisteredEvent extends ApplicationEvent {
     *     private Long userId;
     *     private String username;
     *
     *     public UserRegisteredEvent(Object source, Long userId, String username) {
     *         super(source);  // source 通常是发布事件的对象（如 this）
     *         this.userId = userId;
     *         this.username = username;
     *     }
     *
     *     // getter 方法...
     * }
     * }</pre>
     * <p>
     * <b>监听器定义示例：</b>
     * <pre>{@code
     * @Component
     * public class UserEventListener {
     *
     *     @EventListener
     *     public void handleUserRegistered(UserRegisteredEvent event) {
     *         // 处理用户注册成功后的业务逻辑
     *         System.out.println("用户 " + event.getUsername() + " 注册成功");
     *     }
     * }
     * }</pre>
     * <p>
     * <b>适用场景：</b>
     * <ul>
     *     <li>用户注册成功 → 发送欢迎邮件、初始化积分、记录日志</li>
     *     <li>订单支付成功 → 扣减库存、更新积分、发送短信通知</li>
     *     <li>数据变更通知 → 同步缓存、更新搜索引擎索引</li>
     *     <li>业务完成后的统计、审计等辅助操作</li>
     * </ul>
     * <p>
     * <b>注意事项：</b>
     * <ul>
     *     <li>默认是同步执行，耗时操作建议配合 @Async 使用</li>
     *     <li>如需事务提交后再执行，请使用 @TransactionalEventListener</li>
     *     <li>事件对象建议定义为不可变对象，避免监听器间相互影响</li>
     * </ul>
     *
     * @param event 事件对象（需继承 ApplicationEvent）
     */
    public static void publishEvent(ApplicationEvent event) {
        getApplicationContext().publishEvent(event);
    }

    /**
     * 发布普通对象作为事件
     * <p>
     * Spring 会自动将普通对象包装为 PayloadApplicationEvent，方便快捷发布事件。
     * 监听器可以使用 @EventListener 直接监听该对象类型。
     * <p>
     * <b>与 publishEvent(ApplicationEvent) 的区别：</b>
     * <ul>
     *     <li>本方法接收任意普通 Java 对象，无需继承 ApplicationEvent</li>
     *     <li>Spring 内部会自动将其包装成 PayloadApplicationEvent 对象</li>
     *     <li>代码更简洁，适合快速开发和简单场景</li>
     *     <li>监听器直接监听业务对象类型，更符合业务语义</li>
     * </ul>
     * <p>
     * <b>使用示例 - 发布事件：</b>
     * <pre>{@code
     * @Service
     * public class OrderService {
     *
     *     public void createOrder(OrderDTO order) {
     *         // 订单创建逻辑...
     *
     *         // 直接发布普通 POJO 对象
     *         SpringUtils.publishEvent(order);
     *     }
     * }
     * }</pre>
     * <p>
     * <b>使用示例 - 监听事件：</b>
     * <pre>{@code
     * @Component
     * public class OrderEventListener {
     *
     *     // 直接监听业务对象类型
     *     @EventListener
     *     public void handleOrderCreated(OrderDTO order) {
     *         System.out.println("订单创建成功: " + order.getOrderId());
     *     }
     * }
     * }</pre>
     * <p>
     * <b>实际案例 - 用户注册：</b>
     * <pre>{@code
     * // 1. 定义事件数据（普通类，无需继承）
     * public class UserRegisterEventData {
     *     private Long userId;
     *     private String username;
     *     private String email;
     *     // 构造方法、getter...
     * }
     *
     * // 2. 发布事件
     * @Service
     * public class UserService {
     *     public void register(String username, String email) {
     *         // 保存用户...
     *         UserRegisterEventData eventData = new UserRegisterEventData(userId, username, email);
     *         SpringUtils.publishEvent(eventData);  // 直接发布 POJO
     *     }
     * }
     *
     * // 3. 监听处理
     * @Component
     * public class UserEventListener {
     *
     *     @EventListener
     *     @Async  // 异步发送邮件
     *     public void sendWelcomeEmail(UserRegisterEventData event) {
     *         emailService.send(event.getEmail(), "欢迎", "您好 " + event.getUsername());
     *     }
     *
     *     @EventListener
     *     @Order(1)
     *     public void initPoints(UserRegisterEventData event) {
     *         pointsService.addPoints(event.getUserId(), 100);
     *     }
     * }
     * }</pre>
     * <p>
     * <b>适用场景：</b>
     * <ul>
     *     <li>快速开发，不想定义事件子类时使用</li>
     *     <li>事件数据就是现有业务对象（如 DTO、VO、Entity）</li>
     *     <li>多个监听器需要处理同一份数据的不同方面</li>
     *     <li>第三方类无法继承 ApplicationEvent 时使用</li>
     * </ul>
     * <p>
     * <b>注意事项：</b>
     * <ul>
     *     <li>多个监听器处理同一个对象时，需要注意对象是否会被修改（建议使用不可变对象）</li>
     *     <li>如果需要携带事件类型信息（如 source 来源），还是建议继承 ApplicationEvent</li>
     *     <li>可以通过泛型限制监听的事件类型：@EventListener 直接写业务类型即可</li>
     *     <li>与 ApplicationEvent 方式可以混用，Spring 会自动处理</li>
     * </ul>
     *
     * @param event 事件数据（任意 Java 对象，如 POJO、DTO、String、数字等）
     */
    public static void publishEvent(Object event) {
        getApplicationContext().publishEvent(event);
    }

    // ==================== 应用信息 ====================

    /**
     * 获取应用名称
     * <p>
     * 从配置文件中读取 spring.application.name 的值，
     * 通常用于日志输出、服务间调用标识等场景。
     *
     * @return 应用名称，未配置返回 null
     */
    public static String getApplicationName() {
        return getProperty("spring.application.name", String.class);
    }

    /**
     * 获取服务器端口
     * <p>
     * 从配置文件中读取 server.port 的值，默认为 8080。
     * 常用于动态获取当前服务运行的端口号。
     *
     * @return 服务器端口，默认 8080
     */
    public static Integer getServerPort() {
        return getProperty("server.port", Integer.class);
    }

    /**
     * 获取应用上下文路径
     * <p>
     * 从配置文件中读取 server.servlet.context-path 的值，
     * 用于构建完整的请求 URL。
     *
     * @return 上下文路径（如 "/api"），未配置返回空字符串
     */
    public static String getContextPath() {
        return getProperty("server.servlet.context-path", String.class);
    }

    /**
     * 自动装配（对非 Spring 管理的对象进行依赖注入）
     * <p>
     * 对于通过 new 关键字手动创建的对象，Spring 不会自动注入依赖。
     * 调用此方法可以手动对该对象进行依赖注入，@Autowired 等注解会生效。
     * 适用于工具类、动态创建的对象等场景。
     *
     * @param bean 需要注入的对象实例
     */
    public static void autowireBean(Object bean) {
        getApplicationContext().getAutowireCapableBeanFactory().autowireBean(bean);
    }
}