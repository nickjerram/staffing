package org.camra.staffing.controller;

import org.camra.staffing.util.CaptchaImageGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;


@Controller
@SessionScope
public class CaptchaController {

    @Autowired private FormManager formManager;
    @Autowired private CaptchaImageGenerator captchaGenerator;

    @RequestMapping(value = "/captcha", method= RequestMethod.GET)
    public void getCaptchaImage(@RequestParam Optional<Boolean> refresh, HttpServletResponse response) throws Exception {
        if (refresh.orElse(false)) {
            formManager.clearCaptchaWord();
        }
        captchaGenerator.createImage(formManager.getCaptchaWord(), response.getOutputStream());
    }

}