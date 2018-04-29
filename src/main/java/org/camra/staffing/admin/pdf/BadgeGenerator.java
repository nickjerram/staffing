package org.camra.staffing.admin.pdf;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.camra.staffing.data.dto.BadgeDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class BadgeGenerator {

    public static void main(String[] args) throws Exception {
        List<BadgeDTO> bs = new ArrayList<>();
        bs.add(BadgeDTO.create("Lollipop","", "", null));
        bs.add(BadgeDTO.create("Someone", "Else", "", null));
        bs.add(BadgeDTO.create("Person with a", "Ridiculously long name", "", null));
        BadgeGenerator bg = new BadgeGenerator();
        bg.logoResource = new FileSystemResource("/home/nick/Documents/badges/logo.png");
        OutputStream out = new FileOutputStream("/home/nick/Documents/badges/badges.pdf");
        bg.createBadges(bs, out);
        out.close();
    }

    private static float SCALE = 28.3f; //dots per cm

    //Page margins
    private static float LEFT_MARGIN = 0;//SCALE * 1.0f;
    private static float TOP_MARGIN = SCALE * 0.8f;
    private static float RIGHT_MARGIN = 0;//SCALE * 2.4f;

    //spacing between badges
    private static float SPACING_VERTICAL = SCALE * 0.2f;
    private static float SPACING_HORIZONTAL = SCALE * 0.5f; //SCALE * 0.2f;

    //badges dimensions
    private static int COLUMNS = 5;
    private static float COLUMN_WIDTH = SCALE;
    private static float BADGE_WIDTH = COLUMN_WIDTH * COLUMNS;
    private static float LEFT_WIDTH = COLUMN_WIDTH * 3;
    private static float RIGHT_WIDTH = COLUMN_WIDTH * 2;
    private static float TOP_BANNER_HEIGHT = SCALE * 0.8f;
    private static float NAME_BANNER_HEIGHT = SCALE * 0.8f;
    private static float BOTTOM_BANNER_HEIGHT = SCALE * 0.8f;
    private static float BADGE_HEIGHT = SCALE * 4.5f;
    private static float PIC_HEIGHT = BADGE_HEIGHT-TOP_BANNER_HEIGHT-NAME_BANNER_HEIGHT-BOTTOM_BANNER_HEIGHT;
    private static float LOGO_HEIGHT = BADGE_HEIGHT-TOP_BANNER_HEIGHT-BOTTOM_BANNER_HEIGHT;

    private static Font TOP_BANNER_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
    private static Font NAME_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK);
    private static Font BOTTOM_BANNER_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, Color.BLACK);

    private Document document;

    @Value(value = "classpath:logo.png") private Resource logoResource;
    @Value("${staffing.festivalName}") private String festivalName;
    @Value("${staffing.festivalYear}") private String festivalYear;


    public void createBadges(List<BadgeDTO> badges, OutputStream out) throws Exception{

        while (badges.size()<12) {
            badges.add(new BadgeDTO());
        }

        while (badges.size()%12 !=0) {
            badges.add(new BadgeDTO());
        }

        document = new Document();
        document.setMargins(LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, 0);

        PdfWriter writer = PdfWriter.getInstance(document, out);
        Pager pager = new Pager();
        writer.setPageEvent(pager);
        document.open();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);

        boolean right = false;
        for (BadgeDTO badge : badges) {
            table.addCell(getBadgeSpace(badge, right));
            right = !right;
        }

        document.add(table);
        document.close();
    }

    /**
     * Create the table cell in which the badge is placed - this is larger than the actual badge because it also
     * contains the padding around the badge.
     * @param right - if true, the badge is in the right hand column so the padding is on the left.
     * @return
     * @throws Exception
     */
    private PdfPCell getBadgeSpace(BadgeDTO badge, boolean right) throws Exception {
        PdfPCell badgeSpace = new PdfPCell();
        badgeSpace.addElement(createBadge(badge));
        badgeSpace.setFixedHeight(BADGE_HEIGHT + SPACING_VERTICAL);
        badgeSpace.setPaddingTop(0);
        badgeSpace.setPaddingBottom(SPACING_VERTICAL);
        badgeSpace.setPaddingLeft(right ? SPACING_HORIZONTAL/2 : 0);
        badgeSpace.setPaddingRight(right ? 0 : SPACING_HORIZONTAL/2);
        badgeSpace.setBorder(PdfPCell.NO_BORDER);
        return badgeSpace;
    }

    /**
     * Create the actual badge
     * @return
     * @throws IOException
     * @throws MalformedURLException
     * @throws BadElementException
     * @throws Exception
     */
    private PdfPTable createBadge(BadgeDTO badge) throws Exception {
        PdfPTable badgeTable = new PdfPTable(COLUMNS);
        badgeTable.setWidthPercentage(100);
        badgeTable.addCell(getTopBanner());
        //badgeTable.addCell(getNameBanner(badge.getName()));
        badgeTable.addCell(getLeftSide(badge.getName(), badge.getPicture()));
        //badgeTable.addCell(getLeftSide());
        badgeTable.addCell(getLogoCell());
        //badgeTable.addCell(getPictureCell(badge.getPicture()));
        //badgeTable.completeRow();
        if (StringUtils.hasLength(badge.getRole())) {
            badgeTable.addCell(getBottomBanner(badge.getBannerColor(), badge.getRole()));
        } else {
            badgeTable.addCell(getSessionCell());
            badgeTable.addCell(getSessionCell());
            badgeTable.addCell(getSessionCell());
            badgeTable.addCell(getSessionCell());
            badgeTable.addCell(getSessionCell());
        }
        return badgeTable;
    }

    private PdfPCell getTopBanner() {
        String bannerText = festivalName+" "+festivalYear;
        Paragraph bannerParagraph = createText(bannerText.replace("&amp;", "&"), TOP_BANNER_FONT);
        PdfPCell banner = new PdfPCell();
        banner.setFixedHeight(TOP_BANNER_HEIGHT);
        banner.addElement(bannerParagraph);
        banner.setBackgroundColor(Color.BLACK);
        banner.setColspan(COLUMNS);
        //banner.setBorder(0);
        return banner;
    }

    private PdfPCell getLeftSide(String name, byte[] pictureData) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100f);
        table.addCell(getNameBanner(name));
        table.addCell(getPictureCell(pictureData));
        PdfPCell cell = new PdfPCell();
        cell.setColspan(3);
        cell.setPaddingLeft(0);
        cell.addElement(table);
        cell.setBorder(PdfPCell.LEFT);
        return cell;
    }

    private PdfPCell getNameBanner(String name) {
        Paragraph bannerParagraph = createText(LEFT_WIDTH, name, NAME_FONT);
        PdfPCell banner = new PdfPCell();
        banner.setFixedHeight(NAME_BANNER_HEIGHT);
        banner.addElement(bannerParagraph);
        banner.setBorder(0);
        return banner;
    }


    private PdfPCell getLogoCell() throws Exception {
        Image logo = Image.getInstance(logoResource.getURL());
        logo.scaleToFit(RIGHT_WIDTH, LOGO_HEIGHT * 0.9f);
        Paragraph p = new Paragraph();
        p.add(new Chunk(logo,5,0));
        PdfPCell cell = new PdfPCell(p);
        cell.setColspan(2);
        cell.setBorder(0);
        cell.setBorder(PdfPCell.RIGHT);
        return cell;
    }

    private PdfPCell getPictureCell(byte[] pictureData) throws Exception {
        Image picture = pictureData==null ? null : Image.getInstance(pictureData);
        Paragraph p = new Paragraph();
        if (picture!=null) {
            picture.scaleToFit(LEFT_WIDTH*0.9f, PIC_HEIGHT*0.86f);
            Chunk img = new Chunk(picture,30.0f,0f);
            p.add(img);
        }
        PdfPCell cell = new PdfPCell(p);
        cell.setFixedHeight(PIC_HEIGHT * 0.93f);
        cell.setBorder(0);
        return cell;
    }

    private PdfPCell getBottomBanner(Color color, String role) {
        Paragraph bannerParagraph = createText(BADGE_WIDTH, role.toUpperCase(), BOTTOM_BANNER_FONT);
        PdfPCell banner = new PdfPCell();
        banner.setFixedHeight(BOTTOM_BANNER_HEIGHT);
        banner.addElement(bannerParagraph);
        banner.setBackgroundColor(color);
        banner.setColspan(COLUMNS);
        return banner;
    }

    private PdfPCell getSessionCell() {
        PdfPCell cell = new PdfPCell();
        cell.setFixedHeight(BOTTOM_BANNER_HEIGHT);
        return cell;
    }

    private Paragraph createText(float width, String text, Font font) {
        Chunk chunk = new Chunk(text);
        adjustScale(width, chunk);
        Paragraph para = new Paragraph();
        para.setAlignment(Element.ALIGN_CENTER);
        para.setFont(font);
        para.add(chunk);
        return para;
    }

    private Paragraph createText(String text, Font font) {
        Chunk chunk = new Chunk(text);
        Paragraph para = new Paragraph();
        para.setAlignment(Element.ALIGN_CENTER);
        para.setFont(font);
        para.add(chunk);
        return para;
    }

    /**
     * Adjust the size of the chunk of text so it fits in the given space
     * (will distort the font, but that's ok)
     * @param chunk : text chunk to adjust
     */
    private void adjustScale(float width, Chunk chunk) {
        float scale = width/chunk.getWidthPoint();
        //if (scale<1)
        chunk.setHorizontalScaling(scale);
    }

    public static class Pager extends PdfPageEventHelper {
        public Pager() {}
        public void onEndPage(PdfWriter writer, Document document) {}
    }

}
