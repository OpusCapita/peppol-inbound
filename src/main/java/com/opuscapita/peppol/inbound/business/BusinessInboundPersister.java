package com.opuscapita.peppol.inbound.business;

import com.opuscapita.peppol.commons.container.state.Source;
import no.difi.vefa.peppol.common.model.Header;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface BusinessInboundPersister {

    void persist(String filename, Source source, Header header, InputStream inputStream) throws IOException;

}
