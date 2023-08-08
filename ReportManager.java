package org.example;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class ReportManager {
    Connection connection;
    public ReportManager(Connection conn) {
        connection = conn;
    }

    public void reportDisplay() {
        while (true) {
            System.out.println("""
                    Select any of the following options
                    1) Query total bookings in a date range by city
                    2) Query total bookings in a date range by zip code within a city
                    3) Query total listings per country/city/postal_code
                    4) Rank hosts by total listings overall per country/city
                    5) Query hosts with more than 10% of the listings in a city and country
                    6) Rank renters by the number of bookings in a date range
                    7) Rank renters by bookings in a date range per city
                    8) Hosts and renters with the largest number of cancellations
                    9) Get popular nouns from listing
                    10) Exit""");

            Scanner scanner = new Scanner(System.in);
            String option;
            while (!Main.validate(1, 10, option = scanner.nextLine()))
                continue;

            boolean p1, p2, p3;
            Date startDate, endDate;
            String city;
            switch (Integer.parseInt(option)) {
                case 1:
                case 2:
                    System.out.println("Enter start date in the form yyyy-mm-dd");
                    startDate = Main.validateDate(scanner.nextLine());

                    System.out.println("Enter end date in the form yyyy-mm-dd");
                    endDate = Main.validateDate(scanner.nextLine());

                    queryCityByDate(new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()),
                            Integer.parseInt(option) == 2);
                    break;
                case 3:
                    System.out.println("Press y if you want to query by City, anything else otherwise");
                    p1 = Objects.equals(scanner.nextLine(), "y");

                    System.out.println("Press y if you want to query by Country, anything else otherwise");
                    p2 = Objects.equals(scanner.nextLine(), "y");

                    System.out.println("Press y if you want to query by Postal, anything else otherwise");
                    p3 = Objects.equals(scanner.nextLine(), "y");

                    queryLocation(p1, p2, p3);
                    break;
                case 4:
                    System.out.println("Press y if you want to query hosts by City, anything else otherwise");
                    p1 = Objects.equals(scanner.nextLine(), "y");

                    System.out.println("Press y if you want to query hosts by Country, anything else otherwise");
                    p2 = Objects.equals(scanner.nextLine(), "y");

                    queryHost(p1, p2);
                    break;
                case 5:
                    queryCommercial();
                    break;
                case 6:
                case 7:
                    System.out.println("Enter start date in the form yyyy-mm-dd");
                    startDate = Main.validateDate(scanner.nextLine());

                    System.out.println("Enter end date in the form yyyy-mm-dd");
                    endDate = Main.validateDate(scanner.nextLine());

                    queryBookings(new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()),
                            Integer.parseInt(option) == 7);
                    break;
                case 8:
                    queryMostCancellations();
                    break;
                case 9:
                    queryNounPhrases();
                    break;
                case 10:
                    return;
            }
        }
    }

    public void queryCityByDate(Date start, Date end, boolean zip) {
        String query  = "SELECT city";
        if (zip) {
            query += ", postal_code";
        }

        query += ", COUNT(lID) as count FROM Book NATURAL JOIN Listing NATURAL JOIN Location " +
                "WHERE date >= ? AND date <= ? ";

        if (zip)
            query += "GROUP BY city, postal_code;";
        else
            query += "GROUP BY city;";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDate(1, new java.sql.Date(start.getTime()));
            pstmt.setDate(2, new java.sql.Date(end.getTime()));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String city = rs.getString("city");
                int count = rs.getInt("count");
                String output = count + " bookings in " + city;
                if (zip)
                    output += " with zip code " + rs.getString("postal_code");
                System.out.println(output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryLocation(boolean city, boolean country, boolean pc) {
        String selection = "";
        if (city) selection += "City, ";
        if (country) selection += "Country, ";
        if (pc) selection += "Postal_code, ";
        String groupBy = "GROUP BY ";
        if (city) groupBy += "City, ";
        if (country) groupBy += "Country, ";
        if (pc) groupBy += "Postal_code, ";
        String query = "SELECT " + selection + " COUNT(lID) AS count FROM Listing NATURAL JOIN Location "
                + groupBy.substring(0, groupBy.length() - 2) + ";";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String output = "";
                if (city) output += rs.getString("city") + ", ";
                if (country) output += rs.getString("country") + ", ";
                if (pc) output += rs.getString("postal_code") + " ";
                output += "Listing Count: " + rs.getString("count");
                System.out.println(output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryHost(boolean city, boolean country) {
        String selection = "";
        if (city) selection += "City, ";
        if (country) selection += "Country, ";
        String groupBy = "GROUP BY ";
        if (city) groupBy += "City, ";
        if (country) groupBy += "Country, ";
        String query = "SELECT UserID, " + selection + "COUNT(lID) AS count FROM Location NATURAL JOIN Listing " +
                "NATURAL JOIN Host " + groupBy + "UserID ORDER BY " + selection + "COUNT(lID) DESC;";

        String currentLocation = "";
        int hostRank = 1;
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String location = "";

                if (city) location += rs.getString("city") + " ";
                if (country) location += rs.getString("country") + " ";

                if (!currentLocation.equals(location)) {
                    System.out.println("\n" + location);
                    System.out.println("------------------------------------------------------------------");
                    currentLocation = location;
                    hostRank = 1;
                }

                String output = hostRank + ". Host:";
                output += rs.getString("UserID") + " ";

                output += location + "Listing Count: " + rs.getString("count");
                System.out.println(output);
                hostRank++;
            }
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryCommercial() {
        String query = "SELECT UserID, City, Country, COUNT(lID) AS count FROM Host NATURAL JOIN Listing NATURAL JOIN Location " +
                       "GROUP BY UserID, City, Country " +
                       "HAVING COUNT(lID) > (SELECT 0.1 * COUNT(lID) FROM Host NATURAL JOIN Listing NATURAL JOIN Location AS l " +
                       "WHERE l.city = Location.city AND l.country = Location.country);";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String output = "UserID: ";
                output += rs.getString("UserID") + " ";
                output += "with listing Count " + rs.getString("count");
                output += ", in " + rs.getString("city") + ", ";
                output += rs.getString("country");
                System.out.println(output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryBookings(Date start, Date end, boolean byCity) {
        String order = "ORDER BY ";
        if (byCity)
            order += "City, ";
        String query = "SELECT UserID," + (byCity ? " city, " : " ") + "COUNT(lID) as count FROM Book NATURAL JOIN Listing NATURAL JOIN Location " +
                "WHERE date >= ? AND date <= ? " +
                "GROUP BY UserID" + (byCity ? ", city " : " ") +
                (byCity ? "HAVING COUNT(lID) >= 2 " : "") +
                order + "COUNT(lID) DESC;";

        String currentLocation = "";
        int userRank = 1;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDate(1, new java.sql.Date(start.getTime()));
            pstmt.setDate(2, new java.sql.Date(end.getTime()));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                if (byCity && !currentLocation.equals(rs.getString("city"))) {
                    System.out.println("\n" + rs.getString("city"));
                    System.out.println("------------------------------------------------------------------");
                    currentLocation = rs.getString("city");
                    userRank = 1;
                }

                String output = userRank + ". UserID:";
                output += rs.getString("UserID") + " ";
                output += "with Booking Count " + rs.getString("count");

                System.out.println(output);
                userRank++;
            }
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryMostCancellations() {
        String query = "SELECT UserID, COUNT(userID) AS total FROM cancelled_bookings " +
                "GROUP BY UserID;";

        String host_query = "SELECT UserID, COUNT(lID) AS total FROM cancelled_listings " +
                "GROUP BY UserID;";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                String output = "UserID: ";
                output += rs.getString("UserID") + " ";
                output += "with " + rs.getInt("total") + " booking cancellations";
                System.out.println(output);
            }

            rs = stmt.executeQuery(host_query);

            if (rs.next()) {
                String output = "Host: ";
                output += rs.getString("UserID") + " ";
                output += "with " + rs.getInt("total") + " listing cancellations";
                System.out.println(output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void queryNounPhrases() {
        // Gets all lID and orders them so the same lID will be continuous
        String query = "SELECT * FROM Feedback_Listing ORDER BY lID;";
        int currentlID = -1;
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            List<Map.Entry<String, Integer>> frequency = new ArrayList<>();

            while (rs.next()) {
                int lID = rs.getInt("lID");
                String comment = rs.getString("comment");
                String[] words = comment.split(" ");
                Map<String, Integer> nouns = new HashMap<>();

                List<String> preceding = List.of(
                        "the", "a", "between", "few", "an", "in", "that", "her", "these", "above",
                        "his", "beside", "all", "my", "on", "two", "their", "every",
                        "far", "several", "under", "that", "one", "your", "the", "its", "near",
                        "another", "this", "many", "each", "between", "our",
                        "some", "beside", "far", "below", "above", "any", "with", "was"
                );

                for (int i = 1; i < words.length; i++) {
                    if (preceding.contains(words[i - 1])) {
                        nouns.putIfAbsent(words[i], 0);
                        nouns.put(words[i], nouns.get(words[i]) + 1);
                    }
                }

                if (currentlID != lID) {
                    // https://www.digitalocean.com/community/tutorials/sort-hashmap-by-value-java
                    frequency = new ArrayList<>(nouns.entrySet());
                    frequency.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

                    if (currentlID != -1) {
                        System.out.print("Some popular noun phrases are: ");
                        for (int i = 0; i < Math.min(10, frequency.size()); i++) {
                            System.out.print(frequency.get(i) + " ");
                        }
                        System.out.println("\n");
                    }

                    currentlID = lID;
                    System.out.println("LID: " + lID + "\n------------------------------------");
                }
            }

            if (currentlID != -1) {
                System.out.print("Some popular noun phrases are: ");
                for (int i = 0; i < Math.min(10, frequency.size()); i++) {
                    System.out.print(frequency.get(i) + " ");
                }
                System.out.println("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
