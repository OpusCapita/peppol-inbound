package com.opuscapita.peppol.inbound.rest;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import com.opuscapita.peppol.commons.template.ApiListRestResponse;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class InboundHomeServlet extends HttpServlet {

    @Override
    // not nice, but it won't be like that for other services, inbound has to use servlets
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        if (uri.contains("/api/health/check")) {
            getHealth(resp);
        } else if (uri.contains("/api/list/apis")) {
            getList(resp);
        } else {
            getHome(resp);
        }
    }

    private void getHealth(HttpServletResponse resp) throws IOException {
        resp.getWriter().print("{\"message\": \"Yes, I'm alive!\"}");
    }

    private void getList(HttpServletResponse resp) throws IOException {
        List<ApiListRestResponse> apiList = new ArrayList<>();
        apiList.add(new ApiListRestResponse("/public/a2a", "/public/a2a"));
        apiList.add(new ApiListRestResponse("/public/xib", "/public/xib"));
        apiList.add(new ApiListRestResponse("/public/as2", "/public/as2"));
        apiList.add(new ApiListRestResponse("/public/status", "/public/status"));
        resp.getWriter().print(new Gson().toJson(apiList));
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
