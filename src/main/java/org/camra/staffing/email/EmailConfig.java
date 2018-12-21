package org.camra.staffing.email;

import java.util.Properties;

public class EmailConfig {

    private String sendingHost;
    private String username;
    private String password;
    private String from;
    private String tls;
    private String sendingProtocol;
    private String auth;

    private String receivingProtocol;
    private String receivingHost;

    public void setProperties(Properties properties) {
        sendingHost = properties.getProperty("mail.smtp.host");
        username = properties.getProperty("mail.user");
        password = properties.getProperty("mail.password");
        from = properties.getProperty("mail.from");
        tls = properties.getProperty("mail.smtp.starttls.enable");
        sendingProtocol = properties.getProperty("mail.transport.protocol");
        auth = properties.getProperty("mail.smtp.auth");

        receivingProtocol = properties.getProperty("mail.store.protocol");
        receivingHost = properties.getProperty("mail.imap.host");
    }

    public String getSendingHost() {
        return sendingHost;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFrom() {
        return from;
    }

    public String getTls() {
        return tls;
    }

    public String getSendingProtocol() {
        return sendingProtocol;
    }

    public String getAuth() {
        return auth;
    }

    public String getReceivingProtocol() {
        return receivingProtocol;
    }

    public String getReceivingHost() {
        return receivingHost;
    }

}
