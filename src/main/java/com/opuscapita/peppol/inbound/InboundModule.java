package com.opuscapita.peppol.inbound;

import com.google.inject.Module;
import com.opuscapita.peppol.inbound.business.BusinessInboundPersister;
import com.opuscapita.peppol.inbound.network.InboundHandler;
import no.difi.oxalis.api.inbound.InboundService;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.as2.inbound.OcInboundService;
import no.difi.oxalis.commons.guice.OxalisModule;

public class InboundModule extends OxalisModule implements Module {

    @Override
    protected void configure() {
        bindTyped(PayloadPersister.class, InboundHandler.class);
        bindTyped(ReceiptPersister.class, InboundHandler.class);
        bindTyped(PersisterHandler.class, InboundHandler.class);
        bindTyped(BusinessInboundPersister.class, InboundHandler.class);

        bind(InboundService.class).to(OcInboundService.class);
    }

}
