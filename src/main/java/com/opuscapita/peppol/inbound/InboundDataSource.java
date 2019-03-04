package com.opuscapita.peppol.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class InboundDataSource {

    private static final Logger logger = LoggerFactory.getLogger(InboundDataSource.class);

    @Value("${db-init.user:root}")
    private String user;

    @Value("${db-init.password:test}")
    private String password;

    @Value("${db-init.database:peppol-inbound}")
    private String database;

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder
                .create()
                .username(user)
                .password(password)
                .url("jdbc:mysql://mysql:3306/" + database)
                .driverClassName("com.mysql.jdbc.Driver")
                .build();
    }
}
