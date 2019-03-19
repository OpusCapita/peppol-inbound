package com.opuscapita.peppol.inbound.module;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.PeppolMessageMetadata;
import com.opuscapita.peppol.commons.container.state.Endpoint;
import com.opuscapita.peppol.commons.container.state.ProcessFlow;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.storage.Storage;
import no.difi.oxalis.api.inbound.InboundMetadata;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Component
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    @Value("${spring.application.name}")
    private String componentName;

    private final Storage storage;
    private final MessageSender messageSender;
    private final TicketReporter ticketReporter;

    @Autowired
    public MessageHandler(@NotNull Storage storage, @NotNull MessageSender messageSender, @NotNull TicketReporter ticketReporter) {
        this.storage = storage;
        this.messageSender = messageSender;
        this.ticketReporter = ticketReporter;
    }

    @NotNull
    String preProcess(String transmissionId, InputStream inputStream) throws IOException {
        return storeTemporary(transmissionId, inputStream);
    }

    // this is the only method that allowed to throw an exception which will be propagated to the sending party
    private String storeTemporary(String transmissionId, InputStream inputStream) throws IOException {
        try {
            String result = storage.putToTemporary(inputStream, transmissionId + ".xml");
            logger.info("Received message stored as " + result);
            return result;
        } catch (Exception e) {
            fail("Failed to store message " + transmissionId + ".xml", transmissionId, e);
            throw new IOException("Failed to store message " + transmissionId + ".xml: " + e.getMessage(), e);
        }
    }

    void process(InboundMetadata inboundMetadata, Path payloadPath) {
        ContainerMessage cm = createContainerMessage(payloadPath.toString());
        cm.setMetadata(PeppolMessageMetadata.create(inboundMetadata));
        messageSender.send(cm);
    }

    private ContainerMessage createContainerMessage(String dataFile) {
        Endpoint endpoint = new Endpoint(componentName, ProcessFlow.IN, ProcessStep.INBOUND);
        ContainerMessage cm = new ContainerMessage(dataFile, endpoint);
        cm.getHistory().addInfo("Received and stored");
        return cm;
    }

    private void fail(String message, String transmissionId, Exception e) {
        logger.error(message, e);
        try {
            ticketReporter.reportWithoutContainerMessage(null, transmissionId + ".xml", e, message);
        } catch (Exception ex) {
            logger.error("Failed to report error '" + message + "'", ex);
        }
    }

}