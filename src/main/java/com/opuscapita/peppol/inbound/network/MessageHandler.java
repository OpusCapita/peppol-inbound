package com.opuscapita.peppol.inbound.network;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.OcTransmissionResult;
import com.opuscapita.peppol.commons.container.metadata.PeppolMessageMetadata;
import com.opuscapita.peppol.commons.container.state.Endpoint;
import com.opuscapita.peppol.commons.container.state.ProcessFlow;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.storage.Storage;
import no.difi.oxalis.api.inbound.InboundMetadata;
import no.difi.vefa.peppol.common.model.Header;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Component
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final Storage storage;
    private final MessageSender messageSender;
    private final TicketReporter ticketReporter;

    @Autowired
    public MessageHandler(@NotNull Storage storage, @NotNull MessageSender messageSender, @NotNull TicketReporter ticketReporter) {
        this.storage = storage;
        this.messageSender = messageSender;
        this.ticketReporter = ticketReporter;
    }

    void process(InboundMetadata inboundMetadata, Path payloadPath, Source source) {
        ContainerMessage cm = createContainerMessage(payloadPath.toString(), source);
        cm.setMetadata(PeppolMessageMetadata.create(inboundMetadata));
        messageSender.send(cm);
    }

    void process(Header header, String filePath, Source source) {
        ContainerMessage cm = createContainerMessage(filePath, source);
        cm.setMetadata(PeppolMessageMetadata.create(new OcTransmissionResult(header)));
        messageSender.send(cm);
    }

    private ContainerMessage createContainerMessage(String dataFile, Source source) {
        Endpoint endpoint = new Endpoint(source, ProcessFlow.IN, ProcessStep.INBOUND);
        ContainerMessage cm = new ContainerMessage(dataFile, endpoint);
        cm.getHistory().addInfo("Received and stored");
        return cm;
    }

    // this is the only method that allowed to throw an exception which will be propagated to the sending party
    String store(String filename, InputStream inputStream) throws IOException {
        try {
            return storage.putToTemporary(inputStream, filename);
        } catch (Exception e) {
            fail("Failed to store message " + filename, filename, e);
            throw new IOException("Failed to store message " + filename + ", reason: " + e.getMessage(), e);
        }
    }

    private void fail(String message, String filename, Exception e) {
        logger.error(message, e);
        try {
            ticketReporter.reportWithoutContainerMessage(null, filename, e, message);
        } catch (Exception ex) {
            logger.error("Failed to report error '" + message + "'", ex);
        }
    }

}