package com.opuscapita.peppol.inbound;

import com.opuscapita.peppol.inbound.module.MessageHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
            String dirName = "/run/secrets/";
            Files.list(new File(dirName).toPath())
                    .limit(10)
                    .forEach(path -> {
                        System.out.println(path);
                    });
        } catch (IOException e) {
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
}