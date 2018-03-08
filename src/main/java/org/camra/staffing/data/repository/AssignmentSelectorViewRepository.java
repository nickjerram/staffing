package org.camra.staffing.data.repository;

import org.camra.staffing.data.entityviews.AssignmentSelectorView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentSelectorViewRepository extends JpaRepository<AssignmentSelectorView, AssignmentSelectorView.ID> {

    List<AssignmentSelectorView> findByIdVolunteerIdAndIdSessionId(int volunteerId, int sessionId);

}
