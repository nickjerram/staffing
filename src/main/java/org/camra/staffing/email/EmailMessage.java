package org.camra.staffing.email;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Data
public class EmailMessage {

    private String body;
    private String subject;
    private String sender;
    private Date receivedDate;
    private List<String> recipients = new ArrayList<>();

    public void addRecipient(String email) {
        recipients.add(email);
    }

    public boolean hasRecipients() {
        return recipients.size()>0;
    }


}
