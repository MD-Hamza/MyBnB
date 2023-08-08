package org.example;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

import static org.example.Main.login;
import static org.example.Main.scanner;

public class PaymentManager {
    Connection connection;

    public PaymentManager(Connection conn) {
        connection = conn;
    }

    public void RegisterCard(BigInteger cardNumber, int cvc, Date expiry) {
        String query = "INSERT INTO card VALUES (?, ?, ?)";
        String payment_query = "INSERT INTO payment VALUES (?, ?)";

        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setBigDecimal(1, new java.math.BigDecimal(cardNumber));
            pstmt.setInt(2, cvc);
            pstmt.setDate(3, new java.sql.Date(expiry.getTime()));
            pstmt.executeUpdate();

            pstmt = connection.prepareStatement(payment_query);
            pstmt.setBigDecimal(1, new java.math.BigDecimal(cardNumber));
            pstmt.setString(2, Main.login.userID);
            pstmt.executeUpdate();

            System.out.println("Card registered Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveCardInformation() {
        String query = "SELECT * FROM Payment WHERE userID = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, Main.login.userID);
            if (pstmt.executeQuery().next()) {
                System.out.println("You already have a card entering a new one will overwrite the previous, press y to continue");
                if (!Objects.equals(scanner.nextLine(), "y"))
                    return;

                query = "DELETE FROM Payment WHERE userID = ?";
                pstmt = connection.prepareStatement(query);
                pstmt.setString(1, Main.login.userID);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Enter Card Number");
        BigInteger number;
        while (true) {
            try {
                number = new BigInteger(scanner.nextLine());
                break;
            } catch (Exception e) {
                System.out.println("Please enter a valid integer");
            }
        }

        System.out.println("Enter CVC");
        String cvc;
        while (!Main.validate(0, 999, cvc = scanner.nextLine()))
            continue;

        System.out.println("Enter Expiry Date in the form mm/yyyy");
        java.util.Date expiryDate = Main.validateDate(scanner.nextLine(), "MM/yyyy");

        RegisterCard(number, Integer.parseInt(cvc),
                new java.sql.Date(expiryDate.getTime()));
    }
    public void displayPayment(String userID) {
        String query = "SELECT number FROM payment WHERE userID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Paying with card " + rs.getString("number"));
                } else {
                    System.out.println("Card not found please register a card");
                    receiveCardInformation();
                    ResultSet updated = pstmt.executeQuery();
                    updated.next();
                    System.out.println("Paying with card " + updated.getString("number"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
