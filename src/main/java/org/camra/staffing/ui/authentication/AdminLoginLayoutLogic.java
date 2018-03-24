package org.camra.staffing.ui.authentication;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import org.camra.staffing.AdminUI;
import org.camra.staffing.data.entity.AdminUser;
import org.camra.staffing.data.service.AdminLoginService;
import org.camra.staffing.ui.WelcomeLayoutLogic;
import org.camra.staffing.util.Properties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Optional;

@UIScope
@SpringComponent
public class AdminLoginLayoutLogic extends LoginLayout {

    @Autowired private AdminLoginService adminLoginService;
    @Autowired private AdminUI ui;
    @Value("${admin.login}") private String adminLogin;
    @Value("${admin.prompt}") private String adminPrompt;

    @PostConstruct
    private void init() {
        title.setValue(adminLogin);
        message.setValue(adminPrompt);
        login.addClickListener(this::login);
    }

    private void login(Button.ClickEvent clickEvent) {
        Optional<AdminUser> adminUser = adminLoginService.login(username.getValue(), password.getValue());
        adminUser.ifPresent(user -> ui.setAdminUser(user));
    }



}
