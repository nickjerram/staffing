package org.camra.staffing.data.provider;

import com.vaadin.data.provider.*;
import com.vaadin.shared.Registration;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.service.AbstractExampleService;
import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@SpringComponent
@UIScope
public class VolunteerDataProvider extends ExampleDataProvider<VolunteerDTO, Volunteer> {

    @Autowired private VolunteerService volunteerService;
    private Map<String,String> filters = new HashMap<>();


    @PostConstruct
    private void init() {
        createDelegate();
    }

    @Override
    protected AbstractExampleService<VolunteerDTO, Volunteer> getService() {
        return volunteerService;
    }

    @Override
    public void setFilter(Map<String,String> filter) {
        ExampleMatcher matcher = ExampleMatcher.matching();
        for (String field : filter.keySet()) {
            matcher = matcher.withMatcher(field, match -> match.contains().ignoreCase());
        }
        Volunteer sampleVolunteer = new Volunteer();
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(sampleVolunteer);
        wrapper.setPropertyValues(filter);
        Example<Volunteer> example = Example.of(sampleVolunteer, matcher);
        delegate.setFilter(example);
    }


    public void addFilter(String field, String value) {
        if (StringUtils.isEmpty(value)) {
            filters.remove(field);
        } else {
            filters.put(field, value);
        }
        setFilter(filters);
    }

}
