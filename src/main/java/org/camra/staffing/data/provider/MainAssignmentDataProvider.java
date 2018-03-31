package org.camra.staffing.data.provider;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import org.camra.staffing.data.dto.MainViewDTO;
import org.camra.staffing.data.entity.MainView;
import org.camra.staffing.data.service.AbstractExampleService;
import org.camra.staffing.data.service.MainViewService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Map;

@SpringComponent
@UIScope
public class MainAssignmentDataProvider extends ExampleDataProvider<MainViewDTO, MainView> {

    @Autowired private MainViewService mainViewService;

    @PostConstruct
    private void init() {
        createDelegate();
    }

    @Override
    protected AbstractExampleService<MainViewDTO, MainView> getService() {
        return mainViewService;
    }

    @Override
    public void setFilter(Map<String, String> stringStringMap) {

    }
}
