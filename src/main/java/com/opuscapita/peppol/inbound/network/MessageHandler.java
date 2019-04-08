package com.opuscapita.peppol.inbound.network;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.metadata.MetadataExtractor;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final Storage storage;
    private final MessageSender messageSender;
    private final TicketReporter ticketReporter;
    private final MetadataExtractor metadataExtractor;

    @Autowired
    public MessageHandler(Storage storage, MessageSender messageSender, TicketReporter ticketReporter, MetadataExtractor metadataExtractor) {
        this.storage = storage;
        this.messageSender = messageSender;
        this.ticketReporter = ticketReporter;
        this.metadataExtractor = metadataExtractor;
    }

    void process(ContainerMessageMetadata metadata, Source source, String filename) {
        ContainerMessage cm = new ContainerMessage(filename, source, ProcessStep.INBOUND);
        cm.setMetadata(metadata);
        cm.getHistory().addInfo("Received file from " + source);

        messageSender.send(cm);
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

    ContainerMessageMetadata extractMetadata(InputStream content) {
        return metadataExtractor.extract(content);
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