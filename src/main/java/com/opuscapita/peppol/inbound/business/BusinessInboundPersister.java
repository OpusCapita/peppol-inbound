package com.opuscapita.peppol.inbound.business;

import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.inbound.rest.ServletRequestWrapper;

import java.io.IOException;

@FunctionalInterface
public interface BusinessInboundPersister {

    void persist(String filename, Source source, ServletRequestWrapper wrapper) throws IOException;

}
