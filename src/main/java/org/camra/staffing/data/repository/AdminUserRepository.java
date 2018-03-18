package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Integer> {

    AdminUser findByMembership(Integer membership);

    AdminUser findByUsername(String username);
}
