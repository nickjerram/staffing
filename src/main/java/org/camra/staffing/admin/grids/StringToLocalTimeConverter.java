package org.camra.staffing.admin.grids;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StringToLocalTimeConverter implements Converter<String, LocalTime> {

    private DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public Result<LocalTime> convertToModel(String s, ValueContext valueContext) {
        try {
            if (StringUtils.isEmpty(s)) return Result.ok(null);
            return Result.ok(LocalTime.parse(s));
        } catch (Exception e) {
            return Result.error("Invalid format");
        }
    }

    @Override
    public String convertToPresentation(LocalTime localTime, ValueContext valueContext) {
        return localTime==null ? "" : HHMM.format(localTime);
    }
}
