package org.camra.staffing.images;

import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class JPEGImage {

    private BufferedImage image;
    private ByteArrayOutputStream imageBuffer = null;
    private JPEGImage() {}

    public static JPEGImage createImage(byte[] data) {
        JPEGImage img = new JPEGImage();
        try {
            img.image = ImageIO.read(new ByteArrayInputStream(data));
        } catch (Exception e) {
            img.image = null;
        }
        return img;
    }

    public void send(OutputStream stream) throws Exception {
        ImageIO.write(image, "jpeg", stream);
    }

    public static JPEGImage createImage(InputStream in) throws Exception {
        JPEGImage img = new JPEGImage();
        img.image = ImageIO.read(in);
        return img;
    }

    public int getWidth() {return image.getWidth();}
    public int getHeight() {return image.getHeight();}

    public JPEGImage createThumbnail(int size) throws Exception {
        int newWidth;
        int newHeight;
        if (getWidth()>getHeight()) {
            newWidth = size;
            newHeight = size*getHeight()/getWidth();
        } else {
            newHeight = size;
            newWidth = size*getWidth()/getHeight();
        }
        BufferedImage scaledImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D gScaledImg = scaledImg.createGraphics();
        gScaledImg.drawImage(image, 0, 0, newWidth, newHeight, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(scaledImg, "jpeg", baos);
        byte[] bytes = baos.toByteArray();

        JPEGImage jpg = JPEGImage.createImage(bytes);
        return jpg;
    }

    public byte[] getPicture() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Resource getResource() {
        if (image==null) {
            return new ThemeResource("img/nopic.jpg");
        } else {
            return new StreamResource(this::getStream, "picture.jpg");
        }
    }

    public InputStream getStream() {
        try {
            imageBuffer = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", imageBuffer);
            return new ByteArrayInputStream(imageBuffer.toByteArray());
        } catch (IOException e) {
            return null;
        }
    }
}
