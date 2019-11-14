package com.opuscapita.peppol.inbound.network;

import com.opuscapita.peppol.commons.auth.AuthorizationService;
import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.metadata.MetadataExtractor;
import com.opuscapita.peppol.commons.container.state.ProcessStep;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.storage.Storage;
import com.opuscapita.peppol.commons.storage.StorageException;
import com.opuscapita.peppol.commons.storage.StorageUtils;
import com.opuscapita.peppol.commons.storage.blob.BlobServiceResponse;
import com.opuscapita.peppol.inbound.rest.ServletRequestWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

@Component
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    @Value("${peppol.storage.blob.hot:hot}")
    private String hotFolder;

    @Value("${peppol.storage.blob.url}")
    private String host;
    @Value("${peppol.storage.blob.port}")
    private String port;

    @Value("${peppol.auth.tenant.id}")
    private String tenant;

    private final Storage storage;
    private final MessageSender messageSender;
    private final TicketReporter ticketReporter;
    private final MetadataExtractor metadataExtractor;

    private RestTemplate restTemplate;
    private AuthorizationService authService;

    @Autowired
    public MessageHandler(Storage storage, MessageSender messageSender, TicketReporter ticketReporter, MetadataExtractor metadataExtractor,
                          AuthorizationService authService, RestTemplateBuilder restTemplateBuilder) {
        this.storage = storage;
        this.messageSender = messageSender;
        this.ticketReporter = ticketReporter;
        this.metadataExtractor = metadataExtractor;

        this.authService = authService;
        this.restTemplate = restTemplateBuilder.build();
        this.restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
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
            logger.debug("MesssageHandler.store invoked for filename: " + filename);
            String path = hotFolder + StorageUtils.FILE_SEPARATOR + source.name().toLowerCase();
            path = StorageUtils.createDailyPath(path, "");
            return localPut(inputStream, path, filename);
        } catch (Exception e) {
            fail("Failed to store message " + filename, filename, e);
            throw new IOException("Failed to store message " + filename + ", reason: " + e.getMessage(), e);
        }
    }

    private String localPut(InputStream content, String folder, String filename) throws StorageException {
        String path = folder + filename;

        logger.debug("File storage requested from blob service to path: " + path);
        try {
            String endpoint = getEndpoint(path, true);
            logger.debug("Putting file to endpoint: " + endpoint);

            HttpHeaders headers = new HttpHeaders();
            authService.setAuthorizationHeader(headers);
            headers.set("Transfer-Encoding", "chunked");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            HttpEntity<Resource> entity = new HttpEntity<>(new InputStreamResource(content), headers);
            logger.debug("Wrapped and set the request body as input stream");

            ResponseEntity<BlobServiceResponse> result = restTemplate.exchange(endpoint, HttpMethod.PUT, entity, BlobServiceResponse.class);
            logger.debug("File stored successfully to blob service path: " + path);
            return result.getBody().getPath();
        } catch (Exception e) {
            throw new StorageException("Error occurred while trying to put the file to blob service", e);
        }
    }

    private String getEndpoint(String path, boolean createMissing) throws StorageException {
        if (StringUtils.isBlank(tenant)) {
            throw new StorageException("Blob service cannot be used: Missing configuration \"peppol.auth.tenant.id\".");
        }

        return UriComponentsBuilder
                .fromUriString("http://" + host)
                .port(port)
                .path("/api/" + tenant + "/files" + path)
                .queryParam("inline", "true")
                .queryParam("createMissing", String.valueOf(createMissing))
                .queryParam("recursive", "true")
                .toUriString();
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