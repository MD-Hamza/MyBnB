package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.Date;

public class BookingManager {
    Connection connection;
    SearchManager searchManager;
    CommentManager commentManager;
    PaymentManager paymentManager;
    ListingManager listingManager;
    public BookingManager(Connection conn) {
        connection = conn;
        searchManager = new SearchManager(conn);
        commentManager = new CommentManager(conn);
        paymentManager = new PaymentManager(conn);
        listingManager = new ListingManager(conn);
    }

    public void bookingDisplay() {
        while (true) {
            System.out.println("""
                    Select any of the following options
                    1) Search by Exact Address
                    2) Search by Coordinates
                    3) Search by Postal Code
                    4) Search by Date Range
                    5) Apply Filters
                    6) Book
                    7) Cancel Booking
                    8) Comment
                    9) Update/Add payment information
                    10) Exit""");

            Scanner scanner = new Scanner(System.in);

            String option, input;
            do {
                option = scanner.nextLine();
            } while (!Main.validate(1, 10, option));

            String longitude, latitude, postalCode, country, city;
            Date startDate, endDate;
            int lID;

            switch (Integer.parseInt(option)) {
                case 1:
                    System.out.println("Enter longitude");
                    do {
                        longitude = scanner.nextLine();
                    } while (!Main.validateDouble(-180, 180, longitude));

                    System.out.println("Enter latitude");
                    do {
                        latitude = scanner.nextLine();
                    } while (!Main.validateDouble(-90, 90, latitude));

                    System.out.println("Enter postal code");
                    do {
                        postalCode = scanner.nextLine();
                    } while (postalCode.length() == 0);

                    System.out.println("Enter country");
                    do {
                        country = scanner.nextLine();
                    } while (country.length() == 0);

                    System.out.println("Enter city");
                    do {
                        city = scanner.nextLine();
                    } while (city.length() == 0);

                    searchManager.getExact(Double.parseDouble(longitude), Double.parseDouble(latitude), postalCode, city, country);
                    break;
                case 2:
                    String distance;
                    System.out.println("Enter longitude");
                    do {
                        longitude = scanner.nextLine();
                    } while (!Main.validateDouble(-180, 180, longitude));

                    System.out.println("Enter latitude");
                    do {
                        latitude = scanner.nextLine();
                    } while (!Main.validateDouble(-90, 90, latitude));

                    System.out.println("Enter threshold distance where the listings will be within");
                    do {
                        distance = scanner.nextLine();
                    } while (!Main.validate(0, 999999999, distance));
                    searchManager.getNearest(Double.parseDouble(longitude), Double.parseDouble(latitude), Double.parseDouble(distance));
                    break;
                case 3:
                    System.out.println("Enter postal code");
                    do {
                        postalCode = scanner.nextLine();
                    } while (postalCode.length() == 0);

                    searchManager.getAdjacentPostal(postalCode);
                    break;
                case 4:
                    System.out.println("Enter start date in the form yyyy-mm-dd");
                    startDate = Main.validateDate(scanner.nextLine());

                    System.out.println("Enter end date in the form yyyy-mm-dd");
                    endDate = Main.validateDate(scanner.nextLine());

                    searchManager.getFromDate(new java.sql.Date(startDate.getTime()), new java.sql.Date(endDate.getTime()));
                    break;
                case 5:
                    boolean choosing = true;

                    ArrayList<String> amenitiesList = new ArrayList<>();
                    postalCode = null;
                    startDate = null;
                    endDate = null;
                    Double low = null;
                    Double high = null;

                    while (choosing) {
                        System.out.println("""
                            Select the filter category and once ready press 5 for results
                            1. Postal Code
                            2) Date Range
                            3) Price Range
                            4) Amenities
                            5) Apply Filters""");

                        option = scanner.nextLine();
                        switch (Integer.parseInt(option)) {
                            case 1:
                                System.out.println("Enter postal code:");
                                postalCode = scanner.nextLine();
                                break;
                            case 2:
                                System.out.println("Enter start date in the form yyyy-mm-dd");
                                startDate = Main.validateDate(scanner.nextLine());

                                System.out.println("Enter end date in the form yyyy-mm-dd");
                                endDate = Main.validateDate(scanner.nextLine());

                                break;
                            case 3:
                                System.out.println("Enter low price");
                                while (!Main.validateDouble(0, 999999999, input = scanner.nextLine())) {
                                    continue;
                                }
                                low = Double.parseDouble(input);

                                System.out.println("Enter high price:");
                                while (!Main.validateDouble(0, 999999999, input = scanner.nextLine())) {
                                    continue;
                                }
                                high = Double.parseDouble(input);
                                break;
                            case 4:
                                amenitiesList = listingManager.pickAmenities();
                                break;
                            case 5:
                                choosing = false;
                                break;
                            default:
                                System.out.println("Invalid option! Please try again.");
                                break;
                        }
                    }

                    searchManager.applyFilter(postalCode, amenitiesList, startDate, endDate, low, high);
                    break;
                case 6:
                    if (Main.login.userID == null) {
                        System.out.println("You need to login first to use this feature");
                        break;
                    }

                    System.out.println("Enter the lID of the listing you want to book, try using the search feature " +
                            "if you haven't to find listings");
                    while (!Main.validate(1, 999999999, input = scanner.nextLine())) {
                        continue;
                    }

                    lID = Integer.parseInt(input);
                    double total = 0;
                    if (isAvailable(lID)) {
                        ArrayList<Date> availabilities = listingManager.printAvailability(lID);
                        ArrayList<Date> dates = new ArrayList<>();
                        do {
                            System.out.println("Enter date you want to book in the form (yyyy-mm-dd)");
                            Date date = Main.validateDate(scanner.nextLine());
                            if (availabilities.contains(new java.sql.Date(date.getTime()))) {
                                dates.add(date);
                                total += getPrice(lID, date);
                            } else
                                System.out.println("Invalid Date");

                            System.out.println("Press y to add a date");
                        } while (Objects.equals(scanner.nextLine(), "y"));

                        System.out.println("In total that is $" + total);
                        paymentManager.displayPayment(Main.login.userID);
                        book(Main.login.userID, lID, dates);
                    } else
                        System.out.println("Listing not available try using the search features");

                    break;
                case 7:
                    if (Main.login.userID == null) {
                        System.out.println("You need to login first to use this feature");
                        break;
                    }
                    ArrayList<Integer> lIDs = getCurrentBookings(Main.login.userID);
                    if (lIDs.isEmpty()) {
                        System.out.println("No bookings found");
                        break;
                    }

                    System.out.println("Enter the lID of the booking you want to cancel");
                    while (!Main.validate(1, 999999999, input = scanner.nextLine())) {
                        continue;
                    }

                    lID = Integer.parseInt(input);
                    if (!lIDs.contains(lID)) {
                        System.out.println("You have not booked a booking with the requested ID");
                        break;
                    }
                    cancel_booking(lID);
                    System.out.println("Successfully cancelled");
                    break;
                case 8:
                    if (Main.login.userID == null) {
                        System.out.println("You need to login first to use this feature");
                        break;
                    }
                    System.out.println("Here are all the previous bookings");
                    ArrayList<Integer> previous = getPreviousBookings(Main.login.userID);

                    System.out.println("Enter the lID of the listing you wish to comment on");
                    while (!Main.validate(1, 999999999, input = scanner.nextLine())) {
                        continue;
                    }

                    lID = Integer.parseInt(input);

                    if (!previous.contains(lID)) {
                        System.out.println("You haven't booked this lID");
                        break;
                    }

                    System.out.println("Enter the comment");
                    String comment = scanner.nextLine();

                    System.out.println("Enter the rating out of 5");
                    while (!Main.validate(0, 5, input = scanner.nextLine())) {
                        continue;
                    }

                    commentManager.addListingComment(lID, comment, Integer.parseInt(input), Main.login.userID);
                    break;
                case 9:
                    if (Main.login.userID == null) {
                        System.out.println("You need to login first to use this feature");
                        break;
                    }

                    paymentManager.receiveCardInformation();
                    break;
                case 10:
                    return;
            }
        }
    }

