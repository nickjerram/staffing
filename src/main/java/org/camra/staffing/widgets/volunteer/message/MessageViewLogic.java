package org.camra.staffing.widgets.volunteer.message;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;

@SpringComponent
@UIScope
public class MessageViewLogic extends MessageView {

    public void setTitle(String title) {
        this.title.setValue(title);
    }

    public void setMessage(String message) {
        messageText.setValue(message);
    }
}
