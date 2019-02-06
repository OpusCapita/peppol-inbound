package com.opuscapita.peppol.inbound.oxalis;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import no.difi.oxalis.commons.util.OxalisVersion;
import no.difi.vefa.peppol.mode.Mode;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import java.util.Date;

@Singleton
public class StatusServlet extends HttpServlet {

    private final X509Certificate certificate;

    private final Config config;

    private final Mode mode;

    @Inject
    public StatusServlet(X509Certificate certificate, Config config, Mode mode) {
        this.certificate = certificate;
        this.mode = mode;
        this.config = config;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");

        PrintWriter writer = resp.getWriter();
        writer.println("version.oxalis: " + OxalisVersion.getVersion());
        writer.println("version.java: " + System.getProperty("java.version"));
        writer.println("mode: " + mode.getIdentifier());

        if (config.hasPath("lookup.locator.hostname")) {
            writer.print("lookup.locator.hostname");
            writer.print(": ");
            writer.println(config.getString("lookup.locator.hostname"));
        }

        writer.println("certificate.subject: " + certificate.getSubjectX500Principal().getName());
        writer.println("certificate.issuer: " + certificate.getIssuerX500Principal().getName());
        writer.println("certificate.expired: " + certificate.getNotAfter().before(new Date()));
        writer.println("build.id: " + OxalisVersion.getBuildId());
        writer.println("build.tstamp: " + OxalisVersion.getBuildTimeStamp());
    }
}