    public boolean book(String userID, int lID, ArrayList<Date> dates) {

        for (Date date : dates) {
            String query = "INSERT INTO Book VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, lID);
                pstmt.setString(2, userID);
                pstmt.setDate(3, new java.sql.Date(date.getTime()));
                pstmt.setDouble(4, getPrice(lID, new java.sql.Date(date.getTime())));
                pstmt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        StringBuilder remove = new StringBuilder("DELETE FROM Availability WHERE lid = ? AND date IN (");
        for (int i = 0; i < dates.size(); i++) {
            remove.append("?");
            if (i < dates.size() - 1) {
                remove.append(", ");
            }
        }
        remove.append(")");

        try (PreparedStatement pstmt = connection.prepareStatement(remove.toString())) {
            pstmt.setInt(1, lID);
            for (int i = 0; i < dates.size(); i++) {
                pstmt.setDate(i + 2, new java.sql.Date(dates.get(i).getTime()));
            }
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean cancel_booking(int lID) {
        String getBooking = "SELECT * FROM Book WHERE lID = ? AND userID = ?";
        String removeBooking = "DELETE FROM Book WHERE lID = ? AND userID = ?";
        String query = "INSERT INTO cancelled_bookings VALUES (?, ?, ?)";
        String availability = "INSERT INTO availability VALUES (?, ?, ?)";

        java.sql.Date booked;
        double price;

        try {
            PreparedStatement pstmt = connection.prepareStatement(getBooking);
            pstmt.setInt(1, lID);
            pstmt.setString(2, Main.login.userID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                booked = rs.getDate("Date");
                price = rs.getDouble("Price");

                pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, lID);
                pstmt.setString(2, Main.login.userID);
                pstmt.setDate(3, booked);
                pstmt.executeUpdate();

                pstmt = connection.prepareStatement(availability);
                pstmt.setInt(1, lID);
                pstmt.setDate(2, booked);
                pstmt.setDouble(3, price);
                pstmt.executeUpdate();

                pstmt = connection.prepareStatement(removeBooking);
                pstmt.setInt(1, lID);
                pstmt.setString(2, Main.login.userID);
                pstmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Integer> getPreviousBookings(String userID) {
        ArrayList<Integer> output = new ArrayList<>();
        String query = "SELECT * FROM Listing NATURAL JOIN Location NATURAL JOIN Book WHERE userID = ? AND date < ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userID);
            pstmt.setDate(2, new java.sql.Date(new Date().getTime()));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int lID = rs.getInt("lID");
                String type = rs.getString("type");
                double longitude = rs.getDouble("longitude");
                double latitude = rs.getDouble("latitude");
                String postalCode = rs.getString("postal_code");
                String city = rs.getString("city");
                String country = rs.getString("country");
                Date dateBooked = rs.getDate("date");

                System.out.println("Listing ID: " + lID + ", Type: " + type + ", Longitude: " + longitude +
                        ", Latitude: " + latitude + ", Postal Code: " + postalCode + ", City: " + city +
                        ", Country: " + country + ", Date booked: " + dateBooked);
                output.add(lID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return output;
    }

    public ArrayList<Integer> getCurrentBookings(String userID) {
        String query = "SELECT * FROM Listing NATURAL JOIN Location NATURAL JOIN Book WHERE userID = ? AND date > ?";
        ArrayList<Integer> output = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userID);
            pstmt.setDate(2, new java.sql.Date(new Date().getTime()));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int lID = rs.getInt("lID");
                output.add(lID);
                String type = rs.getString("type");
                double longitude = rs.getDouble("longitude");
                double latitude = rs.getDouble("latitude");
                String postalCode = rs.getString("postal_code");
                String city = rs.getString("city");
                String country = rs.getString("country");
                Date dateBooked = rs.getDate("date");

                System.out.println("Listing ID: " + lID + ", Type: " + type + ", Longitude: " + longitude +
                        ", Latitude: " + latitude + ", Postal Code: " + postalCode + ", City: " + city +
                        ", Country: " + country + ", Date booked: " + dateBooked);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return output;
    }

    public boolean isAvailable(int lID) {
        String query = "SELECT * FROM Listing NATURAL JOIN Host WHERE lID = ? AND userID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, lID);
            pstmt.setString(2, Main.login.userID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Cant book your own Listing");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        query = "SELECT * FROM Availability WHERE lID = ? AND Date > ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, lID);
            pstmt.setDate(2, new java.sql.Date(new Date().getTime()));
            ResultSet rs = pstmt.executeQuery();

            return (rs.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getPrice(int lID, Date date) {
        String query = "SELECT * FROM Availability WHERE lID = ? AND Date = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, lID);
            pstmt.setDate(2, new java.sql.Date(date.getTime()));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                return rs.getDouble("price");
            else
                return 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
