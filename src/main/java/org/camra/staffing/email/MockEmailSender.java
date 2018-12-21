package org.camra.staffing.email;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class MockEmailSender implements EmailSender {

    PrintWriter out;

    public MockEmailSender() {
        File file = new File(System.getProperty("user.home"), "MockEmailSender.out");
        try {
            out = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean sendMessage(EmailMessage message) {

        if (message==null) {
            out.close();
            return false;
        }
        StringBuffer recips = new StringBuffer();
        for (String recip : message.getRecipients()) {
            recips.append(" "+recip);
        }
        out.println("TO:"+recips.toString());
        out.println("Subject: "+message.getSubject());
        out.println("Body:"+message.getBody());
        out.println("-------------------------------------------------------------------");
        out.println();
        return true;
    }

}