package org.camra.staffing.ui.admin.grids;

import com.vaadin.ui.Grid;

import java.util.function.Consumer;

public abstract class MainGrid<T> extends Grid<T> {

    private Consumer<T> editHandler;
    private Consumer<T> detailHandler;

    public MainGrid() {
        setSizeFull();
        addItemClickListener(this::itemClick);
    }

    public void setEditHandler(Consumer<T> consumer) {
        this.editHandler = consumer;
    }

    public void setDetailViewHandler(Consumer<T> consumer) {
        this.detailHandler = consumer;
    }

    private void itemClick(Grid.ItemClick<T> event) {
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
