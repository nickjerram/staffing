package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.PossibleSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PossibleSessionRepository extends JpaRepository<PossibleSession, PossibleSession.ID> {

    List<PossibleSession> findByIdVolunteerId(int volunteerId);
}
