package org.example;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class LoginManager {
    Connection connection;
    String userID;

    public LoginManager(Connection conn) {
        connection = conn;
    }

    boolean registerPerson(int SIN, String name, String address, int age, Date DOB, String occupation) {
        String query = "INSERT INTO Person VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, SIN);
            pstmt.setString(2, name);
            pstmt.setInt(3, age);
            pstmt.setString(4, address);
            pstmt.setDate(5, DOB);
            pstmt.setString(6, occupation);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Something went wrong could not register");
        }
        return false;
    }
    boolean registerUser(String userID, String password, int SIN) {
        String query = "INSERT INTO User (UserID, password, SIN) VALUES (?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userID);
            pstmt.setString(2, password);
            pstmt.setInt(3, SIN);
            pstmt.executeUpdate();
            System.out.println("Successfully registered");
            return true;
        } catch (SQLException e) {
            System.out.println("Something went wrong could not register user");
        }
        return false;
    }

    boolean isTaken(String userID) {
        String query = "SELECT UserID FROM User WHERE UserID = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("User ID Has Been Taken!");
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean signIn(String userID, String password) {
        String query = "SELECT UserID FROM User WHERE UserID = ? AND password = ?;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userID);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.userID = userID;
                return true;
            } else {
                System.out.println("Invalid Username or Password");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void loginDisplay() {
        System.out.println("1. Login to account\n2. Delete account");
        String option;
        do {
            option = Main.scanner.nextLine();
        } while (!Main.validate(1, 2, option));

        System.out.println("Enter UserID");
        String id = Main.scanner.nextLine();
        System.out.println("Enter Password");
        String password = Main.scanner.nextLine();
        if (signIn(id, password)) {
            if (Integer.parseInt(option) == 1)
                System.out.println("Successfully Signed In");
            else
                deleteAccount(id);
        }
    }

    public void registerDisplay() {
        String id;
        do {
            System.out.println("Enter UserID");
            id = Main.scanner.nextLine();
        } while (id.length() == 0 || !isTaken(id));

        System.out.println("Enter Password");
        String password = Main.scanner.nextLine();

        String DOB, occupation, SIN, name, address;

        System.out.println("Enter Name");
        name = Main.scanner.nextLine();

        System.out.println("Enter Address");
        address = Main.scanner.nextLine();

        System.out.println("Enter Date of Birth in form YYYY-MM-DD");
        java.util.Date date;

        while (true) {
            DOB = Main.scanner.nextLine();
            try {
                date = new SimpleDateFormat("yyyy-MM-dd").parse(DOB);
                break;
            } catch (Exception e) {
                System.out.println("Invalid Format");
            }
        }

        int age = new java.util.Date().getYear() - date.getYear();

        if (age < 18) {
            System.out.println("Must be at least 18 years of age");
            return;
        }

        System.out.println("Enter Occupation");
        occupation = Main.scanner.nextLine();

        do {
            System.out.println("Enter SIN");
            SIN = Main.scanner.nextLine();
        } while (!Main.validate(100000000, 999999999, SIN));

        registerPerson(Integer.parseInt(SIN), name, address, age, new java.sql.Date(date.getTime()), occupation);
        registerUser(id, password, Integer.parseInt(SIN));
    }

    void deleteAccount(String userID) {
        try {
            String query = "SELECT * FROM User WHERE userID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();
            int SIN = 0;
            if (rs.next()) {
                SIN = rs.getInt("SIN");
            }

            BookingManager bookingManager = new BookingManager(connection);
            ListingManager listingManager = new ListingManager(connection);

            ArrayList<Integer> lIDs = bookingManager.getCurrentBookings(userID);
            ArrayList<Integer> hosted = listingManager.getCurrentListings(userID);
            for (Integer lID : lIDs)
                bookingManager.cancel_booking(lID);

            for (Integer lID : hosted)
                listingManager.removeListing(lID);

            String personquery = "DELETE FROM Person WHERE SIN = ?";
            String userquery = "DELETE FROM User WHERE userID = ?";

            pstmt = connection.prepareStatement(userquery);
            pstmt.setString(1, userID);
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement(personquery);
            pstmt.setInt(1, SIN);
            pstmt.executeUpdate();

            System.out.println("Deleted Successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
