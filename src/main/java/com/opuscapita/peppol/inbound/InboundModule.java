package com.opuscapita.peppol.inbound;

import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.opuscapita.peppol.inbound.business.BusinessInboundPersister;
import com.opuscapita.peppol.inbound.network.InboundHandler;
import no.difi.oxalis.api.inbound.InboundService;
import no.difi.oxalis.api.lang.OxalisLoadingException;
import no.difi.oxalis.api.persist.PayloadPersister;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.persist.ReceiptPersister;
import no.difi.oxalis.as2.inbound.OcInboundService;
import no.difi.oxalis.commons.guice.OxalisModule;
import no.difi.oxalis.as4.config.TrustStore;
import no.difi.oxalis.as4.inbound.*;
import no.difi.oxalis.commons.settings.SettingsBuilder;
import org.apache.cxf.wsdl.interceptors.AbstractEndpointSelectionInterceptor;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class InboundModule extends OxalisModule implements Module {

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
        SettingsBuilder.with(this.binder(), TrustStore.class);
    }

    @Provides
    @Singleton
    @Named("truststore-ap")
    protected KeyStore getTruststoreAp() {
        try (InputStream inputStream = getClass().getResourceAsStream("peppol-truststore.jks")) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inputStream, "peppol".toCharArray());
            return keyStore;
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new OxalisLoadingException("Unable to load truststore for AP.", e);
        }
    }

}
