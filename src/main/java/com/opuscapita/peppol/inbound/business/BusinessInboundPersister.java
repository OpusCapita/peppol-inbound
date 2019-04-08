package com.opuscapita.peppol.inbound.business;

import com.opuscapita.peppol.commons.container.state.Source;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface BusinessInboundPersister {

    void persist(String filename, Source source, InputStream inputStream) throws IOException;

}
