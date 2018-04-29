package org.camra.staffing.admin.pdf;

import org.camra.staffing.admin.access.Manager;
import org.camra.staffing.data.dto.BadgeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/badges")
public class Badges {

    @Autowired private BadgeGenerator badgeGenerator;
    @Autowired private Manager manager;

    @RequestMapping(method= RequestMethod.GET)
    public void getBadges(HttpServletResponse response) throws Exception {
        List<BadgeDTO> badges = manager.getBadges();
        response.setContentType("application/pdf");
        badgeGenerator.createBadges(badges, response.getOutputStream());
    }

}
