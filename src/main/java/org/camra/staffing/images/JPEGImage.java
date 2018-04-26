package org.camra.staffing.images;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class JPEGImage {

    private BufferedImage image;

    private JPEGImage() {}

    public static JPEGImage createImage(byte[] data) throws Exception {
        JPEGImage img = new JPEGImage();
        img.image = ImageIO.read(new ByteArrayInputStream(data));
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

}
