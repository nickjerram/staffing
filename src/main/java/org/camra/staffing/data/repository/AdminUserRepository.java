package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Integer> {

    Optional<AdminUser> findByMembership(Integer membership);

    Optional<AdminUser> findByUsername(String username);
}
