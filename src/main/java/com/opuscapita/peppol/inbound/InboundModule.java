package com.opuscapita.peppol.inbound;

import com.google.inject.Module;
import com.opuscapita.peppol.inbound.business.BusinessInboundPersister;
import com.opuscapita.peppol.inbound.network.InboundHandler;
import com.opuscapita.peppol.inbound.network.MessageHandler;
import no.difi.oxalis.api.inbound.InboundService;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.as2.inbound.OcInboundService;
import no.difi.oxalis.as4.inbound.*;
import no.difi.oxalis.commons.guice.OxalisModule;
import org.apache.cxf.wsdl.interceptors.AbstractEndpointSelectionInterceptor;

public class InboundModule extends OxalisModule implements Module {

    private static MessageHandler messageHandler;

    public InboundModule(MessageHandler springInjectedMessageHandler) {
        InboundModule.messageHandler = springInjectedMessageHandler;
    }

    @Override
    protected void configure() {
        bindTyped(PayloadPersister.class, InboundHandler.class);
        bindTyped(ReceiptPersister.class, InboundHandler.class);
        bindTyped(PersisterHandler.class, InboundHandler.class);
        bindTyped(BusinessInboundPersister.class, InboundHandler.class);

        bind(InboundService.class).to(OcInboundService.class);

        bind(AbstractEndpointSelectionInterceptor.class).to(As4EndpointSelector.class);
        bind(As4Provider.class);
        bind(As4EndpointsPublisher.class).to(As4EndpointsPublisherImpl.class);
        bind(As4InboundHandler.class);
    }

    public static MessageHandler getMessageHandler() {
        return InboundModule.messageHandler;
    }
}
