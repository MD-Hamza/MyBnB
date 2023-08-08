package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class CommentManager {
    Connection connection;
    public CommentManager(Connection conn) {
        connection = conn;
    }

    /* Before calling this function it should be verified if the User has booked listing with
       ID LID */
    public void addListingComment(int lID, String comment, int rating, String userID) {
        String query = "INSERT INTO Feedback_Listing VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, lID);
            pstmt.setString(2, comment);
            pstmt.setInt(3, rating);
            pstmt.setString(4, userID);

            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addUserComment(String userID, String comment, int rating, String hostID) {
        String query = "INSERT INTO Feedback_User VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userID);
            pstmt.setString(2, hostID);
            pstmt.setString(3, comment);
            pstmt.setInt(4, rating);

            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
