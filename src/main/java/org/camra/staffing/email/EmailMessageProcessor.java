package org.camra.staffing.email;

import javax.mail.Message;
import javax.mail.MessagingException;

public interface EmailMessageProcessor {

    boolean filter(Message message) throws MessagingException;

    boolean processMessage(EmailMessage message);

    void handleException(Exception e);
}