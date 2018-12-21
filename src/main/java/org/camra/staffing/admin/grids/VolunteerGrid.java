package org.camra.staffing.admin.grids;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import org.camra.staffing.admin.access.Manager;
import org.camra.staffing.admin.popup.Confirmation;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.Volunteer;
import org.camra.staffing.data.provider.SortableDataProvider;
import org.camra.staffing.data.provider.VolunteerDataProvider;
import org.camra.staffing.data.service.VolunteerService;
import org.camra.staffing.email.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@SpringComponent
@UIScope
public class VolunteerGrid extends AbstractGrid<VolunteerDTO,Volunteer> {

    @Autowired private VolunteerDataProvider volunteerDataProvider;
    @Autowired private EmailSender emailSender;
    @Autowired private Manager manager;
    @Autowired private VolunteerService volunteerService;
    private boolean confirmationsEnabled = false;

    @PostConstruct
    private void init() {
        setSizeFull();
        setDataProvider(volunteerDataProvider);

        if (manager.isSuperUser()) {
            addColumn(this::formatDelete, new HtmlRenderer()).setWidth(50).setId("del");
            addColumn(this::formatEdit, new HtmlRenderer()).setWidth(50).setId("edit");
            addColumn(this::formatConfirm, new HtmlRenderer()).setWidth(50).setId("confirm");
        }
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
        if (manager.isSuperUser()) {
            addColumn(this::formatComment, new HtmlRenderer()).setCaption("Comment").setId("comment").setSortable(false);
        }

        addStringFilters("surname","role");

        addItemClickListener(this::volunteerClick);

    }

    public void enableConfirmations(boolean enabled) {
        confirmationsEnabled = enabled;
        volunteerDataProvider.refreshAll();
    }

    private void volunteerClick(ItemClick<VolunteerDTO> event) {
        if (event.getColumn().getId()==null) return;
        if (event.getColumn().getId().equals("edit")) {
            if (editHandler!=null) {
                editHandler.accept(event.getItem());
            }
        } else if (event.getColumn().getId().equals("sessions")) {
            if (detailHandler!=null) {
                detailHandler.accept(event.getItem());
            }
        } else if (event.getColumn().getId().equals("del")) {
            if (deleteHandler!=null) {
                deleteHandler.accept(event.getItem());
            }
        } else if (confirmationsEnabled && event.getColumn().getId().equals("confirm") && event.getItem().isAssigned()) {
            //if (emailSender.sendConfirmation(event.getItem())) {
            //   volunteerService.setConfirmed(event.getItem());
            //    volunteerDataProvider.refreshAll();
            //}
        } else if (event.getColumn().getId().equals("comment")) {
            if (StringUtils.hasText(event.getItem().getComment())) {
                UI.getCurrent().addWindow(new Confirmation(event.getItem().getComment()));
            }
        }
    }

    private String formatDelete(VolunteerDTO item) {
        return Columns.getIconCode("#333", VaadinIcons.TRASH);
    }

    private String formatEdit(VolunteerDTO item) {
        return Columns.getIconCode("#333", VaadinIcons.EDIT);
    }

    private String formatConfirm(VolunteerDTO item) {
        String enabledColour = item.isAssigned() ? "#900" : "#ccc";
        return item.isConfirmed() ?
            Columns.getIconCode("#090", VaadinIcons.CHECK_SQUARE) :
            Columns.getIconCode((confirmationsEnabled ? enabledColour : "#ccc"), VaadinIcons.ENVELOPE);
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
        return Columns.formatBoolean("#00aa00", VaadinIcons.DOCTOR_BRIEFCASE, volunteer.isFirstaid());
    }

    private String formatSIA(VolunteerDTO volunteer) {
        return Columns.formatBoolean("#ffff00", VaadinIcons.EYE, volunteer.isSia());
    }

    private String formatCellar(VolunteerDTO volunteer) {
        return Columns.formatBoolean("#444400", VaadinIcons.GLASS, volunteer.isCellar());
    }

    private String formatComment(VolunteerDTO volunteer) {
        String comment = volunteer.getComment();
        String commentIcon = Columns.formatBoolean("#333", VaadinIcons.COMMENT_ELLIPSIS, StringUtils.hasText(comment), false);
        if (comment==null) return "";
        if (comment.length()>50) {
            return commentIcon+" "+comment.substring(0, 50)+"...";
        } else {
            return commentIcon+" "+comment;
        }
    }

    protected SortableDataProvider<VolunteerDTO, Volunteer> dataProvider() {
        return volunteerDataProvider;
    }

}
