package com.opuscapita.peppol.inbound.business;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.inbound.rest.ServletRequestWrapper;
import no.difi.oxalis.api.header.HeaderParser;
import no.difi.vefa.peppol.common.model.Header;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

public class BusinessInboundHandler {

    private final HeaderParser headerParser;
    private final BusinessInboundPersister persisterHandler;

    @Inject
    public BusinessInboundHandler(HeaderParser headerParser, @Named("opuscapita") BusinessInboundPersister persisterHandler) {
        this.headerParser = headerParser;
        this.persisterHandler = persisterHandler;
    }

    public void receive(final HttpServletRequest request) throws Exception {
        ServletRequestWrapper wrapper = new ServletRequestWrapper(request);

        Source source = getSource(request);
        String filename = request.getParameter("filename");

        Header header;
        try (InputStream inputStream = wrapper.getInputStream()) {
            header = headerParser.parse(inputStream);
        }

        if (header == null) {
            // TODO: return something to sender and stop processing
        }

        try (InputStream inputStream = wrapper.getInputStream()) {
            persisterHandler.persist(filename, source, header, inputStream);
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
