package com.aantriksanket.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class AppProperties {

    private boolean allowDomainOverride;

    public boolean isAllowDomainOverride() {
        return allowDomainOverride;
    }

    public void setAllowDomainOverride(boolean allowDomainOverride) {
        this.allowDomainOverride = allowDomainOverride;
    }
}
