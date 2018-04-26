package org.camra.staffing.admin.grids;

import com.vaadin.data.HasValue;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.components.grid.HeaderRow;
import lombok.AllArgsConstructor;
import org.camra.staffing.data.dto.VolunteerDTO;
import org.camra.staffing.data.provider.SortableDataProvider;
import org.camra.staffing.data.specification.BooleanCriterion;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractGrid<DTO,E> extends Grid<DTO> {

    private HeaderRow filterRow;
    private Map<String, RatioSpecification> ratios = new HashMap<>();
    Consumer<DTO> editHandler;
    Consumer<DTO> detailHandler;
    Consumer<DTO> deleteHandler;

    public void setEditHandler(Consumer<DTO> consumer) {
        this.editHandler = consumer;
    }

    public void setDetailViewHandler(Consumer<DTO> consumer) {
        this.detailHandler = consumer;
    }

    public void setDeleteHandler(Consumer<DTO> consumer) {
        this.deleteHandler = consumer;
    }


    class StatefulButton extends Button {
        private BooleanCriterion.State state = BooleanCriterion.State.X;
        private String columnId;

        StatefulButton(String columnId) {
            setCaptionAsHtml(true);
            setCaption(Columns.getUndefined());
            addClickListener(this::nextState);
            this.columnId = columnId;
        }

        BooleanCriterion.State nextState(ClickEvent clickEvent) {
            BooleanCriterion.State nextState;
            switch (state) {
                case YES:
                    nextState = BooleanCriterion.State.NO;
                    setCaption(Columns.getNo());
                    break;
                case NO:
                    nextState = BooleanCriterion.State.X;
                    setCaption(Columns.getUndefined());
                    break;
                default:
                    nextState = BooleanCriterion.State.YES;
                    setCaption(Columns.getYes());
            }
            state = nextState;
            return state;
        }
    }

    private HeaderRow getFilterRow() {
        if (filterRow==null) {
            filterRow = appendHeaderRow();
        }
        return filterRow;
    }

    void addStringFilters(String... columns) {
        HeaderRow filterRow = getFilterRow();
        for (String column : columns) {
            String fieldId = column.contains(".") ? column.split("\\.")[0] : column;
            HeaderCell headerCell = filterRow.getCell(column);
            TextField filterField = new TextField();
            headerCell.setComponent(filterField);
            filterField.setId(fieldId);
            filterField.setWidth("90%");
            filterField.addValueChangeListener(this::doStringFilter);
        }
    }

    void addRatioFilter(String column, String topProperty, String bottomProperty, String ratioProperty) {
        RatioSpecification rs = new RatioSpecification(column, topProperty, bottomProperty, ratioProperty);
        //ratios.put(column, rs);
        HeaderRow filterRow = getFilterRow();
        HeaderCell headerCell = filterRow.getCell(column);
        TextField filterField = new TextField();
        headerCell.setComponent(filterField);
        filterField.setId(column);
        filterField.setWidth("90%");
        filterField.addValueChangeListener(rs::doRatioFilter);
    }

    void addBooleanFilter(String column) {
        HeaderRow filterRow = getFilterRow();
        HeaderCell headerCell = filterRow.getCell(column);
        StatefulButton button = new StatefulButton(column);
        headerCell.setComponent(button);
        button.addClickListener(this::doBooleanFilter);
    }


    protected SortableDataProvider<DTO,E> dataProvider() {
        return null;
    }

    private void doStringFilter(HasValue.ValueChangeEvent<String> event) {
        String column = event.getComponent().getId();
        String fieldId = column.contains(".") ? column.split("\\.")[0] : column;
        dataProvider().addStringFilter(fieldId, event.getValue());
    }

    private void doBooleanFilter(Button.ClickEvent event) {
        StatefulButton button = (StatefulButton) event.getButton();
        String filterId = button.columnId;
        dataProvider().addBooleanFilter(filterId, button.state);
    }

    @AllArgsConstructor
    private class RatioSpecification {

        private String column;
        private String topProperty;
        private String bottomProperty;
        private String ratioProperty;

        private void doRatioFilter(HasValue.ValueChangeEvent<String> event) {
            dataProvider().addRatioFilter(column, topProperty, bottomProperty, ratioProperty, event.getValue());
        }
    }

}
