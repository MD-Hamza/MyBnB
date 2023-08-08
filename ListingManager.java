package org.example;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class ListingManager {
    Connection connection;
    String[] amenities;
    CommentManager commentManager;
    public ListingManager(Connection conn) {
        connection = conn;
        amenities = new String[] {
                "Bedding",
                "Towels",
                "Soap",
                "Toilet paper",
                "Kitchen facilities",
                "Wi-Fi",
                "Heating and air conditioning",
                "TV and entertainment",
                "Washer and dryer",
                "Parking",
                "First aid kit",
                "Check-in process",
                "Outdoor spaces",
                "Family-friendly amenities",
                "Pet-friendly options",
                "Pool",
                "Gym",
                "Bicycles",
                "Breakfast",
                "Snacks and beverages",
                "Toiletries",
                "Beach gear",
                "Workspace",
                "Games and entertainment",
                "Extra bedding",
                "Barbecue grill",
                "Sports equipment",
                "Fireplace",
                "Local guides and maps",
                "Yoga mats",
                "Travel adapters",
                "Accessibility features"
        };
        commentManager = new CommentManager(conn);
    }

    public void listingDisplay() {
        if (Main.login.userID == null) {
            System.out.println("You must login to use this feature");
            return;
        }

        while (true) {
            System.out.println("""
                    Welcome to the Host Toolkit, here you can host your listings and receive guiding steps from the toolkit
                    1) Create Listing
                    2) Modify Listing
                    3) Cancel Listing
                    4) Comment on Renter
                    5) Exit""");

            Scanner scanner = new Scanner(System.in);

            String option, input;
            ArrayList<Integer> lIDs;

            do {
                option = scanner.nextLine();
            } while (!Main.validate(1, 5, option));

            switch (Integer.parseInt(option)) {
                case 1:
                    int lID = createListing();
                    String[] types = {"apartment", "full house", "room"};
                    System.out.println("You have just created your listing with id " + lID + " to complete you listing enter " +
                            "the rest of the information");

                    String type;
                    System.out.println("""
                            Enter Type of the Listing
                            1) Apartment
                            2) Full House
                            3) Room""");
                    do {
                        type = scanner.nextLine();
                    } while (!Main.validate(1, 3, type));

                    addType(lID, types[Integer.parseInt(type) - 1]);
                    System.out.println("""
                            Great Choice, now we need the address
                            """);
                    String latitude, longitude, pc, city, country;

                    System.out.println("Enter longitude");
                    do {
                        longitude = scanner.nextLine();
                    } while (!Main.validateDouble(-180, 180, longitude));

                    System.out.println("Enter latitude");
                    do {
                        latitude = scanner.nextLine();
                    } while (!Main.validateDouble(-90, 90, latitude));

                    System.out.println("Enter the Postal Code");
                    pc = scanner.nextLine();

                    System.out.println("Enter the City");
                    city = scanner.nextLine();

                    System.out.println("Enter the Country");
                    country = scanner.nextLine();

                    if (!insertLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), pc, city, country))
                        break;

                    addLatitude(lID, Double.parseDouble(latitude))
                            .addLongitude(lID, Double.parseDouble(longitude));

                    System.out.println("After analyzing the location these are the amenities " +
                            "that you should add followed by the approximate revenue increase after adding them");

                    System.out.println("Here are the Amenities to pick from");
                    recommendAmenities(Double.parseDouble(longitude), Double.parseDouble(latitude));

                    System.out.println("Enter anything to see the expected revenue increase for each amenity");
                    scanner.nextLine();

                    for (int i = 0; i < amenities.length; i++) {
                        System.out.println((i + 1) + ". " + amenities[i] + " " + getEstimatedRevenueIncrease(amenities[i]));
                    }

                    System.out.println("The top 4 amenities on this list are essentials and highly recommended to add " +
                            "press y to add them");

                    ArrayList<String> chosen = new ArrayList<>();

                    if (Objects.equals(scanner.nextLine(), "y"))
                        chosen.addAll(List.of("Bedding", "Towels", "Soap", "Toilet Paper"));

                    chosen.addAll(pickAmenities());
                    Set<String> uniqueAmenities = new HashSet<>(chosen);

                    for (String i : uniqueAmenities)
                        addAmenity(lID, i);

                    System.out.println("Now add some dates you want to host your listing");
                    System.out.println("A good pricing from your area seems to be ");
                    recommendPricing(Double.parseDouble(longitude), Double.parseDouble(latitude));

                    while (true) {
                        System.out.println("Press y if you want to add a date and price");
                        if (!Objects.equals(scanner.nextLine(), "y"))
                            break;

                        System.out.println("Enter Date in form YYYY-MM-DD");
                        Date date = Main.validateDate(scanner.nextLine());

                        if (date.before(new Date())) {
                            System.out.println("Availability has to be in the future");
                            continue;
                        }

                        System.out.println("Enter the price for this day");
                        while (!Main.validateDouble(1, 999999999, input = scanner.nextLine()))
                            continue;

                        addAvailability(lID, date, Double.parseDouble(input));
                    }

                    attachHost(Main.login.userID, lID);
                    break;
                case 2:
                    lIDs = displayUserListings(Main.login.userID);
                    System.out.println("Select the lID you wish to alter");

                    while (!Main.validate(0, 999999999, input = scanner.nextLine()))
                        continue;

                    lID = Integer.parseInt(input);

                    if (!lIDs.contains(lID)) {
                        System.out.println("You may not alter this lID");
                        break;
                    }

                    System.out.println("""
                            What would you like to modify
                            1) Add Availability
                            2) Remove Availability
                            3) Update Price
                            4) Exit""");

                    while (!Main.validate(1, 4, option = scanner.nextLine()))
                        continue;

                    Date availabilityDate;
                    switch (Integer.parseInt(option)) {
                        case 1:
                            printAvailability(lID);
                            System.out.println("Enter Date in form yyyy-mm-dd to add another availability");
                            availabilityDate = Main.validateDate(scanner.nextLine());

                            if (availabilityDate.before(new Date())) {
                                System.out.println("Availability has to be in the future");
                                continue;
                            }

                            System.out.println("Add price for this availability");
                            while (!Main.validateDouble(1, 999999999, input = scanner.nextLine()))
                                continue;

                            addAvailability(lID, availabilityDate, Double.parseDouble(input));
                            break;
                        case 2:
                            printAvailability(lID);
                            System.out.println("Enter Date in form yyyy-mm-dd to remove an availability");
                            availabilityDate = Main.validateDate(scanner.nextLine());

                            removeAvailability(lID, availabilityDate);
                            break;
                        case 3:
                            printAvailability(lID);
                            System.out.println("Enter Date in form yyyy-mm-dd to choose which day's price to modify");
                            availabilityDate = Main.validateDate(scanner.nextLine());

                            System.out.println("Add price new for this availability");
                            while (!Main.validateDouble(1, 999999999, input = scanner.nextLine()))
                                continue;

                            updatePrice(lID, new java.sql.Date(availabilityDate.getTime()), Double.parseDouble(input));
                            break;
                        case 4:
                            break;
                    }
                    break;
                case 3:
                    lIDs = displayUserListings(Main.login.userID);
                    System.out.println("Select the lID you wish to cancel");

                    while (!Main.validate(0, 999999999, input = scanner.nextLine()))
                        continue;

                    lID = Integer.parseInt(input);
                    if (!lIDs.contains(lID)) {
                        System.out.println("You can't cancel this lID");
                        break;
                    }

                    cancelListing(lID);
                    break;
                case 4:
                    if (Main.login.userID == null) {
                        System.out.println("You need to login first to use this feature");
                        break;
                    }
                    System.out.println("Here are all the previous renters");
                    ArrayList<String> renters = getPreviousRenters(Main.login.userID);

                    System.out.println("Enter the UserID you wish to comment on");
                    String userID = scanner.nextLine();

                    if (!renters.contains(userID)) {
                        System.out.println("UserID not found");
                        break;
                    }

                    System.out.println("Enter the comment");
                    String comment = scanner.nextLine();

                    System.out.println("Enter the rating out of 5");
                    while (!Main.validate(0, 5, input = scanner.nextLine())) {
                        continue;
                    }

                    commentManager.addUserComment(userID, comment, Integer.parseInt(input), Main.login.userID);
                    break;
                case 5:
                    return;
            }
        }
    }

    boolean execute(String query) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(query);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    ListingManager addType(int lID, String type) {
        String query = "UPDATE Listing SET type = '" + type + "' WHERE lID = " + lID + ";";
        execute(query);
        return this;
    }

    ListingManager addLatitude(int lID, double latitude) {
        String query = "UPDATE Listing SET latitude = '" + latitude + "' WHERE lID = " + lID + ";";
        execute(query);
        return this;
    }

    ListingManager addAmenity(int lID, String name) {
        String query = "INSERT INTO Amenities VALUES ( '" + name + "',  " + lID + ");";
        execute(query);
        return this;
    }

    ListingManager addLongitude(int lID, double longitude) {
        String query = "UPDATE Listing SET longitude = '" + longitude + "' WHERE lID = " + lID + ";";
        execute(query);
        return this;
    }

    int createListing() {
        int lID = (int) (Math.random() * 100000);
        String query = "SELECT * FROM Listing WHERE lID = " + lID + ";";
        while (true)
        {
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next())
                    lID = (int) (Math.random() * 100000);
                else
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        query = "INSERT INTO Listing (lID) values ("+ lID +");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lID;
    }

    public boolean insertLocation(double latitude, double longitude, String postalCode, String city, String country) {
        String query = "INSERT INTO Location VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, longitude);
            pstmt.setDouble(2, latitude);
            pstmt.setString(3, postalCode);
            pstmt.setString(4, city);
            pstmt.setString(5, country);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Location doesn't match database");
            return false;
        }
    }

    ListingManager attachHost(String userID, int lID) {
        String query = "INSERT INTO Host VALUES (" + lID + ", '" + userID + "');";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    ListingManager updatePrice(int lID, java.sql.Date date, double price) {
        if (isBooked(lID, date)) {
            System.out.println("Listing already booked you may not change the price");
            return this;
        }

        String query = "UPDATE Availability SET price = '" + price + "' WHERE lID = "
                        + lID + " AND date = DATE '" + date + "';";
        execute(query);
        return this;
    }

    ListingManager addAvailability(int lID, Date date, double price) {
        String checkBooked = "SELECT * FROM Book WHERE lid = ? AND date = ?;";
        String query = "INSERT INTO Availability VALUES (?, ?, ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(checkBooked);
            pstmt.setInt(1, lID);
            pstmt.setDate(2, new java.sql.Date(date.getTime()));
            if (pstmt.executeQuery().next()) {
                System.out.println("Booking is booked, hence price cant be updated");
                return this;
            }

            pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, lID);
            pstmt.setDate(2, new java.sql.Date(date.getTime()));
            pstmt.setDouble(3, price);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return this;
    }

    ListingManager removeAvailability(int lID, Date date) {
        String checkBooked = "SELECT * FROM Book WHERE lid = " + lID + " AND date = " + date + ";";
        String query = "DELETE FROM Availability WHERE lid = " + lID + " AND date = " + date + ";";
        try (Statement stmt = connection.createStatement()) {
            if (stmt.executeQuery(checkBooked).next()) {
                System.out.println("Booking is booked, hence cant be removed");
                return this;
            }
            stmt.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    public ArrayList<Date> printAvailability(int lID) {
        ArrayList<Date> dates = new ArrayList<>();
        try {
            String query = "SELECT date, price FROM Availability WHERE lID = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, lID);

            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("Here are the availabilities for lID: " + lID);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            while (resultSet.next()) {
                java.sql.Date date = resultSet.getDate("date");
                double price = resultSet.getDouble("price");
                dates.add(date);
                System.out.println("Date: " + dateFormat.format(date) + ", Price: " + price);
            }

            System.out.println("");
            resultSet.close();
            preparedStatement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dates;
    }

    public ArrayList<Integer> displayUserListings(String userID) {
        String query = "SELECT * FROM Host NATURAL JOIN Listing NATURAL JOIN Location WHERE UserID = ?";
        ArrayList<Integer> lIDs = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userID);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int lID = rs.getInt("lID");
                String type = rs.getString("type");
                double longitude = rs.getDouble("longitude");
                double latitude = rs.getDouble("latitude");
                String postal_code = rs.getString("postal_code");
                lIDs.add(lID);

                System.out.println("Listing ID: " + lID);
                System.out.println("Type: " + type);
                System.out.println("Longitude: " + longitude);
                System.out.println("Latitude: " + latitude);
                System.out.println("Postal Code: " + postal_code);
                System.out.println("-------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lIDs;
    }

    public boolean isBooked(int lID, java.sql.Date date) {
        String query = "SELECT * FROM Book WHERE lID = ? AND date = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, lID);
            pstmt.setDate(2, date);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void cancelListing(int lID) {
        try {
            String query = "INSERT INTO cancelled_listings VALUES (?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, lID);
            pstmt.setString(2, Main.login.userID);
            pstmt.executeUpdate();

            query = "DELETE FROM Host WHERE lID = ?";
            pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, lID);
            pstmt.executeUpdate();

            query = "DELETE FROM Book WHERE lID = ?";
            pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, lID);
            pstmt.executeUpdate();

            System.out.println("Listing with lID " + lID + " has been cancelled and removed from any bookings");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getPreviousRenters(String hostID) {
        ArrayList<String> output = new ArrayList<>();
        String query = "SELECT * FROM " +
                "Book INNER JOIN Host ON Book.lID = Host.lID " +
                "WHERE Host.userID = ? AND date < ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, hostID);
            pstmt.setDate(2, new java.sql.Date(new Date().getTime()));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String userID = rs.getString("Book.userID");

                System.out.println("UserID: " + userID + " booked listing " + rs.getString("lID"));
                output.add(userID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return output;
    }

    public ArrayList<String> pickAmenities() {
        ArrayList<String> output = new ArrayList<>();
        System.out.println("Here are the Amenities to pick from:");

        for (int i = 0; i < amenities.length; i++) {
            System.out.println((i + 1) + ". " + amenities[i]);
        }

        while (true) {
            System.out.print("Enter the number of the amenity you want: ");

            String amenityNumber;
            do {
                amenityNumber = Main.scanner.nextLine();
            } while (!Main.validate(1, amenities.length, amenityNumber));

            if (output.contains(amenities[Integer.parseInt(amenityNumber) - 1]))
                System.out.println("You already have this amenity");
            else
                output.add(amenities[Integer.parseInt(amenityNumber) - 1]);

            System.out.println("Press y to continue choosing");
            if (!Objects.equals(Main.scanner.nextLine(), "y"))
                return output;
        }
    }

    public double getEstimatedRevenueIncrease(String amenity) {
        String haveAmenity = "SELECT AVG(price) as average FROM " +
                "Amenities NATURAL JOIN Book " +
                "WHERE Amenities.name = ?";

        String notHaveAmenity = "SELECT AVG(price) as average FROM " +
                "Book " +
                "WHERE lID NOT IN (SELECT lID FROM Amenities WHERE name = ?)";

        double haveAvg = 0;
        double notHaveAvg = 0;

        try {
            PreparedStatement pstmt = connection.prepareStatement(haveAmenity);
            pstmt.setString(1, amenity);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                haveAvg = rs.getDouble("average");
            }

            pstmt = connection.prepareStatement(notHaveAmenity);
            pstmt.setString(1, amenity);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                notHaveAvg = rs.getDouble("average");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Math.max(0, haveAvg - notHaveAvg);
    }

    public void recommendAmenities(double longitude, double latitude) {
        String distanceQuery = "SQRT(POW(longitude - ?, 2) + POW(latitude - ?, 2))";
        String query = "SELECT DISTINCT name FROM Listing NATURAL JOIN Book NATURAL JOIN Amenities " +
                "WHERE " + distanceQuery + " < 20;";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, longitude);
            pstmt.setDouble(2, latitude);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    System.out.println(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void recommendPricing(double longitude, double latitude) {
        String distanceQuery = "SQRT(POW(longitude - ?, 2) + POW(latitude - ?, 2))";
        String query = "SELECT AVG(price) FROM Listing NATURAL JOIN Book " +
                "WHERE " + distanceQuery + " < 20;";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, longitude);
            pstmt.setDouble(2, latitude);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double price = rs.getDouble("AVG(price)");
                    price = price == 0 ? 180 : price;
                    System.out.print("$" + price);
                } else {
                    System.out.print("$1000");
                }
            }
            System.out.println("\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
