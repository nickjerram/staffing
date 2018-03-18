package org.camra.staffing.data.service;

import org.camra.staffing.data.entity.AdminUser;
import org.camra.staffing.data.repository.AdminUserRepository;
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
        Optional<AdminUser> adminUser = Optional.ofNullable(adminUserRepository.findByMembership(membership));
        adminUser.filter(u -> camraAuthentication.requestMemberDetails(u.getMembership(), password).isPresent());
        return adminUser;
    }

    private Optional<AdminUser> loginWithUsername(String username, String password) {
        AdminUser adminUser = adminUserRepository.findByUsername(username);

        String hashedPasswordAttempt = passwordHash.hashPassword(password);
        if (hashedPasswordAttempt.equals(adminUser.getPassword())) {
            return Optional.of(adminUser);
        } else {
           return Optional.empty();
        }

    }

    private int fromString(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

}
