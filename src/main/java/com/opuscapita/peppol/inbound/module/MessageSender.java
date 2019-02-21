package com.opuscapita.peppol.inbound.module;

import com.opuscapita.peppol.commons.container.ContainerMessage;
import com.opuscapita.peppol.commons.errors.ErrorHandler;
import com.opuscapita.peppol.commons.mq.MessageQueue;
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
    private final ErrorHandler errorHandler;

    @Autowired
    public MessageSender(@NotNull MessageQueue messageQueue, @NotNull ErrorHandler errorHandler) {
        this.messageQueue = messageQueue;
        this.errorHandler = errorHandler;
    }

    // no exception must be thrown by this method
    // in case of a failure we have local file to reprocess
    void send(ContainerMessage cm) {
        try {
            logger.debug("Sending message to " + outputQueue + " about file: " + cm.getFileName());

            messageQueue.convertAndSend(outputQueue, cm);

            logger.info("Message sent to " + outputQueue + ", about file " + cm.toLog());
        } catch (Exception e) {
            logger.error("Failed to report received file " + cm.getFileName() + " to queue " + outputQueue, e);
            errorHandler.reportWithContainerMessage(cm, e, "Failed to report received file " + cm.getFileName() + " to queue " + outputQueue);
            return;
        }

        try {
            messageQueue.convertAndSend(eventingQueue, cm);
        } catch (Exception e) {
            logger.error("Failed to report received file " + cm.getFileName() + " status to " + eventingQueue + " queue");
            errorHandler.reportWithContainerMessage(cm, e, "Failed to report file " + cm.getFileName() + " status to queue " + outputQueue);
        }
    }
}
