package org.camra.staffing.admin.popup;

import com.vaadin.ui.*;

public class Confirmation extends Window {

    public Confirmation(String comment) {
        super("Volunteer Notes");
        addStyleName("darkred");
        setWidth(500.0f, Unit.PIXELS);
        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout buttons = new HorizontalLayout();
        Label commentLabel = new Label(comment);
        commentLabel.setWidth(480.0f, Unit.PIXELS);
        layout.addComponent(commentLabel);
        layout.addComponent(buttons);
        Button ok = new Button("OK");
        ok.addClickListener(event -> close());
        buttons.addComponents(ok);
        setContent(layout);
        center();
    }

    public Confirmation(String message, Runnable onConfirm) {
        super("Confirmation");
        addStyleName("darkred");
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