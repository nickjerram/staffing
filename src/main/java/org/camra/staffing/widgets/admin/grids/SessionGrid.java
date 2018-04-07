package org.camra.staffing.widgets.admin.grids;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.entity.Session;
import org.camra.staffing.data.provider.SessionDataProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class SessionGrid extends AbstractGrid<SessionDTO, Session> {

    @Autowired private SessionDataProvider dataProvider;

    @PostConstruct
    private void init() {
        setSizeFull();
        setDataProvider(dataProvider);

    }
}
