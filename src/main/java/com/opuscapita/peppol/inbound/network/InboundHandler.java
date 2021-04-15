package com.opuscapita.peppol.inbound.network;

import com.opuscapita.peppol.commons.container.metadata.ContainerMessageMetadata;
import com.opuscapita.peppol.commons.container.state.Source;
import com.opuscapita.peppol.inbound.InboundModule;
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
import java.util.Date;
import java.text.SimpleDateFormat;

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
        this.messageHandler = InboundModule.getMessageHandler();
        logger.info("OpusCapita inbound receiver initialized");
    }

    // file coming from network: oxalis payload persister
    @Override
    public Path persist(TransmissionIdentifier transmissionIdentifier, Header header, InputStream inputStream) throws IOException {
        String filename = header.getIdentifier().getIdentifier() + ".xml";
        logger.info("Received a message from NETWORK, storing content as: " + filename);
        return Paths.get(messageHandler.store(filename, Source.NETWORK, inputStream));
    }

    // file coming from network: oxalis receipt persister
    @Override
    public void persist(InboundMetadata inboundMetadata, Path payloadPath) {
        Source source = Source.NETWORK;
        String filename = payloadPath.toString();
        logger.debug("Second InboundHandler.persist invoked for file: " + filename);
        ContainerMessageMetadata metadata = ContainerMessageMetadata.create(inboundMetadata);
        logger.debug("ContainerMessageMetadata successfully created for file: " + filename);

        logReceipt(metadata, source, filename);
        messageHandler.process(metadata, source, filename);
    }

    // file coming from business platform: source, both payload and receipt persistence
    @Override
    public void persist(String filename, Source source, ServletRequestWrapper wrapper) throws IOException {
        ContainerMessageMetadata metadata;

        logger.info("TODO entering persist = " + filename +" source=" + source, " wrapper=" + wrapper );

        if( source != Source.GW ) {
          logger.info("TODO: source != Source.GW" );
          metadata = messageHandler.extractMetadata(wrapper);
        }
        else {
          logger.info("TODO: source == Source.GW" );
          metadata = this.extractGWMetadataFromHeaderParams(wrapper);
          logger.info("TODO: source == Source.GW - after" );
        }
        filename = StringUtils.isBlank(filename) ? metadata.getMessageId() + ".xml" : filename;
logger.info("TODO: 30" );
        logger.info("Received a message from " + source.name() + ", storing content as: " + filename);
        String dataFile = messageHandler.store(filename, source, wrapper.getInputStream());
logger.info("TODO: 40" );
        logReceipt(metadata, source, dataFile);

        logger.info("TODO: 50" );
        messageHandler.process(metadata, source, dataFile);

        logger.info("TODO: 60" );
    }


    ContainerMessageMetadata extractGWMetadataFromHeaderParams( ServletRequestWrapper wrapper ) { //TODO move to common??
        ContainerMessageMetadata md = new ContainerMessageMetadata();

        md.setRecipientId(  "0000:000000000" );
        md.setSenderId(     "0000:000000000" );
        md.setDocumentTypeIdentifier( "cust:opuscapita:unidentified-document" );
        md.setProfileTypeIdentifier(  "cust:opuscapita:unidentified-process" );

        md.setMessageId( wrapper.getHeader("transactionid") );
        md.setTransmissionId( wrapper.getHeader("transactionId") );

        md.setDocumentTypeIdentifier(     "0000:000000000" );
        md.setProfileTypeIdentifier(     "0000:000000000" );

        md.setProtocol( wrapper.getHeader("protocol") );
        md.setUserAgent( wrapper.getHeader("useragent") );
        md.setUserAgentVersion( wrapper.getHeader("useragentversion") );

        md.setSendingAccessPoint( "GW:" + wrapper.getHeader("gwalias") + ":" + wrapper.getHeader("gwaccount") );

        try {
          md.setTimestamp(
            new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss").parse( wrapper.getHeader("gwreceivetimestamp") )
            );
          }
        catch(Exception e) {
          md.setTimestamp( new Date() );
        }

       return md;
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
