package org.camra.staffing.admin.popup;

import com.vaadin.ui.*;

import java.util.function.Consumer;

public class Editor extends Window {

    private Consumer<String> valueConsumer;
    private TextField textField;

    public Editor(String title, String value, Consumer<String> valueConsumer) {
        super("Volunteer Notes");
        addStyleName("darkred");
        this.valueConsumer = valueConsumer;
        setWidth(500.0f, Unit.PIXELS);
        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout buttons = new HorizontalLayout();
        Label commentLabel = new Label(title);
        commentLabel.setWidth(480.0f, Unit.PIXELS);
        textField = new TextField();
        textField.setValue(value==null ? "" : value);
        layout.addComponent(commentLabel);
        layout.addComponent(textField);
        layout.addComponent(buttons);
        Button ok = new Button("OK");
        Button cancel = new Button("Cancel");
        ok.addClickListener(this::okClicked);
        cancel.addClickListener(event -> close());
        buttons.addComponents(ok);
        buttons.addComponent(cancel);
        setContent(layout);
        center();
    }

    private void okClicked(Button.ClickEvent clickEvent) {
        valueConsumer.accept(textField.getValue());
        close();
    }


}
