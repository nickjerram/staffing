package org.camra.staffing.email;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

public class GDPRMessageProcessor implements EmailMessageProcessor {

    private Map<String,String> messages = new HashMap<>();

    @Override
    public boolean filter(Message message) throws MessagingException {
        System.out.println("Filter message "+message.getReceivedDate());
        return message!=null && message.getSubject()!=null && message.getSubject().contains("GDPR");
    }

    @Override
    public boolean processMessage(EmailMessage message) {
        messages.put(message.getSender(), message.getBody());
        return true;
    }

    @Override
    public void handleException(Exception e) {
        e.printStackTrace();
    }

    public Map<String,String> getMessages() {
        return messages;
    }
}
