package org.camra.staffing.data.service;

import com.vaadin.data.provider.Query;
import org.camra.staffing.data.dto.MainViewDTO;
import org.camra.staffing.data.entity.MainView;
import org.camra.staffing.data.repository.MainViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MainViewService extends AbstractExampleService<MainViewDTO, MainView> {

    @Autowired private MainViewRepository mainViewRepository;

    public Stream<MainViewDTO> getRecords(Specification<MainView> specification, Pageable pageable) {
        return mainViewRepository.findAll(specification,pageable)
                .getContent().stream().map(MainViewDTO::create);
    }

    public int countRecords(Specification<MainView> specification) {
        return (int) mainViewRepository.count(specification);
    }

    public List<MainViewDTO> getRecords(Query<MainViewDTO, Example<MainView>> query) {
        Page<MainView> queryResult;
        if (query.getFilter().isPresent()) {
            queryResult = mainViewRepository.findAll(query.getFilter().get(), pageRequest(query, "surname"));
        } else {
            queryResult = mainViewRepository.findAll(pageRequest(query, "surname"));
        }
        System.out.println("page "+queryResult.getNumber()+" out of "+queryResult.getTotalPages()+" pagesize "+queryResult.getNumberOfElements());
        return queryResult.getContent().stream().map(MainViewDTO::create).collect(Collectors.toList());
    }

    public int countRecords(Query<MainViewDTO,Example<MainView>> query) {
        int count = 0;
        if (query.getFilter().isPresent()) {
            count = (int) mainViewRepository.count(query.getFilter().get());
        } else {
            count = (int) mainViewRepository.count();
        }
        System.out.println("count="+count);
        return count;
    }

}
