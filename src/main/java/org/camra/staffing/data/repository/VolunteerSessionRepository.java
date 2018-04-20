package org.camra.staffing.data.repository;

import org.camra.staffing.data.entityviews.VolunteerSessionView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface VolunteerSessionRepository extends JpaRepository<VolunteerSessionView, VolunteerSessionView.ID>,
        JpaSpecificationExecutor<VolunteerSessionView> {

    List<VolunteerSessionView> findByIdVolunteerId(int volunteerId, Pageable page);

    List<VolunteerSessionView> findByIdSessionId(int sessionId, Pageable page);

    long countByIdVolunteerId(int volunteerId);

    long countByIdSessionId(int sessionId);
}
