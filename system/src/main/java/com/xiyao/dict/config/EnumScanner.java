package com.xiyao.dict.config;

import com.xiyao.dict.enums.BaseEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 枚举类扫描器
 * <p>
 * 项目启动时扫描所有继承 BaseEnum 的枚举类并加载到 DictCache 缓存。
 * 实现应用的零配置自动化，枚举类定义后自动支持字典转换功能。
 *
 * <p>
 * <b>扫描机制：</b>
 * <ol>
 *     <li>扫描 classpath 下路径的所有类</li>
 *     <li>检查类是否继承 BaseEnum 且是枚举类型</li>
 *     <li>将符合条件的枚举类加载到 DictCache 缓存</li>
 * </ol>
 *
 * <p>
 * <b>使用要求：</b>
 * <ul>
 *     <li>枚举类必须实现 BaseEnum 接口</li>
 *     <li>枚举类必须在 com.xiyao 包或其子包下</li>
 *     <li>枚举类必须是具体枚举类型，不能是抽象枚举</li>
 * </ul>
 *
 * @author xiyao
 * @see BaseEnum
 * @see DictCache
 */
@Configuration
@Slf4j
public class EnumScanner {

    /**
     * Spring 应用上下文
     * <p>
     * 用于获取类加载器和支持 SpEL 表达式等高级功能
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 项目启动时执行扫描和加载
     * <p>
     * 扫描 classpath 下所有继承 BaseEnum 的枚举类，加载到 DictCache。
     * 扫描失败或单个枚举加载失败不影响应用启动，仅记录警告日志。
     */
    @PostConstruct
    public void scanAndLoadEnums() {
        try {
            // 扫描所有 BaseEnum 子类
            Set<Class<? extends BaseEnum<?>>> enumClasses = scanBaseEnumClasses();
            log.info("枚举类扫描完成: found {} enum classes", enumClasses.size());

            // 逐个加载到缓存
            for (Class<? extends BaseEnum<?>> enumClass : enumClasses) {
                try {
                    DictCache.getInstance().loadEnum(enumClass);
                } catch (Exception e) {
                    log.warn("枚举类加载失败: class={}, error={}", enumClass.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("枚举类扫描失败: error={}", e.getMessage());
        }
    }

    /**
     * 扫描项目中所有继承 BaseEnum 的枚举类
     * <p>
     * 使用 Spring 的元数据读取器扫描 classpath 下的类文件，
     * 避免类加载以提高性能和避免触发静态块。
     *
     * @return 找到的 BaseEnum 子类集合
     * @throws IOException 如果扫描过程发生 IO 异常
     */
    @SuppressWarnings("unchecked")
    private Set<Class<? extends BaseEnum<?>>> scanBaseEnumClasses() throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory();

        Set<Class<? extends BaseEnum<?>>> result = new HashSet<>();

        // 扫描路径：com.xiyao 包及其子包下的所有类
        String[] scanPaths = {
                "classpath*:com/xiyao/**/*.class"
        };

        for (String scanPath : scanPaths) {
            try {
                org.springframework.core.io.Resource[] resources = resolver.getResources(scanPath);
                for (org.springframework.core.io.Resource resource : resources) {
                    try {
                        MetadataReader reader = readerFactory.getMetadataReader(resource);
                        String className = reader.getClassMetadata().getClassName();

                        // 跳过接口和抽象类
                        if (reader.getClassMetadata().isInterface() || reader.getClassMetadata().isAbstract()) {
                            continue;
                        }

                        // 加载类并检查是否继承 BaseEnum 且是枚举
                        Class<?> clazz = ClassUtils.forName(className, applicationContext.getClassLoader());
                        if (BaseEnum.class.isAssignableFrom(clazz) && clazz.isEnum()) {
                            result.add((Class<? extends BaseEnum<?>>) clazz);
                        }
                    } catch (Exception ignored) {
                        // 单个类处理失败不影响其他类
                    }
                }
            } catch (Exception ignored) {
                // 单个路径扫描失败不影响其他路径
            }
        }

        return result;
    }
}