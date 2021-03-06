package org.camra.staffing.admin.grids;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.colorpicker.Color;

public class Columns {
    public static String getIconCode(String color, VaadinIcons icon) {
        if (icon==null) return "";
        return "<span class=\"v-icon\" style=\"font-family: "
                + icon.getFontFamily() + ";color:" + color + "\">&#x"
                + Integer.toHexString(icon.getCodepoint())
                + ";</span>";

    }

    public static String getYes() {
        return Columns.getIconCode("#0a0", VaadinIcons.CHECK_SQUARE);
    }

    public static String getNo() {
        return Columns.getIconCode("#faa", VaadinIcons.CLOSE_CIRCLE_O);
    }

    public static String getUndefined() {
        return Columns.getIconCode("#777", VaadinIcons.CIRCLE_THIN);
    }

    public static String formatRatio(int top, int bottom) {
        String text = top+" / "+bottom;
        return getIconCode(getColor(top, bottom), VaadinIcons.CIRCLE) + " " + text;
    }

    public static String formatBoolean(String colour, VaadinIcons icon, boolean value) {
        return formatBoolean(colour, icon, value,true);
    }

    public static String formatBoolean(String colour, VaadinIcons icon, boolean value, boolean falseIcon) {
        String useColour = value ? colour : "#aaa";
        VaadinIcons useIcon = value ? icon : (falseIcon ? VaadinIcons.CIRCLE_THIN : null);
        return getIconCode(useColour, useIcon);
    }

    private static String getColor(int top, int bottom){
        if (bottom<=0) return "#ccc";
        if (top>bottom) return "#0ff";
        float ratio = (float) top / (float) bottom;
        int c =  java.awt.Color.HSBtoRGB(ratio/3f, 1f, 1f);
        Color col = new Color(c);
        String red = String.format("%02X", col.getRed());
        String green = String.format("%02X", col.getGreen());
        String blue = String.format("%02X", col.getBlue());
        return "#"+red+green+blue;
    }

}
