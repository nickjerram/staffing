package org.camra.staffing.data.repository;

import org.camra.staffing.data.entity.MainView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface MainViewRepository extends JpaRepository<MainView, Integer>, QueryByExampleExecutor<MainView> {

}
