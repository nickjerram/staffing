package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.AssignedCounts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignedCountRepository extends JpaRepository<AssignedCounts, AssignedCounts.ID> {

    AssignedCounts findByIdAreaIdAndIdSessionId(int areaId, int sessionId);

    List<AssignedCounts> findByIdSessionId(int sessionId);
}