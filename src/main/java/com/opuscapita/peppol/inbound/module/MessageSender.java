package com.opuscapita.peppol.inbound.module;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.eventing.TicketReporter;
import com.opuscapita.peppol.commons.queue.MessageQueue;
import org.jetbrains.annotations.NotNull;
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

    @Value("${peppol.eventing.queue.in.name}")
    private String eventingQueue;

    private final MessageQueue messageQueue;
    private final TicketReporter ticketReporter;

    @Autowired
    public MessageSender(@NotNull MessageQueue messageQueue, @NotNull TicketReporter ticketReporter) {
        this.messageQueue = messageQueue;
        this.ticketReporter = ticketReporter;
    }

    // no exception must be thrown by this method
    // in case of a failure we have local file to reprocess
    void send(ContainerMessage cm) {
        try {
            messageQueue.convertAndSend(outputQueue, cm);
            logger.info("Message sent to " + outputQueue + " queue, about file " + cm.toKibana());
        } catch (Exception e) {
            logger.error("Failed to report received file " + cm.getFileName() + " to queue " + outputQueue, e);
            ticketReporter.reportWithContainerMessage(cm, e, "Failed to report received file " + cm.getFileName() + " to queue " + outputQueue);
            return;
        }

        try {
            messageQueue.convertAndSend(eventingQueue, cm);
            logger.info("Message sent to " + eventingQueue + " queue, about file " + cm.toKibana());
        } catch (Exception e) {
            logger.error("Failed to report received file " + cm.getFileName() + " status to " + eventingQueue + " queue");
            ticketReporter.reportWithContainerMessage(cm, e, "Failed to report file " + cm.getFileName() + " status to queue " + outputQueue);
        }
    }

}
