package org.camra.staffing.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.search.FlagTerm;

public class EmailReceiver {

    private Session session;
    private EmailConfig config;

    public EmailReceiver(EmailConfig config) {
        Properties properties = new Properties();
        this.config = config;
        properties.setProperty("mail.store.protocol", config.getReceivingProtocol());
        session = Session.getDefaultInstance(properties, null);
    }

    public void receiveMessages(EmailMessageProcessor messageProcessor) {

        Store store = null;
        Folder inbox = null;

        try {
            store = session.getStore(config.getReceivingProtocol());
            store.connect(config.getSendingHost(), config.getUsername(), config.getPassword());

            inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);

            //FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            FlagTerm ft = new FlagTerm(new Flags(), false);
            System.out.println("About to search messages");
            Message[] messages = inbox.getMessages();
            System.out.println("Found "+messages.length+" messages");

            for(Message message : messages) {
                if (messageProcessor.filter(message)) {
                    EmailMessage emailMessage = new EmailMessage();
                    String text = getBody(message);
                    Address[] from = message.getFrom();
                    if (from.length >= 1) {
                        emailMessage.setSender(extractEmailAddress(from[0].toString()));
                    }
                    emailMessage.setReceivedDate(message.getReceivedDate());
                    emailMessage.setSubject(message.getSubject());
                    emailMessage.setBody(text);
                    boolean result = messageProcessor.processMessage(emailMessage);
                    //message.setFlag(Flags.Flag.SEEN, result);
                }
            }

        } catch (Exception e) {
            messageProcessor.handleException(e);
        } finally {
            tryClose(inbox);
            tryClose(store);
        }


    }

    private String getBody(Message message) throws IOException, MessagingException {
        List<String> stringParts = new ArrayList<>();
        Object content = message.getContent();
        if (content instanceof  Multipart) {
            Multipart mp = (Multipart) content;
            for (int i=0; i<mp.getCount(); i++) {
                Object body = mp.getBodyPart(i).getContent();
                stringParts.add(body.toString());
            }
        } else if (content instanceof String) {
            stringParts.add((String) content);
        }
        return stringParts.stream().reduce("\\n", String::concat);
    }

    private void tryClose(Object object) {
        if (object==null) return;
        try {
            if (object instanceof Store) {
                ((Store) object).close();
            } else if (object instanceof Folder) {
                ((Folder) object).close(false);
            }
        } catch (Exception e) {
            System.out.println("Exception closing "+object);
        }
    }


    private String extractEmailAddress(String address) {
        if (address.indexOf("<")>=0) {
            int startAddress = address.indexOf("<");
            int endAddress = address.indexOf(">");
            return address.substring(startAddress+1,endAddress);
        } else {
            return address;
        }
    }

}