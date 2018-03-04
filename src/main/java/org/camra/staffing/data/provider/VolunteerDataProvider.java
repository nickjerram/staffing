package org.camra.staffing.data.provider;

import com.vaadin.data.provider.*;
import com.vaadin.shared.Registration;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.Volunteer;
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
public class VolunteerDataProvider implements ConfigurableFilterDataProvider<VolunteerDTO, Example<Volunteer>, Map<String,String>> {

    @Autowired private VolunteerService volunteerService;
    private ConfigurableFilterDataProvider<VolunteerDTO, Example<Volunteer>, Example<Volunteer>> delegate;
    private Map<String,String> filters = new HashMap<>();


    @PostConstruct
    private void init() {
        CallbackDataProvider<VolunteerDTO,Example<Volunteer>> callbackDataProvider = DataProvider.fromFilteringCallbacks(
            q -> volunteerService.getVolunteers(q).stream(),
            q -> volunteerService.countVolunteers(q)
        );
        delegate = callbackDataProvider.withConfigurableFilter((q,c) -> c);
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

    @Override
    public boolean isInMemory() {
        return delegate.isInMemory();
    }

    @Override
    public int size(Query<VolunteerDTO, Example<Volunteer>> query) {
        return delegate.size(query);
    }

    @Override
    public Stream<VolunteerDTO> fetch(Query<VolunteerDTO, Example<Volunteer>> query) {
        return delegate.fetch(query);
    }

    @Override
    public void refreshItem(VolunteerDTO volunteerDTO) {
        delegate.refreshItem(volunteerDTO);
    }

    @Override
    public void refreshAll() {
        delegate.refreshAll();
    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<VolunteerDTO> dataProviderListener) {
        return delegate.addDataProviderListener(dataProviderListener);
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
