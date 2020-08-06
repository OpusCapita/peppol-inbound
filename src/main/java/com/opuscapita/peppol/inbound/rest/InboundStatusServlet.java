package com.opuscapita.peppol.inbound.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import no.difi.oxalis.as4.inbound.OxalisAS4Version;
import no.difi.oxalis.commons.util.OxalisVersion;
import no.difi.vefa.peppol.mode.Mode;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;

@Singleton
public class InboundStatusServlet extends HttpServlet {

    private final Mode mode;
    private final X509Certificate certificate;

    @Inject
    public InboundStatusServlet(X509Certificate certificate, Mode mode) {
        this.mode = mode;
        this.certificate = certificate;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");

        PrintWriter writer = resp.getWriter();
        writer.println("version.oxalis: " + OxalisVersion.getVersion());
        writer.println("version.oxalis-as4: " + OxalisAS4Version.getVersion());
        writer.println("version.java: " + System.getProperty("java.version"));
        writer.println("mode: " + mode.getIdentifier());

        writer.println("certificate.subject: " + certificate.getSubjectX500Principal().getName());
        writer.println("certificate.issuer: " + certificate.getIssuerX500Principal().getName());
        writer.println("certificate.expired: " + certificate.getNotAfter().before(new Date()));
        writer.println("certificate.expiryDate: " + new SimpleDateFormat("dd-MMM-yyyy").format(certificate.getNotAfter()));
        writer.println("build.id: " + OxalisVersion.getBuildId());
        writer.println("build.tstamp: " + OxalisVersion.getBuildTimeStamp());
    }

}
