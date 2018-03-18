package org.camra.staffing.data.service;

import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.entity.Session;
import org.camra.staffing.data.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

@Service
public class SessionService {

    @Autowired private SessionRepository sessionRepository;

    public List<SessionDTO> getSessions() {
        return sessionRepository.findAllByOrderByStartAsc().stream().map(SessionDTO::create).collect(Collectors.toList());
    }

    public Map<LocalDate, List<SessionDTO>> getSessionMap() {
        return sessionRepository.findAllByOrderByStartAsc().stream()
                .map(SessionDTO::create).collect(groupingBy(s->s.getDay(), LinkedHashMap::new, toList()));
    }



}
