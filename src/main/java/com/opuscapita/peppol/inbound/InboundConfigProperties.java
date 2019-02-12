package com.opuscapita.peppol.inbound;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
public class InboundConfigProperties {

    @Value("${test.config.key:test}")
    private String prop;


    public String getProp() {
        return prop;
    }
}
