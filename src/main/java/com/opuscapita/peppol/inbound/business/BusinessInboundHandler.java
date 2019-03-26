package com.opuscapita.peppol.inbound.business;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.opuscapita.peppol.commons.container.state.Source;
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
        InputStream inputStream = request.getInputStream();
        Header header = headerParser.parse(inputStream);
        String filename = request.getParameter("filename");
        Source source = getSource(request);

        persisterHandler.persist(filename, source, header, inputStream);
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
