package org.camra.staffing.controller;

import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.annotation.SessionScope;

import javax.servlet.http.HttpSession;
import java.util.Optional;
import java.util.Random;

@Service
@SessionScope
public class FormManager {

    private static final String STATE = "state";
    private static final String VOLUNTEER = "volunteer";
    private static final String CAPTCHA = "captcha";

    @Autowired private HttpSession httpSession;
    @Value("${captcha.words}") private String captchaWords;


    public enum State {
        Verify,
        Email,
        Form,
        VolunteerFound,
        VolunteerNotFound,
        Finished
    }

    State getState() {
        State state = (State) httpSession.getAttribute(STATE);
        return state==null ? State.Verify : state;
    }

    void setVerify() {
        httpSession.setAttribute(STATE, State.Verify);
    }

    void setEmail() {
        httpSession.setAttribute(STATE, State.Email);
    }

    void setVolunteerFound() {
        httpSession.setAttribute(STATE, State.VolunteerFound);
    }

    void setVolunteerNotFound() {
        httpSession.setAttribute(STATE, State.VolunteerNotFound);
    }

    void setForm() {
        httpSession.setAttribute(STATE, State.Form);
    }

    void setFinished() {
        httpSession.setAttribute(STATE, State.Finished);
    }


    String getCaptchaWord() {
        String captchaWord = (String) httpSession.getAttribute(CAPTCHA);
        if (captchaWord==null) {
            String[] words = captchaWords.split(",");
            int element = new Random().nextInt(words.length);
            captchaWord = words[element];
            httpSession.setAttribute(CAPTCHA, captchaWord);
        }
        return captchaWord;
    }

    void clearCaptchaWord() {
        httpSession.removeAttribute(CAPTCHA);
    }

    boolean verifyCaptcha(String input) {
        String acceptable = clean(getCaptchaWord());
        Optional<String> maybeAttempt = Optional.ofNullable(input);
        String attempt = maybeAttempt.map(this::clean).orElse("");
        return attempt.equals(acceptable);
    }

    VolunteerDTO getVolunteer() {
        return (VolunteerDTO) httpSession.getAttribute(VOLUNTEER);
    }

    void setVolunteer(VolunteerDTO volunteer) {
        httpSession.setAttribute(VOLUNTEER, volunteer);
    }

    private String clean(String input) {
        return input.replaceAll("\\W","").toLowerCase();
    }

}
