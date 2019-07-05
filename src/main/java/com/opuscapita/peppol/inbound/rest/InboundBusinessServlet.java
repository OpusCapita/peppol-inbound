package com.opuscapita.peppol.inbound.rest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.opuscapita.peppol.inbound.business.BusinessInboundHandler;

import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Singleton
public class InboundBusinessServlet extends HttpServlet {

    private Provider<BusinessInboundHandler> businessHandlerProvider;

    @Inject
    public InboundBusinessServlet(Provider<BusinessInboundHandler> businessHandlerProvider) {
        this.businessHandlerProvider = businessHandlerProvider;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.println("Business Platform to OpusCapita PEPPOL Access Point endpoint");
        writer.println("Please use `POST` to send the file");
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        try {
            businessHandlerProvider.get().receive(request);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("File uploaded successfully.");

        } catch (Exception e) {
            e.printStackTrace(); // proper logging..

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("The exception is: " + e.getMessage());
        }
    }

}
