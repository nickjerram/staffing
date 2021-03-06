package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.AssignableArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignableAreaRepository extends JpaRepository<AssignableArea, Integer> {

    List<AssignableArea> findByFormAreaId(int id);

    default AssignableArea getUnassigned() {
        return getOne(AssignableArea.UNASSIGNED);
    }

    default AssignableArea getNotWorking() {
        return getOne(0);
    }
}
