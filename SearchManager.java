package org.example;

import java.sql.*;
import java.util.ArrayList;

public class SearchManager {
    Connection connection;
    public SearchManager(Connection conn) {
        connection = conn;
    }

    public void getNearest(double longitude, double latitude, double distance, boolean price, boolean descending) {
        String distanceQuery = "SQRT(POW(longitude - ?, 2) + POW(latitude - ?, 2))";
        String query = "SELECT *," + distanceQuery + " AS Distance FROM Listing NATURAL JOIN Location NATURAL JOIN Host NATURAL JOIN Availability " +
                "WHERE " + distanceQuery + " < ?";

        if (price) {
            query += " ORDER BY price;";
            if (descending) {
                query = query.substring(0, query.length() - 1) + " DESC";
            }
        } else {
            query += ";";
        }

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDouble(1, longitude);
            pstmt.setDouble(2, latitude);
            pstmt.setDouble(3, longitude);
            pstmt.setDouble(4, latitude);
            pstmt.setDouble(5, distance);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int lID = rs.getInt("lID");
                    String type = rs.getString("type");
                    double l1 = rs.getDouble("longitude");
                    double l2 = rs.getDouble("latitude");
                    String pc = rs.getString("postal_code");
                    String city = rs.getString("city");
                    String country = rs.getString("country");

                    System.out.println("Listing ID: " + lID);
                    System.out.println("----------------------------------------------");
                    System.out.println("Type: " + type);
                    System.out.println("Longitude: " + l1);
                    System.out.println("Latitude: " + l2);
                    System.out.println("Postal Code: " + pc);
                    System.out.println("City: " + city);
                    System.out.println("Country: " + country);

                    Main.listingManager.printAvailability(lID);
                    Main.listingManager.printAmenities(lID);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getAdjacentPostal(String pc) {
        String query = "SELECT DISTINCT lID, type, longitude, latitude, postal_code, city, country " +
                "FROM Listing NATURAL JOIN Location NATURAL JOIN Host " +
                "WHERE postal_code LIKE '" + pc.substring(0, pc.length() - 1) + "_' ;";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int lID = rs.getInt("lID");
                String type = rs.getString("type");
                double l1 = rs.getDouble("longitude");
                double l2 = rs.getDouble("latitude");
                String postal = rs.getString("postal_code");
                String city = rs.getString("city");
                String country = rs.getString("country");
                System.out.println(lID + ", " + type+ ", " + l1 +
                        ", " + l2 + ", " + postal + ", " + city + ", " + country);
                Main.listingManager.printAvailability(lID);
                Main.listingManager.printAmenities(lID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getExact(double longitude, double latitude) {
        String query = "SELECT * " +
                "FROM Listing NATURAL JOIN Location NATURAL JOIN Host " +
                "WHERE longitude = ? AND latitude = ?;";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, longitude);
            preparedStatement.setDouble(2, latitude);

            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                int lID = rs.getInt("lID");
                String type = rs.getString("type");
                double l1 = rs.getDouble("longitude");
                double l2 = rs.getDouble("latitude");
                String postal = rs.getString("postal_code");
                String city1 = rs.getString("city");
                String country1 = rs.getString("country");
                System.out.println(lID + ", " + type + ", " + l1 + ", " + l2 + ", " + postal + ", " + city1 + ", " + country1);
                Main.listingManager.printAvailability(lID);
                Main.listingManager.printAmenities(lID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getFromDate(Date start, Date end) {
        String query = "SELECT DISTINCT lID, type, longitude, latitude, postal_code, city, country " +
                "FROM Listing NATURAL JOIN Location NATURAL JOIN Host NATURAL JOIN Availability " +
                "WHERE date >= ? AND date <= ? ;";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDate(1, start);
            preparedStatement.setDate(2, end);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                int lID = rs.getInt("lID");
                String type = rs.getString("type");
                double l1 = rs.getDouble("longitude");
                double l2 = rs.getDouble("latitude");
                String postal = rs.getString("postal_code");
                String city1 = rs.getString("city");
                String country1 = rs.getString("country");
                System.out.println(lID + ", " + type + ", " + l1 + ", " + l2 + ", " + postal + ", " +
                        city1 + ", " + country1);
                Main.listingManager.printAvailability(lID);
                Main.listingManager.printAmenities(lID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void applyFilter(String pc, ArrayList<String> amenities, java.util.Date start, java.util.Date end,
                            Double low, Double high) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT DISTINCT lID, type, longitude, latitude, postal_code, city, country" +
                " FROM Listing NATURAL JOIN Location NATURAL JOIN Host NATURAL JOIN Availability WHERE 1=1");

        if (pc != null) {
            queryBuilder.append(" AND postal_code = ?");
        }

        if (amenities != null && amenities.size() > 0) {
            for (String amenity : amenities) {
                queryBuilder.append(" AND EXISTS (SELECT * FROM Amenities WHERE lID = Amenities.lID AND name = ?)");
            }
        }

        if (start != null && end != null) {
            queryBuilder.append(" AND date BETWEEN ? AND ?");
        }

        if (low != null && high != null) {
            queryBuilder.append(" AND price BETWEEN ? AND ?");
        }

        try (PreparedStatement pstmt = connection.prepareStatement(queryBuilder.toString())) {
            int parameterIndex = 1;

            if (pc != null) {
                pstmt.setString(parameterIndex++, pc);
            }

            if (amenities != null && amenities.size() > 0) {
                for (String amenity : amenities) {
                    pstmt.setString(parameterIndex++, amenity);
                }
            }

            if (start != null && end != null) {
                pstmt.setDate(parameterIndex++, new java.sql.Date(start.getTime()));
                pstmt.setDate(parameterIndex++, new java.sql.Date(end.getTime()));
            }

            if (low != null && high != null) {
                pstmt.setDouble(parameterIndex++, low);
                pstmt.setDouble(parameterIndex++, high);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int lID = rs.getInt("lID");
                String type = rs.getString("type");
                double longitude = rs.getDouble("longitude");
                double latitude = rs.getDouble("latitude");
                String postalCode = rs.getString("postal_code");
                String city = rs.getString("city");
                String country = rs.getString("country");
                System.out.println(lID + ", " + type + ", " + longitude + ", " + latitude + ", " + postalCode + ", " + city + ", " + country);
                Main.listingManager.printAvailability(lID);
                Main.listingManager.printAmenities(lID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
