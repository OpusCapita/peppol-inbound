package com.opuscapita.peppol.inbound;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class InboundDataSource {

    @Value("jdbc:mysql://mysql:3306/peppol-inbound")
    private String url;

    @Value("${db-init.user:'peppol-inbound'}")
    private String username;

    @Value("${db-init.password:''}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        checkDefaultsForLocalRun();
        return DataSourceBuilder
                .create()
                .username(username)
                .password(password)
                .url(url)
                .driverClassName("com.mysql.jdbc.Driver")
                .build();
    }

    private void checkDefaultsForLocalRun() {
        if (StringUtils.isBlank(password)) {
            password = System.getenv("MYSQL_PASSWORD");
        }
    }
}
