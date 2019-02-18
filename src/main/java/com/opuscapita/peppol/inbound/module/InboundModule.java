package com.opuscapita.peppol.inbound.module;

import com.google.inject.Module;
import com.opuscapita.peppol.commons.statistics.DefaultStatisticsService;
import no.difi.oxalis.api.inbound.InboundService;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.api.statistics.StatisticsService;
import no.difi.oxalis.as2.inbound.OcInboundService;
import no.difi.oxalis.commons.guice.OxalisModule;

public class InboundModule extends OxalisModule implements Module {

    @Override
    protected void configure() {
        bindTyped(PayloadPersister.class, OxalisHandler.class);
        bindTyped(ReceiptPersister.class, OxalisHandler.class);
        bindTyped(PersisterHandler.class, OxalisHandler.class);
        bindTyped(StatisticsService.class, DefaultStatisticsService.class);

        bind(InboundService.class).to(OcInboundService.class);
    }
}