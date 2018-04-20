package org.camra.staffing.data.service;

import org.camra.staffing.data.dto.AreaSelectorDTO;
import org.camra.staffing.data.entity.AssignableArea;
import org.camra.staffing.data.repository.AssignableAreaRepository;
import org.camra.staffing.data.repository.FormAreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AreaService {

    @Autowired private FormAreaRepository formAreaRepository;
    @Autowired private AssignableAreaRepository assignableAreaRepository;

    public List<AreaSelectorDTO> getFormAreas() {
        return formAreaRepository.findAllByOrderById().stream().map(AreaSelectorDTO::create).collect(Collectors.toList());
    }

    public AreaSelectorDTO getFormArea(int assignableAreaId) {
        AssignableArea area = assignableAreaRepository.getOne(assignableAreaId);
        return AreaSelectorDTO.create(area.getFormArea());
    }
}
