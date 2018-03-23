package org.camra.staffing.util;

import com.vaadin.spring.annotation.UIScope;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Random;

@Controller
@Scope("session")
public class CaptchaController {

    @Autowired private CaptchaImageGenerator captchaGenerator;
    @Autowired Properties properties;
    @Getter private String word;


    @RequestMapping(value = "/captcha", method= RequestMethod.GET)
    public void getCaptchaImage(HttpServletResponse response) throws Exception {
        ServletRequestAttributes attr = (ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes();
        String[] words = properties.captchaWords();
        int element = new Random().nextInt(words.length);
        word = words[element];
        captchaGenerator.createImage(word, response.getOutputStream());
    }

}
