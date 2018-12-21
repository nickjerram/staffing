package org.camra.staffing.email;

import javax.mail.Message;

public class BasicMessageprocessor implements EmailMessageProcessor {

    @Override
    public boolean filter(Message message) {
        return true;
    }

    @Override
    public boolean processMessage(EmailMessage message) {
        System.out.println("Message "+message.getSubject()+" "+message.getReceivedDate()+" "+message.getSender());
        return true;
    }

    @Override
    public void handleException(Exception e) {
        e.printStackTrace();
    }
}
