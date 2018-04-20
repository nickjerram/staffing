package org.camra.staffing.data.repository;

import org.camra.staffing.data.entityviews.AreaSelector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AreaSelectorRepository extends JpaRepository<AreaSelector, AreaSelector.ID> {

    List<AreaSelector> findByIdVolunteerIdOrderByIdAreaId(Integer volunteerId);
}
