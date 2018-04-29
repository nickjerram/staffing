package org.camra.staffing.admin.access;

import lombok.Getter;
import lombok.Setter;
import org.camra.staffing.data.dto.BadgeDTO;
import org.camra.staffing.data.entity.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Service
@SessionScope
public class Manager {

    private Optional<AdminUser> currentUser = Optional.empty();
    @Getter @Setter private List<BadgeDTO> badges;
    @Autowired private AdminLoginService loginService;
    @Autowired private Environment environment;

    void login(String username, String password) {
       currentUser = loginService.login(username, password);
    }

    public void adminLogout() {
        currentUser = Optional.empty();
    }

    public Optional<AdminUser> getUser() {
        if (environment.acceptsProfiles("dev")) {
            return Optional.of(AdminUser.getLocalUser());
        } else {
            return currentUser;
        }
    }

    public boolean isSuperUser() {
        return getUser().map(u->u.isSuperuser()).orElse(false);
    }

}
