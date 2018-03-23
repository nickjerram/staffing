package org.camra.staffing.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class Properties {

    @Autowired private Environment environment;

    public String getBaseUrl() {
        return environment.getProperty("verification.url");
    }

    public String getSecurityKey() {
        return environment.getProperty("verification.key");
    }

    public String getFestivalName() {
        return environment.getProperty("staffing.festivalName");
    }

    public String getFestivalYear() {
        return environment.getProperty("staffing.festivalYear");
    }

    public String getMessage() {
        return environment.getProperty("staffing.message");
    }

    public String getAdminMessage() {
        return environment.getProperty("admin.message");
    }

    public String[] captchaWords() {
        return environment.getProperty("captcha.words", String[].class);
    }

}
