package org.camra.staffing.images;

import org.camra.staffing.data.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UploaderHandlerFactory {

    @Autowired private VolunteerService volunteerService;

    public UploadHandler getUploader(int volunteerId) {
        UploadHandler uploadHandler = new UploadHandler(volunteerId);
        uploadHandler.volunteerService = volunteerService;
        return uploadHandler;
    }

}
