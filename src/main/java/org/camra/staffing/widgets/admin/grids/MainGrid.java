package org.camra.staffing.widgets.admin.grids;

import com.vaadin.data.HasValue;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import org.camra.staffing.data.provider.SortableDataProvider;

import java.util.function.Consumer;

public abstract class MainGrid<DTO,E> extends Grid<DTO> {

    private Consumer<DTO> editHandler;
    private Consumer<DTO> detailHandler;

    public MainGrid() {
        setSizeFull();
        addItemClickListener(this::itemClick);
    }

    public void setEditHandler(Consumer<DTO> consumer) {
        this.editHandler = consumer;
    }

    public void setDetailViewHandler(Consumer<DTO> consumer) {
        this.detailHandler = consumer;
    }

    protected void addFilters(String... columns) {
        HeaderRow filterRow = appendHeaderRow();
        for (String column : columns) {
            HeaderCell headerCell = filterRow.getCell(column);
            TextField filterField = new TextField();
            headerCell.setComponent(filterField);
            filterField.setId(column);
            filterField.addValueChangeListener(this::doFilter);
        }
    }

    protected SortableDataProvider<DTO,E> dataProvider() {
        return null;
    }

    private void doFilter(HasValue.ValueChangeEvent<String> event) {
        String column = event.getComponent().getId();
        dataProvider().addFilter(column, event.getValue());
    }

    private void itemClick(Grid.ItemClick<DTO> event) {
        if (event.getColumn().getId()==null) return;
        if (event.getColumn().getId().equals("edit")) {
            if (editHandler!=null) {
                editHandler.accept(event.getItem());
            }
        } else if (event.getColumn().getId().equals("sessions")) {
            if (detailHandler!=null) {
                detailHandler.accept(event.getItem());
            }
        }
    }

}
