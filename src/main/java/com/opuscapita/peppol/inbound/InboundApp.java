package com.opuscapita.peppol.inbound;

import com.opuscapita.peppol.inbound.module.MessageHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;

@SpringBootApplication(scanBasePackages = {
        "com.opuscapita.peppol.inbound",
        "com.opuscapita.peppol.commons",
        "no.difi.oxalis.as2.inbound"})
public class InboundApp {

    private static MessageHandler mh;

    @Autowired
    public InboundApp(@NotNull MessageHandler messageHandler) {
        mh = messageHandler;
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
        return mh;
    }

    private static void prepareOxalisHomeDirectory() throws IOException {
        String oxalisHome = System.getenv("OXALIS_HOME");
        if (StringUtils.isBlank(oxalisHome)) {
            return;
        }

        System.out.println("OXALIS_HOME: " + oxalisHome);

        String conf = System.getenv("OXALIS_CONF");
        String cert = System.getenv("OXALIS_CERT");

        if (StringUtils.isNotBlank(conf)) {
            System.out.println("OXALIS_CONF: found");
            File file = new File(oxalisHome + "/oxalis.conf");
            FileUtils.writeByteArrayToFile(file, DatatypeConverter.parseBase64Binary(conf));
        }
        if (StringUtils.isNotBlank(cert)) {
            System.out.println("OXALIS_CERT: found");
            File file = new File(oxalisHome + "/oxalis-keystore.jks");
            FileUtils.writeByteArrayToFile(file, DatatypeConverter.parseBase64Binary(cert));
        }
    }
}