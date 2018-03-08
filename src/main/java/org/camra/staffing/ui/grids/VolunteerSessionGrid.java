package org.camra.staffing.ui.grids;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.data.dto.VolunteerSessionDTO;

import java.util.Date;

public class VolunteerSessionGrid extends MainGrid<VolunteerSessionDTO> {

    public enum Type {VOLUNTEER, SESSION}

    public VolunteerSessionGrid(Type gridType) {
        switch (gridType) {
            case SESSION: buildForSession(); break;
            case VOLUNTEER: buildForVolunteer();
        }
    }

    private void buildForVolunteer() {
        addColumn(this::formatEditForVolunteer, new HtmlRenderer()).setId("edit");
        addColumn(VolunteerSessionDTO::getSessionName).setCaption("Session");
        addStandardColumns();

    }

    private void buildForSession() {
        addColumn(this::formatEditForSession, new HtmlRenderer());
        addColumn(this::formatName).setCaption("Volunteer");
        addStandardColumns();
    }

    private void addStandardColumns() {
        addColumn(VolunteerSessionDTO::getAreaName).setCaption("Area");
        addColumn(this::formatStartTime).setCaption("Start");
        addColumn(this::formatFinishTime).setCaption("Finish");
        addColumn(this::formatAssigned, new HtmlRenderer()).setCaption("Staff");
        addColumn(this::formatWorked, new HtmlRenderer()).setCaption("Worked");
        addColumn(VolunteerSessionDTO::getTokens).setCaption("Tokens");
        addColumn(VolunteerSessionDTO::getComment).setCaption("Comment");
    }

    private String formatEditForVolunteer(VolunteerSessionDTO assignment) {
        VaadinIcons icon = assignment.isLocked() ? VaadinIcons.LOCK : VaadinIcons.EDIT;
        return Columns.getIconCode("#0c0", icon);
    }

    protected String formatEditForSession(VolunteerSessionDTO assignment) {
        VaadinIcons icon = assignment.isLocked() ? VaadinIcons.LOCK : VaadinIcons.EDIT;
        String colour = assignment.isLocked() ? "#c00" : "#0c0";
        return Columns.getIconCode(colour, icon);
    }

    private String formatName(VolunteerSessionDTO assignment) {
        return assignment.getForename()+" "+assignment.getSurname();
    }

    private String formatStartTime(VolunteerSessionDTO assignment) {
        return assignment.getStart()==null ? "" : formatTime(assignment.getStart());
    }

    private String formatFinishTime(VolunteerSessionDTO assignment) {
        return assignment.getFinish()==null ? "" : formatTime(assignment.getFinish());
    }

    private String formatAssigned(VolunteerSessionDTO assignment) {
        return Columns.formatRatio(assignment.getAssigned(), assignment.getRequired());
    }

    private String formatTime(Date time) {
        return VolunteerSessionDTO.TIME.format(time);
    }

    private String formatWorked(VolunteerSessionDTO assignment) {
        return Columns.formatBoolean("#00aa00", VaadinIcons.CIRCLE, assignment.isWorked());
    }


}
