package com.opuscapita.peppol.inbound;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RestController
@RequestMapping("/public")
public class InboundRestController {

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public void status(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.println("version.oxalis: 4.0.2");
        writer.println("version.java: " + System.getProperty("java.version"));
        writer.println("mode: TEST");

        writer.println("certificate.subject: C=FI,O=OpusCapita Solutions Oy,OU=PEPPOL PRODUCTION AP,CN=PNO000104");
        writer.println("certificate.issuer: CN=PEPPOL ACCESS POINT CA - G2,O=OpenPEPPOL AISBL,C=BE");
        writer.println("certificate.expired: false");
    }

    @RequestMapping(value = "/as2", method = RequestMethod.GET)
    public void as2get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.println("Hello AS2 world");
    }
}
