package org.camra.staffing.data.provider;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.shared.Registration;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.entity.VolunteerSession;

import javax.annotation.PostConstruct;
import java.util.stream.Stream;

@SpringComponent
@UIScope
public class VolunteerSessionDataProvider implements DataProvider<VolunteerSessionDTO, VolunteerSession.ID> {

    @PostConstruct
    private void init() {
        //DataProvider.fromCallbacks()
    }

    public boolean isInMemory() {
        return false;
    }

    @Override
    public int size(Query<VolunteerSessionDTO, VolunteerSession.ID> query) {
        return 0;
    }

    @Override
    public Stream<VolunteerSessionDTO> fetch(Query<VolunteerSessionDTO, VolunteerSession.ID> query) {
        return null;
    }

    @Override
    public void refreshItem(VolunteerSessionDTO volunteerSessionDTO) {

    }

    @Override
    public void refreshAll() {

    }

    @Override
    public Registration addDataProviderListener(DataProviderListener<VolunteerSessionDTO> dataProviderListener) {
        return null;
    }
}
