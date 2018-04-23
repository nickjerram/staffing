package org.camra.staffing.email;

import java.util.Date;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.camra.staffing.data.dto.VolunteerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Configuration
public class EmailSender {

    private Properties properties;
    @Autowired private ConfirmationMessageBuilder messageBuilder;
    @Value("${mail.smtp.starttls.enable}") private String startTls;
    @Value("${mail.transport.protocol}") private String protocol;
    @Value("${mail.smtp.auth}") private String auth;
    @Value("${mail.smtp.host}") private String host;
    @Value("${mail.user}") private String user;
    @Value("${mail.password}") private String password;
    @Value("${staffing.festivalName}") private String festivalName;

    @PostConstruct
    public void init() {
        properties = new Properties();
        properties.setProperty("mail.smtp.starttls.enable", startTls);
        properties.setProperty("mail.transport.protocol", protocol);
        properties.setProperty("mail.smtp.auth", auth);
    }

    public boolean sendConfirmation(VolunteerDTO volunteer) {
        String messageText = messageBuilder.buildMessage(volunteer);
        if (StringUtils.hasText(messageText)) {
            EmailMessage message = new EmailMessage();
            message.addRecipient(volunteer.getEmail());
            message.setSubject(festivalName);
            message.setBody(messageText);
            return sendMessage(message);
        } else {
            return false;
        }
    }

    public boolean sendMessage(EmailMessage emailMessage) {

        if (emailMessage==null) {
            return false;
        }

        try {
            javax.mail.Session session = javax.mail.Session.getInstance(properties, null);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, user, password);

            Message msg = new MimeMessage(session);
            msg.setFrom(InternetAddress.parse(user, false)[0]);
            msg.setSentDate(new Date());

            if (emailMessage.getRecipients().size()==1) {
                msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailMessage.getRecipients().get(0)));
            } else {
                for (String toAddress : emailMessage.getRecipients()) {
                    InternetAddress address;
                    try {
                        address = new InternetAddress(toAddress);
                        msg.addRecipient(Message.RecipientType.BCC, address);
                    } catch (AddressException e) {
                        System.out.println("Invalid email address:"+toAddress);
                    }
                }
            }


            // set title and body
            msg.setSubject(emailMessage.getSubject());
            //msg.setText(emailMessage.getBody());
            msg.setContent(emailMessage.getBody(), "text/html; charset=utf-8");

            // off goes the message...
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

}
