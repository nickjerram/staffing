package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Integer>, JpaSpecificationExecutor<Session> {

    List<Session> findAllByOrderByStartAsc();
}