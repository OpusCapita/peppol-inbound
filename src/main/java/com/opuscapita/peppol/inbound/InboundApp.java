package com.opuscapita.peppol.inbound;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;

@EnableAutoConfiguration
@SpringBootApplication
@ComponentScan({"com.opuscapita.peppol.inbound", "com.opuscapita.peppol.commons", "no.difi.oxalis.as2.inbound"})
public class InboundApp {

    public static void main(String[] args) {
        try {
            prepareKeystore();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpringApplication.run(InboundApp.class, args);
    }

    // This is the workaround to inject key for compose since we use docker secrets
    private static void prepareKeystore() throws IOException {
        String cert = System.getenv("PEPPOL_KEYSTORE");
        if (StringUtils.isNotBlank(cert)) {
            File file = new File("/run/secrets/oxalis-keystore-07082020.jks");
            FileUtils.writeByteArrayToFile(file, DatatypeConverter.parseBase64Binary(cert));
        }
    }
}