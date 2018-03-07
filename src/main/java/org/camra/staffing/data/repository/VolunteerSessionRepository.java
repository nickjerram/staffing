package org.camra.staffing.data.repository;

import com.vaadin.spring.annotation.SpringComponent;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.entity.VolunteerSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VolunteerSessionRepository extends JpaRepository<VolunteerSession, VolunteerSession.ID> {

    List<VolunteerSession> findByIdVolunteerId(int volunteerId, Pageable page);

    long countByIdVolunteerId(int volunteerId);
}
