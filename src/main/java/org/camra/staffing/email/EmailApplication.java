package org.camra.staffing.email;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailApplication {

    private static final String MESSAGE = "/home/nick/dev/message";
    private static final String EMAILS = "/home/nick/dev/emails";

    public static void main(String[] args) throws IOException, InterruptedException {

        InputStream in = EmailApplication.class.getClassLoader().getResourceAsStream("application.properties");
        Properties props = new Properties();
        props.load(in);
        EmailConfig config = new EmailConfig();
        config.setProperties(props);

        EmailReceiver receiver = new EmailReceiver(config);

        GDPRMessageProcessor processor = new GDPRMessageProcessor();
        receiver.receiveMessages(processor);

        for (String from : processor.getMessages().keySet()) {
            System.out.println(from+" ==> "+processor.getMessages().get(from));
            System.out.println("====================================");
        }


    }

}