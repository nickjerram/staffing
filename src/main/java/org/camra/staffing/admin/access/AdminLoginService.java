package org.camra.staffing.admin.access;

import org.camra.staffing.data.entity.AdminUser;
import org.camra.staffing.data.repository.AdminUserRepository;
import org.camra.staffing.data.service.CamraAuthentication;
import org.camra.staffing.data.service.PasswordHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AdminLoginService {

    @Autowired private AdminUserRepository adminUserRepository;
    @Autowired private PasswordHash passwordHash;
    @Autowired private CamraAuthentication camraAuthentication;

    public Optional<AdminUser> login(String username, String password) {
        int membership = fromString(username);
        if (membership>0) {
            return loginWithMembership(membership, password);
        } else {
            return loginWithUsername(username, password);
        }
    }

    private Optional<AdminUser> loginWithMembership(int membership, String password) {
        Optional<AdminUser> adminUser = adminUserRepository.findByMembership(membership);
        return adminUser.filter(u -> camraAuthentication.requestMemberDetails(""+u.getMembership(), password).isPresent());
    }

    private Optional<AdminUser> loginWithUsername(String username, String passwordAttempt) {
        Optional<AdminUser> adminUser = adminUserRepository.findByUsername(username);
        adminUser.ifPresent(user -> user.setAttempt(passwordAttempt));
        return adminUser.filter(this::checkPassword);
    }

    private int fromString(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    private boolean checkPassword(AdminUser user) {
        String hashedPasswordAttempt = passwordHash.hashPassword(user.getAttempt());
        boolean passwordOK = hashedPasswordAttempt.equals(user.getPassword());
        return passwordOK;
    }

}
