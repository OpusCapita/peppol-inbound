package com.opuscapita.peppol.inbound;

import com.opuscapita.peppol.inbound.network.MessageHandler;
import com.opuscapita.peppol.inbound.util.FileUpdateUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan({"com.opuscapita.peppol.inbound", "com.opuscapita.peppol.commons", "no.difi.oxalis.as2.inbound"})
public class InboundApp {

    private static MessageHandler messageHandler;

    @Autowired
    public InboundApp(@NotNull MessageHandler commonMessageHandler) {
        messageHandler = commonMessageHandler;
    }

    public static void main(String[] args) {
        try {
            prepareOxalisHomeDirectory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpringApplication.run(InboundApp.class, args);
    }

    /**
     * A bit tricky thing, Oxalis uses Guice dependency injection while our code uses Spring.
     * This is the way how to inform Oxalis on what class to use.
     *
     * @return message handler bean managed by Spring
     */
    @NotNull
    public static MessageHandler getMessageHandler() {
        return messageHandler;
    }

    /**
     * A bit tricky thing, Local build and testing uses docker compose
     * This is the workaround to inject keys since compose secrets failed.
     */
    private static void prepareOxalisHomeDirectory() throws IOException {
        String oxalisHome = System.getenv("OXALIS_HOME");
        if (StringUtils.isBlank(oxalisHome)) {
            return;
        }

        String conf = System.getenv("OXALIS_CONF");
        String cert = System.getenv("OXALIS_CERT");

        if (StringUtils.isNotBlank(conf)) {
            String key = "oxalis.database.jdbc.password";

            File file = new File(oxalisHome + "/oxalis.conf");
            InputStream content = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(conf));
            InputStream updated = FileUpdateUtils.startAndReplace(content, key, key + "=test");
            FileUtils.copyInputStreamToFile(updated, file);
        }
        if (StringUtils.isNotBlank(cert)) {
            File file = new File(oxalisHome + "/oxalis-keystore.jks");
            FileUtils.writeByteArrayToFile(file, DatatypeConverter.parseBase64Binary(cert));
        }
    }

}