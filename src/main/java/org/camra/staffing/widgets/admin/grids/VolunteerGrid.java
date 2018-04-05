package org.camra.staffing.widgets.admin.grids;

import com.vaadin.data.HasValue;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.provider.SortableDataProvider;
import org.camra.staffing.data.provider.VolunteerDataProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class VolunteerGrid extends MainGrid<VolunteerDTO,Volunteer> {

    @Autowired private VolunteerDataProvider volunteerDataProvider;

    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        setSizeFull();
        setDataProvider(volunteerDataProvider);
        addColumn(this::formatEdit, new HtmlRenderer()).setWidth(50).setId("edit");
        addColumn(this::formatSessions, new HtmlRenderer()).setId("sessions").setWidth(50);
        addColumn(VolunteerDTO::getId, new NumberRenderer()).setCaption("Id");
        addColumn(VolunteerDTO::getForename).setCaption("Forename").setId("forename");
        addColumn(VolunteerDTO::getSurname).setCaption("Surname").setId("surname");
        addColumn(VolunteerDTO::getRole).setCaption("Role").setId("role");
        addColumn(this::formatMembershipStatus, new HtmlRenderer()).setCaption("Membership");
        addColumn(this::formatAssigned, new HtmlRenderer()).setCaption("Assigned").setSortable(false).setWidth(100);
        addColumn(this::formatWorked, new HtmlRenderer()).setCaption("Worked").setSortable(false).setWidth(100);
        addColumn(this::formatFirstAid, new HtmlRenderer()).setCaption("First Aid").setSortable(false);
        addColumn(this::formatSIA, new HtmlRenderer()).setCaption("SIA").setSortable(false);
        addColumn(this::formatCellar, new HtmlRenderer()).setCaption("Cellar").setSortable(false);
        addColumn(this::formatComment).setCaption("Comment").setSortable(false);

        addFilters("surname","role");
    }

    private String formatEdit(VolunteerDTO item) {
        return Columns.getIconCode("#333", VaadinIcons.EDIT);
    }

    private String formatSessions(VolunteerDTO volunteer) {
        return Columns.getIconCode("#333333", VaadinIcons.CALENDAR_CLOCK);
    }

    private String formatAssigned(VolunteerDTO volunteer) {
        return Columns.formatRatio(volunteer.getAssignedSessions(), volunteer.getTotalSessions());
    }

    private String formatWorked(VolunteerDTO volunteer) {
        return Columns.formatRatio(volunteer.getWorked(), volunteer.getAssignedSessions());
    }


    private String formatMembershipStatus(VolunteerDTO volunteer) {
        String color = volunteer.isConfirmed() ? "#009900" : "#990000";
        String membership = volunteer.getMembership();
        String display = membership==null ? volunteer.getManagervouch() : membership;
        return Columns.getIconCode(color, VaadinIcons.CIRCLE) + " " + display;
    }

    private String formatFirstAid(VolunteerDTO volunteer) {
        return Columns.formatBoolean("#00ff00", VaadinIcons.DOCTOR_BRIEFCASE, volunteer.isFirstaid());
    }

    private String formatSIA(VolunteerDTO volunteer) {
        return Columns.formatBoolean("#999900", VaadinIcons.EYE, volunteer.isSia());
    }

    private String formatCellar(VolunteerDTO volunteer) {
        return Columns.formatBoolean("#444400", VaadinIcons.GLASS, volunteer.isCellar());
    }

    private String formatComment(VolunteerDTO volunteer) {
        String comment = volunteer.getComment();
        if (comment==null) return "";
        if (comment.length()>50) {
            return comment.substring(0, 50)+"...";
        } else {
            return comment;
        }
    }

    protected SortableDataProvider<VolunteerDTO, Volunteer> dataProvider() {
        return volunteerDataProvider;
    }

}
