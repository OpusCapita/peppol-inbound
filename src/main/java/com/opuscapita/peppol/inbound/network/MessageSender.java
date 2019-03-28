package com.opuscapita.peppol.inbound.network;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.eventing.EventReporter;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.queue.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageSender {

    private final static Logger logger = LoggerFactory.getLogger(MessageSender.class);

    @Value("${peppol.inbound.queue.out.name}")
    private String outputQueue;

    private final MessageQueue messageQueue;
    private final EventReporter eventReporter;
    private final TicketReporter ticketReporter;

    @Autowired
    public MessageSender(MessageQueue messageQueue, EventReporter eventReporter, TicketReporter ticketReporter) {
        this.messageQueue = messageQueue;
        this.eventReporter = eventReporter;
        this.ticketReporter = ticketReporter;
    }

    // no exception must be thrown by this method
    // in case of a failure we have local file to reprocess
    void send(ContainerMessage cm) {
        eventReporter.reportStatus(cm);

        try {
            messageQueue.convertAndSend(outputQueue, cm);
            logger.info("Message sent to " + outputQueue + " queue, about file " + cm.toKibana());
        } catch (Exception e) {
            logger.error("Failed to report received file " + cm.getFileName() + " to queue " + outputQueue, e);
            ticketReporter.reportWithContainerMessage(cm, e, "Failed to report received file " + cm.getFileName() + " to queue " + outputQueue);
        }
    }

}
