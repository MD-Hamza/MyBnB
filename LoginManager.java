package org.example;

import java.sql.*;

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
                System.out.println("Successfully Signed In");
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
}
