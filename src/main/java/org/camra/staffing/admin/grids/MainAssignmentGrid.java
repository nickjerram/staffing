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
import org.camra.staffing.admin.popup.Editor;
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
    private boolean unlockingEnabled = false;

    public void setUnlockingEnabled(boolean enabled) {
        this.unlockingEnabled = enabled;
        dataProvider.refreshAll();
    }

    @PostConstruct
    private void init() {
        setSizeFull();
        setSelectionMode(SelectionMode.NONE);
        setDataProvider(dataProvider);
        addColumn(MainViewDTO::getVolunteerId).setCaption("").setId("volunteerId").setWidth(50);
        addColumn(this::formatAssignmentsConfirmed, new HtmlRenderer()).setCaption("").setId("other").setWidth(65);
        addColumn(this::formatVolunteer, new HtmlRenderer()).setCaption("Name").setId("volunteerName").setWidth(200);
        addColumn(this::formatComment, new HtmlRenderer()).setCaption("").setId("hasComment").setWidth(65);
        addColumn(MainViewDTO::getSessionName).setCaption("Session").setId("sessionName.start").setWidth(200);
        addColumn(this::formatArea, new HtmlRenderer()).setCaption("Area").setId("areaName").setWidth(200);
        addColumn(this::formatAssigned, new HtmlRenderer()).setCaption("Assigned").setId("assigned").setWidth(100);
        addColumn(this::formatWorked, new HtmlRenderer()).setCaption("Worked").setId("worked").setWidth(100);
        addColumn(this::formatCurrent, new HtmlRenderer()).setCaption("").setId("current").setWidth(65);
        addColumn(this::formatLocked, new HtmlRenderer()).setCaption("Lock").setId("locked").setWidth(65);

        addColumn(MainViewDTO::getCurrentAreaName).setCaption("Name").setId("currentAreaName").setWidth(250);
        addColumn(MainViewDTO::getComment).setCaption("Comment").setId("sessionComment").setExpandRatio(1);


        HeaderRow groupRow = prependHeaderRow();
        groupRow.join("volunteerId","other","volunteerName","hasComment").setText("Volunteer");
        groupRow.join("sessionName.start","areaName","assigned","worked","current","locked").setText("Possible Assignment");
        groupRow.join("currentAreaName","sessionComment").setText("Actual Assignment");
        setFrozenColumnCount(5);

        addStringFilters("volunteerName","sessionName.start","areaName","currentAreaName");

        addRatioFilter("assigned","assigned","required","requiredRatio");

        addBooleanFilter("other");
        addBooleanFilter("hasComment");
        addBooleanFilter("current");
        addItemClickListener(this::selectRow);
    }

    private void selectRow(ItemClick<MainViewDTO> event) {
        String columnId = event.getColumn().getId();
        MainViewDTO item = event.getItem();
        int volunteerId = item.getVolunteerId();
        int sessionId = item.getSessionId();
        if (columnId.equals("current")) {
            if (!item.isLocked() && manager.isSuperUser()) {
                VolunteerSessionDTO reassignment = new VolunteerSessionDTO(item);
                reassignment.setAreaId(item.getAreaId());
                volunteerService.saveAssignment(reassignment);
                dataProvider.refreshAll();
            }
        } else if (columnId.equals("locked")) {
            if (!item.isLocked() || unlockingEnabled) {
                volunteerService.lockAssignment(volunteerId, sessionId, !item.isLocked());
                dataProvider.refreshAll();
            }
        } else if (columnId.equals("hasComment")) {
            String volunteerComment = manager.isSuperUser() ? event.getItem().getVolunteerComment() : "";
            if (StringUtils.hasText(volunteerComment)) {
                UI.getCurrent().addWindow(new Confirmation(volunteerComment));
            }
        } else if (columnId.equals("sessionComment")) {
            UI.getCurrent().addWindow(new Editor("Session Comment", item.getComment(), comment -> {
                volunteerService.saveAssignmentComment(volunteerId, sessionId, comment);
                dataProvider.refreshAll();
            }));
        } else if (columnId.equals("other")) {
            volunteerService.setVolunteerAssignmentsConfirmed(volunteerId, !item.isOther());
            dataProvider.refreshAll();
        }
    }

    private String formatArea(MainViewDTO dto) {
        String yes = Columns.getIconCode("#aa0",dto.isYesArea() ? VaadinIcons.STAR : null);
        return yes+" "+dto.getAreaName();
    }

    private String formatVolunteer(MainViewDTO dto) {
        String name = dto.getVolunteerName();
        String firstAid = Columns.formatBoolean("#00aa00", VaadinIcons.DOCTOR_BRIEFCASE, dto.isFirstaid(), false);
        String sia = Columns.formatBoolean("#ffff00", VaadinIcons.EYE, dto.isSia(), false);
        String cellar = Columns.formatBoolean("#444400", VaadinIcons.GLASS, dto.isCellar(), false);
        return name+" "+firstAid+sia+cellar;
    }

    private String formatComment(MainViewDTO dto) {
        return Columns.formatBoolean("#333", VaadinIcons.COMMENT_ELLIPSIS, dto.isHasComment(), false);
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

    private String formatAssignmentsConfirmed(MainViewDTO dto) {
        return dto.isOther() ? Columns.getYes() : Columns.getNo();
    }

    private String formatLocked(MainViewDTO dto) {
        String lockColour = unlockingEnabled ? "#090" : "#900";
        return dto.isLocked() ? Columns.getIconCode(lockColour, VaadinIcons.LOCK) : "";
    }

    protected SortableDataProvider<MainViewDTO, MainView> dataProvider() {
        return dataProvider;
    }

}
