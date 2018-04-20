package org.camra.staffing.data.repository;

import org.camra.staffing.data.entityviews.PossibleSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PossibleSessionRepository extends JpaRepository<PossibleSession, PossibleSession.ID> {

    List<PossibleSession> findByIdVolunteerIdOrderByStart(int volunteerId);
}
