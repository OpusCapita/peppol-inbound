package com.opuscapita.peppol.inbound;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CommonRestController {

    @RequestMapping("/")
    public String home() {
        return "<!DOCTYPE html>\n" +
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
                "</html>\n";
    }

    @RequestMapping("/api/health/check")
    public CommonRestResponse health() {
        return new CommonRestResponse("Yes, I'm alive!");
    }

    @RequestMapping("/api/list/apis")
    public ApiListRestResponse[] list() {
        List<ApiListRestResponse> response = new ArrayList<>();
        response.add(new ApiListRestResponse("/public/as2", "/public/as2"));
        response.add(new ApiListRestResponse("/public/status", "/public/status"));
        return (ApiListRestResponse[]) response.toArray();
    }
}
