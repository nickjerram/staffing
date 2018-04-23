package org.camra.staffing.admin.ui;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeConverter implements Converter<String, Date> {

    private DateFormat format = new SimpleDateFormat("HH:mm");

    @Override
    public Result<Date> convertToModel(String s, ValueContext valueContext) {
        Date date;
        if (StringUtils.hasText(s)) {
            try {
                date = format.parse(s);
                return Result.ok(date);
            } catch (Exception e) {
                return Result.error("Invalid format");
            }
        } else {
            return Result.ok(null);
        }
    }

    @Override
    public String convertToPresentation(Date date, ValueContext valueContext) {
        return date==null ? "" : format.format(date);
    }

}
