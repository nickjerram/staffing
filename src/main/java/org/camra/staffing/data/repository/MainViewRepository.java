package org.camra.staffing.data.repository;

import org.camra.staffing.data.entityviews.MainView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MainViewRepository extends JpaRepository<MainView, Integer>, JpaSpecificationExecutor<MainView> {

}
