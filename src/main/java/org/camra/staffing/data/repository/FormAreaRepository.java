package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.FormArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormAreaRepository extends JpaRepository<FormArea, Integer> {

    List<FormArea> findAllByOrderById();

}
