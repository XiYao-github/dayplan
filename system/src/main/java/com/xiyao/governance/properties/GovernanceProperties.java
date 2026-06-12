package com.xiyao.governance.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "system.governance")
public class GovernanceProperties {

    /**
     * 是否启用功能
     */
    private boolean enable = true;

}