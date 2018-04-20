package org.camra.staffing.admin.grids;

import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.admin.access.Manager;
import org.camra.staffing.admin.popup.Confirmation;
import org.camra.staffing.data.dto.MainViewDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.entity.Preference;
import org.camra.staffing.data.entityviews.MainView;
import org.camra.staffing.data.provider.MainAssignmentDataProvider;
import org.camra.staffing.data.provider.SortableDataProvider;
import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@UIScope
@SpringComponent
public class MainAssignmentGrid extends AbstractGrid<MainViewDTO, MainView> {

    @Autowired private MainAssignmentDataProvider dataProvider;
    @Autowired private VolunteerService volunteerService;
    @Autowired private Manager manager;

    @PostConstruct
    private void init() {
        setSizeFull();
        setSelectionMode(SelectionMode.NONE);
        setDataProvider(dataProvider);
        addColumn(MainViewDTO::getVolunteerId).setCaption("").setId("volunteerId").setWidth(50);
        addColumn(this::formatVolunteer, new HtmlRenderer()).setCaption("Name").setId("volunteerName").setWidth(200);
        addColumn(MainViewDTO::getSessionName).setCaption("Session").setId("sessionName.start").setWidth(200);
        addColumn(this::formatArea, new HtmlRenderer()).setCaption("Area").setId("areaName").setWidth(200);
        addColumn(this::formatAssigned, new HtmlRenderer()).setCaption("Assigned").setId("assigned").setWidth(100);
        addColumn(this::formatWorked, new HtmlRenderer()).setCaption("Worked").setId("worked").setWidth(100);
        addColumn(this::formatCurrent, new HtmlRenderer()).setCaption("").setId("current").setWidth(75);
        addColumn(MainViewDTO::getCurrentAreaName).setCaption("Name").setId("currentAreaName").setWidth(250);
        addColumn(this::formatLocked, new HtmlRenderer()).setCaption("Locked").setId("locked").setWidth(75);
        addColumn(MainViewDTO::getComment).setCaption("Comment").setId("comment").setExpandRatio(1);


        HeaderRow groupRow = prependHeaderRow();
        groupRow.join("volunteerId","volunteerName").setText("Volunteer");
        groupRow.join("sessionName.start","areaName","assigned","worked","current").setText("Possible Assignment");
        groupRow.join("currentAreaName","locked","comment").setText("Actual Assignment");
        setFrozenColumnCount(5);

        addStringFilters("volunteerName","sessionName.start","areaName","currentAreaName");

        addRatioFilter("assigned","assigned","required","requiredRatio");

        addBooleanFilter("current");
        addItemClickListener(this::selectRow);
    }

    private void selectRow(ItemClick<MainViewDTO> event) {
        String columnId = event.getColumn().getId();
        if (columnId.equals("current")) {
            MainViewDTO item = event.getItem();
            if (!item.isLocked() && manager.isSuperUser()) {
                VolunteerSessionDTO reassignment = new VolunteerSessionDTO(item);
                reassignment.setAreaId(item.getAreaId());
                volunteerService.saveAssignment(reassignment);
                dataProvider.refreshAll();
            }
        } else if (columnId.equals("locked")) {
            MainViewDTO item = event.getItem();
            VolunteerSessionDTO reassignment = new VolunteerSessionDTO(item);
            reassignment.setLocked(!item.isLocked());
            volunteerService.saveAssignment(reassignment);
            dataProvider.refreshAll();
        } else if (columnId.equals("volunteerName")) {
            String volunteerComment = manager.isSuperUser() ? event.getItem().getVolunteerComment() : "";
            if (StringUtils.hasText(volunteerComment)) {
                UI.getCurrent().addWindow(new Confirmation(volunteerComment));
            }
        }
    }

    private String formatArea(MainViewDTO dto) {
        String yes = Columns.getIconCode("#aa0",dto.isYesArea() ? VaadinIcons.STAR : null);
        return yes+" "+dto.getAreaName();
    }

    private String formatVolunteer(MainViewDTO dto) {
        String name = dto.getVolunteerName();
        String volunteerComment = manager.isSuperUser() ? dto.getVolunteerComment() : "";
        String firstAid = Columns.formatBoolean("#00aa00", VaadinIcons.DOCTOR_BRIEFCASE, dto.isFirstaid(), false);
        String sia = Columns.formatBoolean("#ffff00", VaadinIcons.EYE, dto.isSia(), false);
        String cellar = Columns.formatBoolean("#444400", VaadinIcons.GLASS, dto.isCellar(), false);
        String comment = Columns.formatBoolean("#333", VaadinIcons.COMMENT_ELLIPSIS, StringUtils.hasText(volunteerComment), false);
        return name+" "+comment+" "+firstAid+sia+cellar;
    }


    private String formatAssigned(MainViewDTO dto) {
        return Columns.formatRatio(dto.getAssigned(), dto.getRequired());
    }

    private String formatWorked(MainViewDTO dto) {
        return Columns.formatRatio(dto.getWorked(), dto.getAssigned());
    }

    private String formatCurrent(MainViewDTO dto) {
        return dto.isCurrent() ? Columns.getYes() : Columns.getNo();
    }

    private String formatLocked(MainViewDTO dto) {
        return dto.isLocked() ? Columns.getIconCode("#900", VaadinIcons.LOCK) : "";
    }

    protected SortableDataProvider<MainViewDTO, MainView> dataProvider() {
        return dataProvider;
    }

}
