package org.camra.staffing.data.repository;

import org.camra.staffing.data.entityviews.VolunteerSessionView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VolunteerSessionRepository extends JpaRepository<VolunteerSessionView, VolunteerSessionView.ID> {

    List<VolunteerSessionView> findByIdVolunteerId(int volunteerId, Pageable page);

    long countByIdVolunteerId(int volunteerId);
}
