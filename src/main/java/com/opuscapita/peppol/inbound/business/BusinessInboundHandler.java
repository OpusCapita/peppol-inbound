package com.opuscapita.peppol.inbound.business;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.inbound.rest.ServletRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

public class BusinessInboundHandler {

    private final BusinessInboundPersister persisterHandler;

    @Inject
    public BusinessInboundHandler(@Named("opuscapita") BusinessInboundPersister persisterHandler) {
        this.persisterHandler = persisterHandler;
    }

    public void receive(final HttpServletRequest request) throws Exception {
        ServletRequestWrapper wrapper = new ServletRequestWrapper(request);

        Source source = getSource(request);
        String filename = request.getParameter("filename");

        try (InputStream inputStream = wrapper.getInputStream()) {
            persisterHandler.persist(filename, source, inputStream);
        }
    }

    private Source getSource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("a2a")) {
            return Source.A2A;
        }
        if (uri.contains("xib")) {
            return Source.XIB;
        }
        return Source.UNKNOWN;
    }

}
