package com.xiyao.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件加解密属性
 */
@Data
@ConfigurationProperties(prefix = "security-data")
public class SecurityData {

    /**
     * 是否开启加密
     */
    private Boolean enabled = false;

    /**
     * 放行路径
     */
    private List<String> includePaths = new ArrayList<>();

    /**
     * 静态路径
     */
    private List<String> staticPaths = new ArrayList<>();

}
