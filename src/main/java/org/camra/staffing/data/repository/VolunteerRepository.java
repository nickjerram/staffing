package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.Volunteer;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;
import java.util.Optional;

public interface VolunteerRepository extends JpaRepository<Volunteer, Integer>, JpaSpecificationExecutor<Volunteer> {

    Optional<Volunteer> findByUuid(String uuid);

    Optional<Volunteer> findByMembershipAndSurnameAndForename(String membership, String surname, String forename);

}
