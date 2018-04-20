package org.camra.staffing.data.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name="admin_login")
@Cacheable(false)
@Getter
public class AdminUser {

    @Id private Integer id;
    private Integer membership;
    private @Column(length=50) String username;
    private @Column(length=50) String password;
    private @Column(name="super") boolean superuser;

    @Transient @Setter private String attempt;

    public static AdminUser getLocalUser() {
        AdminUser local = new AdminUser();
        local.username = "Local";
        local.superuser = true;
        return local;
    }
}
