package com.opuscapita.peppol.inbound.rest;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Constructs a request object wrapping the given request to support multiple reads
 */
public class ServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] body;

    public ServletRequestWrapper(HttpServletRequest request) {
        super(request);
        try {
            body = IOUtils.toByteArray(request.getInputStream());
        } catch (IOException ex) {
            body = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new OcServletInputStream(new ByteArrayInputStream(body));
    }

}
