package org.camra.staffing.data.service;

import org.camra.staffing.data.dto.AssignedCountsDTO;
import org.camra.staffing.data.entityviews.AssignedCounts;
import org.camra.staffing.data.repository.AssignedCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssignedCountsService {

    @Autowired private AssignedCountRepository assignedCountRepository;

    public Map<Integer, List<AssignedCountsDTO>> getCountsBySession() {
        List<AssignedCounts> counts = assignedCountRepository.findAll();
        return counts.stream().map(AssignedCountsDTO::create).collect(Collectors.groupingBy(AssignedCountsDTO::getSessionId));
    }
}
