package com.opuscapita.peppol.inbound;

import com.opuscapita.peppol.inbound.module.MessageHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.opuscapita.peppol",
        "com.opuscapita.commons",
        "no.difi.oxalis.as2.inbound"})
public class InboundApp {

    private static MessageHandler mh;

    @Autowired
    public InboundApp(@NotNull MessageHandler messageHandler) {
        mh = messageHandler;
    }

    public static void main(String[] args) {
        SpringApplication.run(InboundApp.class, args);
    }

    @NotNull
    public static MessageHandler getMessageHandler() {
        return mh;
    }
}