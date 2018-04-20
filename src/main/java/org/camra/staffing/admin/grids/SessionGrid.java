package org.camra.staffing.admin.grids;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.data.dto.SessionDTO;
import org.camra.staffing.data.dto.SessionSelectorDTO;
import org.camra.staffing.data.entity.Session;
import org.camra.staffing.data.provider.SessionDataProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@SpringComponent
@UIScope
public class SessionGrid extends AbstractGrid<SessionSelectorDTO, Session> {

    @Autowired private SessionDataProvider dataProvider;

    @PostConstruct
    private void init() {
        setSizeFull();
        setSelectionMode(SelectionMode.NONE);
        setDataProvider(dataProvider);
        addColumn(this::formatVolunteers, new HtmlRenderer()).setWidth(50).setId("volunteers");
        addColumn(SessionSelectorDTO::getSessionName).setCaption("Session");
        addColumn(SessionSelectorDTO::getStartTime).setCaption("Start");
        addColumn(SessionSelectorDTO::getFinishTime).setCaption("Finish");
        addColumn(this::formatAssigned, new HtmlRenderer()).setCaption("Assigned");
        setStyleGenerator(this::rowStyle);
        addItemClickListener(this::sessionClick);
    }

    private void sessionClick(ItemClick<SessionSelectorDTO> event) {
        if ("volunteers".equals(event.getColumn().getId())) {
            if (detailHandler!=null) {
                detailHandler.accept(event.getItem());
            }
        }
    }

    private String formatVolunteers(SessionSelectorDTO session) {
        return Columns.getIconCode(session.isNight() ? "#ff9": "#333", VaadinIcons.GROUP);
    }

    private String formatAssigned(SessionSelectorDTO session) {
        return Columns.formatRatio(session.getTotalAssigned(), session.getTotalRequired());
    }

    private String rowStyle(SessionSelectorDTO session) {
        return session.isNight() ? "night" :"";
    }

}
