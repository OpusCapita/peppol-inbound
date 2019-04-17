package com.opuscapita.peppol.inbound.network;

import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.inbound.InboundApp;
import com.opuscapita.peppol.inbound.business.BusinessInboundPersister;
import com.opuscapita.peppol.inbound.rest.ServletRequestWrapper;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.oxalis.api.model.TransmissionIdentifier;
import no.difi.oxalis.api.persist.PersisterHandler;
import no.difi.oxalis.api.util.Type;
import no.difi.vefa.peppol.common.model.Header;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Receives message from Oxalis and gives it to us.
 * Exception in persist method will be propagated to the other party
 * So please be careful on updates to this methods.
 */
@Singleton
@Type("opuscapita")
public class InboundHandler implements PersisterHandler, BusinessInboundPersister {

    private final static Logger logger = LoggerFactory.getLogger(InboundHandler.class);

    private final MessageHandler messageHandler;

    // this is done to separate Spring dependency injection from Guice one (we're in Guice now, while messageHandler is in Spring)
    public InboundHandler() {
        messageHandler = InboundApp.getMessageHandler();
        logger.info("OpusCapita inbound receiver initialized");
    }

    // file coming from network: oxalis payload persister
    @Override
    public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream inputStream) throws IOException {
        String filename = header.getIdentifier().getIdentifier() + ".xml";
        logger.info("Received a message from NETWORK, storing content as: " + filename);
        return Paths.get(messageHandler.store(filename, inputStream));
    }

    // file coming from network: oxalis receipt persister
    @Override
    public void persist(InboundMetadata inboundMetadata, Path payloadPath) {
        Source source = Source.NETWORK;
        String filename = payloadPath.toString();
        ContainerMessageMetadata metadata = ContainerMessageMetadata.create(inboundMetadata);

        logReceipt(metadata, source, filename);
        messageHandler.process(metadata, source, filename);
    }

    // file coming from business platform: source, both payload and receipt persistence
    @Override
    public void persist(String filename, Source source, ServletRequestWrapper wrapper) throws IOException {
        ContainerMessageMetadata metadata = messageHandler.extractMetadata(wrapper);
        filename = StringUtils.isBlank(filename) ? metadata.getMessageId() + ".xml" : filename;

        logger.info("Received a message from " + source.name() + ", storing content as: " + filename);
        String dataFile = messageHandler.store(filename, wrapper.getInputStream());

        logReceipt(metadata, source, dataFile);
        messageHandler.process(metadata, source, dataFile);
    }

    private void logReceipt(ContainerMessageMetadata metadata, Source source, String filename) {
        logger.info("TransmissionReceipt {filename=" + filename +
                ", source=" + source.name() +
                ", sender=" + metadata.getSenderId() +
                ", receiver=" + metadata.getRecipientId() +
                ", profile=" + metadata.getProfileTypeIdentifier() +
                ", documentType=" + metadata.getDocumentTypeIdentifier() +
                ", messageId=" + metadata.getMessageId() +
                ", transmissionId=" + metadata.getTransmissionId() +
                ", instanceType=" + metadata.getInstanceType() +
                ", timestamp=" + metadata.getTimestamp() +
                "}");
    }

}
