package org.camra.staffing.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

@Component
public class CaptchaImageGenerator {

    public static final String KEY = "captcha";

    private List<Font> defaultFonts;

    /**
     * The height image in pixels.
     */
    private int height = 50;

    /**
     * Create an image which have written a distorted text, text given
     * as parameter. The result image is put on the output stream
     *
     * @param stream the OutputStrea where the image is written
     * @param text the distorted characters written on image
     * @throws IOException if an error occurs during the image written on
     * output stream.
     */
    public void createImage(String text, OutputStream stream)
            throws Exception {

        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] availableFonts = e.getAllFonts();
        defaultFonts = new ArrayList<Font>();
        Random r = new Random();
        for (Font f : availableFonts) {
            if (f.getFontName().startsWith("Century") || f.getFontName().startsWith("DejaVu")
                    || f.getFontName().startsWith("Lucida")) {
                defaultFonts.add(new Font(f.getFontName(), Font.BOLD, r.nextInt(20)+30));
            }
        }


        int w = 25+(text.length()*25);

        //put the text on the image
        BufferedImage bi =  renderWord (text, w,height);

        //create a new distorted (wound version of) the image
        //bi = getDistortedImage( bi );

        //add a background to the image
        bi = addBackground(bi);

        ImageIO.write(bi, "jpeg", stream);
    }

    /**
     * Render a word to a BufferedImage.
     *
     * @param word The word to be rendered.
     * @param width The width of the image to be created.
     * @param height The heigth of the image to be created.
     * @return The BufferedImage created from the word,
     */
    private BufferedImage renderWord (String word, int width, int height) {

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2D = image.createGraphics();
        g2D.setColor(Color.black);

        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));

        g2D.setRenderingHints(hints);

        Random generator = new Random();

        char[] wc =word.toCharArray();
        Color fontColor = Color.black;
        g2D.setColor(fontColor);
        FontRenderContext frc = g2D.getFontRenderContext();
        int startPosX = 25;

        for (int i = 0;i<wc.length;i++) {
            char[] itchar = new char[]{wc[i]};
            int choiceFont = generator.nextInt(Math.max(10, defaultFonts.size())) ;
            Font itFont = defaultFonts.get(choiceFont);
            g2D.setFont(itFont);
            GlyphVector gv = itFont.createGlyphVector(frc, itchar);
            double charWitdth = gv.getVisualBounds().getWidth();

            g2D.drawChars(itchar,0,itchar.length,startPosX ,35);
            startPosX = startPosX+(int)charWitdth+2;
        }

        return image;
    }

    private BufferedImage addBackground(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        Color from = new Color(0x33,0xaf,0x33);
        Color to = new Color(0x85,0x33,0x33);

        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D graph = (Graphics2D)resultImage.getGraphics();
        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);

        hints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
        hints.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));

        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));

        graph.setRenderingHints(hints);

        //create the gradient color
        GradientPaint ytow = new GradientPaint(0, 0, from, width, height, to);
        graph.setPaint(ytow);
        //draw gradient color
        graph.fill(new Rectangle2D.Double(0, 0, width, height));

        //draw the transparent image over the background
        graph.drawImage(image, 0, 0, null);

        return resultImage;
    }
}