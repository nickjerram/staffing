package org.camra.staffing.data.service;


import org.camra.staffing.data.dto.MainViewDTO;
import org.camra.staffing.data.entityviews.MainView;
import org.camra.staffing.data.repository.MainViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class MainViewService {

    @Autowired private MainViewRepository mainViewRepository;

    public Stream<MainViewDTO> getRecords(Specification<MainView> specification, Pageable page) {
        return mainViewRepository.findAll(specification, page).getContent().stream().map(MainViewDTO::create);
    }

    public int getRecordCount(Specification<MainView> specification) {
        return (int) mainViewRepository.count(specification);
    }

}
