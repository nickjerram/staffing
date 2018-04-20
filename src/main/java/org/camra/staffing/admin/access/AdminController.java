package org.camra.staffing.admin.access;

import org.camra.staffing.data.entity.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Controller
public class AdminController {

    @Autowired private Manager manager;

    @GetMapping("/login")
    public String getAdminLoginForm() {
        return "login";
    }

    @PostMapping(path = "/login", consumes = "application/json")
    @ResponseBody
    public AdminDTO adminLogin(@RequestBody AdminDTO requestData) {
        manager.login(requestData.user, requestData.password);
        Optional<AdminUser> user = manager.getUser();
        return user.map(this::fromUser).orElse(failure());
    }

    private AdminDTO fromUser(AdminUser user) {
        AdminDTO dto = new AdminDTO();
        dto.success = true;
        return dto;
    }

    private AdminDTO failure() {
        AdminDTO dto = new AdminDTO();
        dto.error = true;
        dto.errorMessage = "Invalid username/password";
        return dto;
    }

}
