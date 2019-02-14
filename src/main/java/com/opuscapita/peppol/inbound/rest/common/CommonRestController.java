package com.opuscapita.peppol.inbound.rest.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CommonRestController {

    @RequestMapping("/api/health/check")
    public CommonRestResponse health() {
        return new CommonRestResponse("Yes, I'm alive!");
    }

    @RequestMapping("/api/list/apis")
    public List<ApiListRestResponse> list() {
        List<ApiListRestResponse> response = new ArrayList<>();
        response.add(new ApiListRestResponse("/public/as2", "/public/as2"));
        response.add(new ApiListRestResponse("/public/status", "/public/status"));
        return response;
    }
}
