package org.camra.staffing.images;

import com.vaadin.ui.Upload;
import org.camra.staffing.data.service.VolunteerService;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;


public class UploadHandler implements Upload.Receiver, Upload.SucceededListener {

    VolunteerService volunteerService;
    private ByteArrayOutputStream out;
    private int id;

    UploadHandler(int id) {
        this.id = id;
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        out = new ByteArrayOutputStream();
        return out;
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        try {
            JPEGImage jpg = JPEGImage.createImage(out.toByteArray());
            JPEGImage thumbNail = jpg.createThumbnail(150);
            volunteerService.saveVolunteerPicture(id, thumbNail.getPicture());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
