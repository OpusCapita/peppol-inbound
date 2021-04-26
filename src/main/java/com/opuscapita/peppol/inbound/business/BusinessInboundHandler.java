package com.opuscapita.peppol.inbound.business;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.inbound.rest.ServletRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessInboundHandler {

    private final BusinessInboundPersister persisterHandler;
    private final static Logger logger = LoggerFactory.getLogger(BusinessInboundHandler.class);

    @Inject
    public BusinessInboundHandler(@Named("opuscapita") BusinessInboundPersister persisterHandler) {
        this.persisterHandler = persisterHandler;
    }

    public void receive(final HttpServletRequest request) throws Exception {
        ServletRequestWrapper wrapper = new ServletRequestWrapper(request);


        logger.debug("BussinessInboundHandler.receive entered");

        /* OUTCOMMENT FOR EXTRA DEBUG
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);

            //logger.debug("HTTP HEADER: " + key + " -> ''" + value + "'"); super debug
        }*/

        Source source = getSource(request);
        String filename = request.getParameter("filename");

        logger.info("Received Filename = " + filename +" source=" + source );

        persisterHandler.persist(filename, source, wrapper);
    }

    private Source getSource(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.contains("reprocess")) {
            return Source.valueOf(request.getHeader("Peppol-Source"));
        }
        if (uri.contains("a2a")) {
            return Source.A2A;
        }
        else if (uri.contains("xib")) {
            return Source.XIB;
        }
        else if (uri.contains("sirius")) {
            return Source.SIRIUS;
        }
        else if (uri.contains("gw")) {
            return Source.GW;
        }

        return Source.UNKNOWN;
    }

}
