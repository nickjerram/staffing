package org.camra.staffing.widgets.popup;

import com.vaadin.ui.*;

public class Confirmation extends Window {

    public Confirmation(String message, Runnable onConfirm) {
        super("Confirmation");
        setWidth(500.0f, Unit.PIXELS);
        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout buttons = new HorizontalLayout();
        layout.addComponent(new Label(message));
        layout.addComponent(buttons);
        Button confirm = new Button("OK");
        Button cancel = new Button("Cancel");
        confirm.addClickListener(event -> {onConfirm.run(); close();});
        cancel.addClickListener(event -> close());
        buttons.addComponents(confirm, cancel);
        setContent(layout);
        center();
    }
}
