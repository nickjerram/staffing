package org.camra.staffing.ui.grids;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.camra.staffing.data.dto.AreaSelectorDTO;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.entity.Preference;
import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@UIScope
@SpringComponent
public class AreaSelectorGrid extends Grid<AreaSelectorDTO> {

    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        addColumn(AreaSelectorDTO::getAreaName).setCaption("Area").setWidth(220);
        addColumn(this::formatYes, new HtmlRenderer()).setCaption("Yes").setWidth(75).setId(Preference.Yes.name());
        addColumn(this::formatDontMind, new HtmlRenderer()).setCaption("Maybe").setWidth(75).setId(Preference.DontMind.name());
        addColumn(this::formatNo, new HtmlRenderer()).setCaption("No").setWidth(75).setId(Preference.No.name());
        setSelectionMode(SelectionMode.NONE);
    }

    private String formatYes(AreaSelectorDTO dto) {
        return Columns.getIconCode("#0a0",dto.getPreference()==Preference.Yes ? VaadinIcons.CHECK_SQUARE : null);
    }

    private String formatDontMind(AreaSelectorDTO dto) {
        return Columns.getIconCode("#0a0",dto.getPreference()==Preference.DontMind ? VaadinIcons.CHECK_SQUARE : null);
    }

    private String formatNo(AreaSelectorDTO dto) {
        return Columns.getIconCode("#0a0",dto.getPreference()==Preference.No ? VaadinIcons.CHECK_SQUARE : null);
    }


}
