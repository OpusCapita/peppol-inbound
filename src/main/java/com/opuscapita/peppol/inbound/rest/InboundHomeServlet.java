package com.opuscapita.peppol.inbound.rest;

import com.google.inject.Singleton;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class InboundHomeServlet extends HttpServlet {

    @Override
    // not nice, but it won't be like that for other services, inbound has to use servlets
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        if (uri.contains("health/check")) {
            getHealth(resp);
        } else {
            getHome(resp);
        }
    }

    private void getHealth(HttpServletResponse resp) throws IOException {
        resp.getWriter().print("{\"message\": \"Yes, I'm alive!\"}");
    }

    private void getHome(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().println("<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "        <title>OpusCapita PEPPOL AP</title>\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>OpusCapita PEPPOL Access Point</h1>\n" +
                "        <p>The protocols for this Access Point are :</p>\n" +
                "        <ul>\n" +
                "            <li><strong>AS2</strong>: Endpoint can be found <a href=\"public/as2\">here</a>.</li>\n" +
                "        </ul>\n" +
                "        <p>Some status information can be found at <a href=\"public/status\">status</a>.</p>\n" +
                "    </body>\n" +
                "</html>\n");
    }

}
