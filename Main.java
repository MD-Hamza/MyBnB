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
                    Select on option below
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

            String userID, password;
            switch (Integer.parseInt(option)) {
                case 1:
                    System.out.println("Enter UserID");
                    userID = scanner.nextLine();
                    System.out.println("Enter Password");
                    password = scanner.nextLine();
                    login.signIn(userID, password);
                    break;
                case 2:
                    do {
                        System.out.println("Enter UserID");
                        userID = scanner.nextLine();
                    } while (userID.length() == 0 || !login.isTaken(userID));

                    System.out.println("Enter Password");
                    password = scanner.nextLine();

                    String DOB, occupation, SIN, name, address;

                    System.out.println("Enter Name");
                    name = scanner.nextLine();

                    System.out.println("Enter Address");
                    address = scanner.nextLine();

                    System.out.println("Enter Date of Birth in form YYYY-MM-DD");
                    Date date;

                    while (true) {
                        DOB = scanner.nextLine();
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd").parse(DOB);
                            break;
                        } catch (Exception e) {
                            System.out.println("Invalid Format");
                        }
                    }

                    int age = new Date().getYear() - date.getYear();

                    if (age < 18) {
                        System.out.println("Must be at least 18 years of age");
                        continue;
                    }

                    System.out.println("Enter Occupation");
                    occupation = scanner.nextLine();

                    do {
                        System.out.println("Enter SIN");
                        SIN = scanner.nextLine();
                    } while (!validate(100000000, 999999999, SIN));

                    login.registerPerson(Integer.parseInt(SIN), name, address, age, new java.sql.Date(date.getTime()), occupation);
                    login.registerUser(userID, password, Integer.parseInt(SIN));

                    break;
                case 3:
                    bookingManager.bookingDisplay();
                    break;
                case 4:
                    listingManager.listingDisplay();
                    break;
                case 5:
                    reportManager.reportDisplay();
                    break;
                case 6:
                    running = false;
                    break;
            }
        }
    }
}