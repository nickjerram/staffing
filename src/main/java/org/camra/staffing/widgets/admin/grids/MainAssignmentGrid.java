package org.camra.staffing.widgets.admin.grids;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.data.dto.MainViewDTO;
import org.camra.staffing.data.entityviews.MainView;
import org.camra.staffing.data.provider.MainAssignmentDataProvider;
import org.camra.staffing.data.provider.SortableDataProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@UIScope
@SpringComponent
public class MainAssignmentGrid extends MainGrid<MainViewDTO, MainView> {

    @Autowired private MainAssignmentDataProvider dataProvider;

    @PostConstruct
    private void init() {
        setSizeFull();
        setDataProvider(dataProvider);
        addColumn(MainViewDTO::getVolunteerId).setCaption("").setId("volunteerId");
        addColumn(MainViewDTO::getForename).setCaption("Forename").setId("forename");
        addColumn(MainViewDTO::getSurname).setCaption("Surname").setId("surname");
        addColumn(MainViewDTO::getSessionName).setCaption("Session").setId("session");
        addColumn(MainViewDTO::getAreaName).setCaption("Area").setId("area");
        addColumn(MainViewDTO::isCurrent).setCaption("Is Assigned").setId("current");
        addColumn(this::formatAssigned, new HtmlRenderer()).setCaption("Assigned").setId("assigned");
        addColumn(this::formatWorked, new HtmlRenderer()).setCaption("Worked").setId("worked");

        HeaderRow groupRow = prependHeaderRow();
        groupRow.join("volunteerId","forename","surname").setText("Volunteer");
    }

    private String formatAssigned(MainViewDTO dto) {
        return Columns.formatRatio(dto.getAssigned(), dto.getRequired());
    }

    private String formatWorked(MainViewDTO dto) {
        return Columns.formatRatio(dto.getWorked(), dto.getAssigned());
    }

    protected SortableDataProvider<MainViewDTO, MainView> dataProvider() {
        return dataProvider;
    }
}
