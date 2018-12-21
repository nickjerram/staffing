package org.camra.staffing.email;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailFile {

    public static void main(String[] args) throws IOException {

        List<String> volunteers = Files.readAllLines(Paths.get("/home/nick/Documents/volunteers"));
        List<String> emailsRaw = Files.readAllLines(Paths.get("/home/nick/Documents/emails2"));
        Map<String,String> emails = new HashMap<>();
        for (String email : emailsRaw) {
            String address = email.split("==>")[0];
            String message = email.split("==>")[1];
            emails.put(address,message);
        }

        Map<String, List<String>> emailMap = new HashMap<>();

        for (String volunteer : volunteers) {
            String forename = volunteer.split(",")[0];
            String surname = volunteer.split(",")[1];
            List<String> emailList = new ArrayList<>();
            emailMap.put(volunteer, emailList);

            for (String address : emails.keySet()) {
                String message = emails.get(address);
                Boolean addressMatch = address.contains(forename) || address.contains(surname) ||
                        forename.contains(address) || surname.contains(address);
                Boolean nameMatch = message.contains(forename) && message.contains(forename);
                if (nameMatch) {
                    emailList.add(address);
                }
            }
        }

        for (String name : emailMap.keySet()) {
            System.out.println(name);
            for (String email : emailMap.get(name)) {
                System.out.println("  "+email);
            }
            System.out.println("----------------");
        }

    }
}
