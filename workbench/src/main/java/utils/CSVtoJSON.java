package utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class CSVtoJSON {

    final static ObjectMapper mapper = new ObjectMapper();
    public final static class Payload {
        String firstName;
        String lastName;
        String email;
        String tarif;
        String amount;
        String telephone;
        String firstEnrolment;
        String year2021;
        String year2022;
        String year2023;
        String year2024;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTarif() {
            return tarif;
        }

        public void setTarif(String tarif) {
            this.tarif = tarif;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getTelephone() {
            return telephone;
        }

        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }

        public String getFirstEnrolment() {
            return firstEnrolment;
        }

        public void setFirstEnrolment(String firstEnrolment) {
            this.firstEnrolment = firstEnrolment;
        }

        public String getYear2021() {
            return year2021;
        }

        public void setYear2021(String year2021) {
            this.year2021 = year2021;
        }

        public String getYear2022() {
            return year2022;
        }

        public void setYear2022(String year2022) {
            this.year2022 = year2022;
        }

        public String getYear2023() {
            return year2023;
        }

        public void setYear2023(String year2023) {
            this.year2023 = year2023;
        }

        public String getYear2024() {
            return year2024;
        }

        public void setYear2024(String year2024) {
            this.year2024 = year2024;
        }
    }
    // private final static String CSV_FILE = "../temp/passe-coque.adherents.csv";
    // private final static String CSV_FILE = "../temp/passe-coque-adhesions-FINAL.csv";
//    private final static String CSV_FILE = "./temp/members.AGE.2023.csv";
    private final static String CSV_FILE = // "./temp/liste adherents et donateurs Versioin 2.csv";
                                           "./temp/Adherents.et.Donateurs.Version.3.csv";
    public static void main(String... args) {
        System.out.printf("Running from %s\n", System.getProperty("user.dir"));
        File csv = new File(CSV_FILE);
        if (csv.exists()) {
            System.out.println("All good, moving on.");
        } else {
            System.out.printf("File %s not found\n", CSV_FILE);
            System.exit(1);
        }
        try {
            List<Payload> jsonList = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(csv));
            String line = "";
            while (line != null) {
                line = br.readLine();
                if (line != null) {
                    String[] members = line.split("\t");
                    if (true) {
                        if (members.length >= 5) {
                            System.out.printf("Read: %s, %s, %s\n", members[0], members[1], members[5]);
                            Payload payload = new Payload();
                            payload.lastName = members[0];
                            payload.firstName = members.length > 1 ? members[1] : "";
                            payload.tarif = members.length > 2 ? members[2] : "";
                            payload.amount = members.length > 3 ? members[3] : "";
                            payload.telephone = members.length > 4 ? members[4] : "";
                            payload.email = members.length > 5 ? members[5] : "";
                            payload.firstEnrolment = members.length > 6 ? members[6] : "";
                            payload.year2021 = members.length > 7 ? members[7] : "";
                            payload.year2022 = members.length > 8 ? members[8] : "";
                            payload.year2023 = members.length > 9 ? members[9] : "";
                            payload.year2024 = members.length > 10 ? members[10] : "";
                            jsonList.add(payload);
                        }
                    } else { // AG 2023
                        if (members.length >= 3) {
                            System.out.printf("Read: %s, %s, %s\n", members[0], members[1], members[2]);
                            Payload payload = new Payload();
                            payload.firstName = members[1];
                            payload.lastName = members[0];
                            payload.email = members[2];
                            jsonList.add(payload);
                        }
                    }
                }
            }
            br.close();

            FileWriter jsonOutput = new FileWriter("members.json");
            // mapper.writeValue(jsonOutput, jsonList);  // Minified
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonOutput, jsonList); // Beautified

            jsonOutput.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
