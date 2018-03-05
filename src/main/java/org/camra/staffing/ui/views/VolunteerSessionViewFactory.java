package org.camra.staffing.ui.views;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.dto.VolunteerSessionDTO;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.ui.grids.Columns;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@UIScope
@SpringComponent
public class VolunteerSessionViewFactory {

    @Autowired private VolunteerService volunteerService;

    public VolunteerSessionView createViewForVolunteer(VolunteerDTO volunteer) {
        Grid<VolunteerSessionDTO> grid = new Grid<>();
        grid.addColumn(this::formatEditForVolunteer);
        grid.addColumn(VolunteerSessionDTO::getSessionName).setCaption("Session");
        grid.setItems(volunteerService.getSessions(volunteer.getId()));
        addStandardColumns(grid);
        return new VolunteerSessionView(volunteer, grid);
    }

    public VolunteerSessionView createViewForSession(SessionDTO session) {
        Grid<VolunteerSessionDTO> grid = new Grid<>();
        grid.addColumn(this::formatEditForSession);
        grid.addColumn(this::formatName).setCaption("Volunteer");
        addStandardColumns(grid);
        return new VolunteerSessionView(session, grid);
    }

    private void addStandardColumns(Grid<VolunteerSessionDTO> grid) {
        grid.addColumn(VolunteerSessionDTO::getAreaName).setCaption("Area");
        grid.addColumn(this::formatStartTime).setCaption("Start");
        grid.addColumn(this::formatFinishTime).setCaption("Finish");
        grid.addColumn(this::formatAssigned, new HtmlRenderer()).setCaption("Staff");
        grid.addColumn(this::formatWorked, new HtmlRenderer()).setCaption("Worked");
        grid.addColumn(VolunteerSessionDTO::getTokens).setCaption("Tokens");
        grid.addColumn(VolunteerSessionDTO::getComment).setCaption("Comment");
    }

    private String formatStartTime(VolunteerSessionDTO assignment) {
        return assignment.getStart()==null ? "" : formatTime(assignment.getStart());
    }

    private String formatFinishTime(VolunteerSessionDTO assignment) {
        return assignment.getFinish()==null ? "" : formatTime(assignment.getFinish());
    }

    private String formatAssigned(VolunteerSessionDTO assignment) {
        return Columns.formatRatio(assignment.getStaffAssigned(), assignment.getStaffRequired());
    }

    private String formatName(VolunteerSessionDTO assignment) {
        return assignment.getForename()+" "+assignment.getSurname();
    }

    private String formatTime(Date time) {
        return VolunteerSessionDTO.TIME.format(time);
    }

    private String formatWorked(VolunteerSessionDTO assignment) {
        return Columns.formatBoolean("#00aa00", VaadinIcons.CIRCLE, assignment.isWorked());
    }

    protected String formatEditForVolunteer(VolunteerSessionDTO assignment) {
        VaadinIcons icon = assignment.isLock() ? VaadinIcons.LOCK : VaadinIcons.EDIT;
        return Columns.getIconCode("#0c0", icon);
    }

    protected String formatEditForSession(VolunteerSessionDTO assignment) {
        VaadinIcons icon = assignment.isLock() ? VaadinIcons.LOCK : VaadinIcons.EDIT;
        String colour = assignment.isLock() ? "#c00" : "#0c0";
        return Columns.getIconCode(colour, icon);
    }

    protected void editClickForVolunteer(VolunteerSessionDTO assignment) {
        //do it
    }

    protected void editClickForSession(VolunteerSessionDTO assignment) {
        if (!assignment.isLock()) {
            //do it
        }
    }


}
