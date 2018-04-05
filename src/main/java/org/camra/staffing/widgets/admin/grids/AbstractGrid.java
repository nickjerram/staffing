package org.camra.staffing.widgets.admin.grids;

import com.vaadin.data.HasValue;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;
import org.camra.staffing.data.provider.SortableDataProvider;

import java.util.StringJoiner;
import java.util.function.Consumer;

public abstract class AbstractGrid<DTO,E> extends Grid<DTO> {

    private Consumer<DTO> editHandler;
    private Consumer<DTO> detailHandler;
    private HeaderRow filterRow;

    private HeaderRow getFilterRow() {
        if (filterRow==null) {
            filterRow = appendHeaderRow();
        }
        return filterRow;
    }

    public AbstractGrid() {
        setSizeFull();
        addItemClickListener(this::itemClick);
    }

    public void setEditHandler(Consumer<DTO> consumer) {
        this.editHandler = consumer;
    }

    public void setDetailViewHandler(Consumer<DTO> consumer) {
        this.detailHandler = consumer;
    }

    protected void addStringFilters(String... columns) {
        HeaderRow filterRow = getFilterRow();
        for (String column : columns) {
            HeaderCell headerCell = filterRow.getCell(column);
            TextField filterField = new TextField();
            headerCell.setComponent(filterField);
            filterField.setId(column);
            filterField.addValueChangeListener(this::doStringFilter);
        }
    }

    protected void addRatioFilter(String column, String topProperty, String bottomProperty, String ratioProperty) {
        HeaderRow filterRow = getFilterRow();
        HeaderCell headerCell = filterRow.getCell(column);
        TextField filterField = new TextField();
        headerCell.setComponent(filterField);
        String filterId = column+":"+topProperty+":"+bottomProperty+":"+ratioProperty;
        filterField.setId(filterId);
        filterField.addValueChangeListener(this::doRatioFilter);
    }

    protected void addBooleanFilter(String column) {
        HeaderRow filterRow = getFilterRow();
        HeaderCell headerCell = filterRow.getCell(column);
        Button button = new Button("x");
        headerCell.setComponent(button);
        button.addClickListener(this::doBooleanFilter);
    }

    private void doBooleanFilter(Button.ClickEvent clickEvent) {
        String state = clickEvent.getButton().getCaption();
        if (state.equals("x")) {
            clickEvent.getButton().setCaption("true");
        } else if (state.equals("true")) {
            clickEvent.getButton().setCaption("false");
        } else if (state.equals("false")) {
            clickEvent.getButton().setCaption("x");
        }
    }

    protected SortableDataProvider<DTO,E> dataProvider() {
        return null;
    }

    private void doStringFilter(HasValue.ValueChangeEvent<String> event) {
        String column = event.getComponent().getId();
        dataProvider().addStringFilter(column, event.getValue());
    }

    private void doRatioFilter(HasValue.ValueChangeEvent<String> event) {
        String filterId = event.getComponent().getId();
        String[] filterIdParts = filterId.split(":");
        String column = filterIdParts[0];
        String topProperty = filterIdParts[1];
        String bottomProperty = filterIdParts[2];
        String ratioProperty = filterIdParts[3];
        dataProvider().addRatioFilter(column, topProperty, bottomProperty, ratioProperty, event.getValue());
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
