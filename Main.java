package org.example;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class Main {
    static LoginManager login;
    static ListingManager listingManager;
    static Scanner scanner = new Scanner(System.in);

    static
    {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean validate(int start, int end, String input)
    {
        try {
            int i = Integer.parseInt(input);
            if (i < start || i > end)
                return false;
        } catch (Exception e) {
            System.out.println("Please enter a valid integer");
            return false;
        }

        return true;
    }

    public static boolean validateDouble(int start, int end, String input)
    {
        try {
            double i = Double.parseDouble(input);
            if (i < start || i > end)
                return false;
        } catch (Exception e) {
            System.out.println("Please enter a valid integer");
            return false;
        }

        return true;
    }

    public static Date validateDate(String input)
    {
        while (true) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(input);
            } catch (Exception e) {
                System.out.println("Invalid Format Try Again");
                input = scanner.nextLine();
            }
        }
    }

    public static Date validateDate(String input, String format)
    {
        while (true) {
            try {
                return new SimpleDateFormat(format).parse(input);
            } catch (Exception e) {
                System.out.println("Invalid Format Try Again");
                input = scanner.nextLine();
            }
        }
    }

    public static void main(String[] args) {
        Connection conn = null;
        try {
            String url = "jdbc:mysql://127.0.0.1/MyBnB";
            conn = DriverManager.getConnection(url, "root", "");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        login = new LoginManager(conn);
        listingManager = new ListingManager(conn);
        BookingManager bookingManager = new BookingManager(conn);
        SearchManager searchManager = new SearchManager(conn);
        ReportManager reportManager = new ReportManager(conn);

        boolean running = true;

        System.out.println("Welcome to MyBnB");
        while (running) {
            System.out.println("""
                    Select an option below
                    1) Login
                    2) Register
                    3) Book
                    4) Host
                    5) Report
                    6) Exit""");

            String option;
            do {
                option = scanner.nextLine();
            } while (!validate(1, 6, option));

            switch (Integer.parseInt(option)) {
                case 1 -> login.loginDisplay();
                case 2 -> login.registerDisplay();
                case 3 -> bookingManager.bookingDisplay();
                case 4 -> listingManager.listingDisplay();
                case 5 -> reportManager.reportDisplay();
                case 6 -> running = false;
            }
        }
    }
}