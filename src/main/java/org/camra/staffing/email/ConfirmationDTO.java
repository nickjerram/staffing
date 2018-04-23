package org.camra.staffing.email;

import lombok.Getter;
import lombok.Setter;
import org.camra.staffing.data.dto.VolunteerSessionDTO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ConfirmationDTO {

    private static DateFormat DAY = new SimpleDateFormat("EEEEE, dd MMM");
    private static DateFormat TIME = new SimpleDateFormat("HH:mm");
    @Getter @Setter private String volunteerName;
    @Getter private PeriodList periodList = new PeriodList();

    public void setAssignments(List<VolunteerSessionDTO> volunteerAssignments) {
        for (VolunteerSessionDTO assignment : volunteerAssignments) {
            Date start = Optional.ofNullable(assignment.getStart()).orElse(assignment.getSessionStart());
            Date finish = Optional.ofNullable(assignment.getFinish()).orElse(assignment.getSessionFinish());

            periodList.addPeriod(start, finish);
        }
    }

    private class PeriodList {

        @Getter private List<Period> periods = new ArrayList<>();
        private Date lastFinish;
        private Period lastPeriod;

        private void addPeriod(Date start, Date finish) {
            System.out.println("adding session "+start+" "+finish);
            if (lastFinish==null || start.after(lastFinish)) {
                lastPeriod = new Period(start, finish);
                periods.add(lastPeriod);
                lastFinish = lastPeriod.finish;
            } else if (start.before(lastFinish) || start.equals(lastFinish)) {
                lastFinish = finish;
                lastPeriod.finish =lastFinish;
            }
        }
    }

    public class Period {
        Period(Date start, Date finish) {
            this.start = start;
            this.finish = finish;
        }
        private Date start;
        private Date finish;

        public String getTimes() {
            System.out.println(DAY.format(start)+" "+TIME.format(start)+"-"+TIME.format(finish));
            return DAY.format(start)+" "+TIME.format(start)+"-"+TIME.format(finish);
        }
    }
}