package org.camra.staffing.ui.volunteer.message;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;

@SpringComponent
@UIScope
public class MessageViewLogic extends MessageView {

    public void setMessage(String message) {
        messageText.setValue(message);
    }
}
