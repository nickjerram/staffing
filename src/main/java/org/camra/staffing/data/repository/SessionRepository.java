package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Integer> {

    List<Session> findAllByOrderByStartAsc();
}