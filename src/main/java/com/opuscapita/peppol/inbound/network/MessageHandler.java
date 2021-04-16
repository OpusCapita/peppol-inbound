package com.opuscapita.peppol.inbound.network;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.metadata.MetadataExtractor;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.commons.storage.StorageUtils;
import com.opuscapita.peppol.inbound.rest.ServletRequestWrapper;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Component
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    @Value("${peppol.storage.blob.hot:hot}")
    private String hotFolder;

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
    String store(String filename, Source source, InputStream inputStream) throws IOException {
        try {
            logger.info("TODO: MesssageHandler.store invoked for filename: " + filename); //should be debug
            String path = hotFolder + StorageUtils.FILE_SEPARATOR + source.name().toLowerCase();
            path = StorageUtils.createDailyPath(path, "");
            logger.info("TODO: MesssageHandler.store invoked for path: " + path);

            String theString = convertInputStreamToString(inputStream);
            logger.info("TODO: body: " + theString);

            InputStream targetStream = new ByteArrayInputStream(theString.getBytes());

            String result = storage.put(inputStream, path, filename);
            logger.info("TODO: result: " + result);

            return  result;
        } catch (Exception e) {
            logger.error("Failed to store message " + filename);
            logger.error( e );

            fail("Failed to store message " + filename, filename, e);
            throw new IOException("Failed to store message " + filename + ", reason: " + e.getMessage(), e);
        }
    }

    // Plain Java
    private String convertInputStreamToString(InputStream is) throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        // Java 1.1
        return result.toString(StandardCharsets.UTF_8.name());

        // Java 10
        // return result.toString(StandardCharsets.UTF_8);

    }

    ContainerMessageMetadata extractMetadata(ServletRequestWrapper wrapper) throws IOException {
        ContainerMessageMetadata metadata = metadataExtractor.extract(wrapper.getInputStream());

        if (metadata == null) {
            metadata = metadataExtractor.extractFromPayload(wrapper.getInputStream());
        }

        if (metadata == null) {
            metadata = new ContainerMessageMetadata();
            metadata.setTransmissionId(UUID.randomUUID().toString());
            metadata.setMessageId(UUID.randomUUID().toString());
        }

        // PD-152: persist access point info through reprocessing
        String accessPointId = wrapper.getHeader("Access-Point");
        if (StringUtils.isNotBlank(accessPointId)) {
            metadata.setSendingAccessPoint(accessPointId);
        }

        metadata.setBusinessMetadata(metadataExtractor.extractBusinessMetadata(wrapper.getInputStream()));
        return metadata;
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
