DROP TABLE IF EXISTS amenities;
DROP TABLE IF EXISTS availability;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS card;
DROP TABLE IF EXISTS feedback_listing;
DROP TABLE IF EXISTS feedback_user;
DROP TABLE IF EXISTS Cancelled_Bookings;
DROP TABLE IF EXISTS Cancelled_Listings;
DROP TABLE IF EXISTS host;
DROP TABLE IF EXISTS listing;
DROP TABLE IF EXISTS location;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS person;

CREATE TABLE person (
    SIN bigint PRIMARY KEY,
    name varchar(50),
    Age int,
    address varchar(50),
    DOB date,
    Occupation varchar(50)
);

CREATE TABLE user (
    UserID varchar(24) NOT NULL PRIMARY KEY,
    Password varchar(24) NOT NULL,
    SIN bigInt,
    FOREIGN KEY (SIN) REFERENCES person(SIN)
);

CREATE TABLE location (
    longitude double,
    latitude double,
    postal_code varchar(15),
    city varchar(50),
    country varchar(50),
    PRIMARY KEY (latitude, longitude)
);

CREATE TABLE listing (
    lID int PRIMARY KEY,
    type varchar(50),
    longitude double,
    latitude double,
    FOREIGN KEY (latitude, longitude) REFERENCES location(latitude, longitude)
);

CREATE TABLE amenities (
    name varchar(50) NOT NULL,
    lID int NOT NULL,
    FOREIGN KEY (lID) REFERENCES listing(lID),
    PRIMARY KEY(lID, name)
);

CREATE TABLE availability (
    lID int NOT NULL,
    date date NOT NULL,
    Price double,
    PRIMARY KEY (lID, date),
    FOREIGN KEY (lID) REFERENCES listing(lID)
);

CREATE TABLE book (
    lID int,
    userID varchar(24),
    date date,
    price int,
    FOREIGN KEY (lID) REFERENCES listing(lID),
    FOREIGN KEY (userID) REFERENCES user(UserID),
    PRIMARY KEY(lID, userID, date)
);

CREATE TABLE Cancelled_Bookings (
    lID INT,
    userID VARCHAR(24),
    date DATE,
    PRIMARY KEY (lID, userID, date),
    FOREIGN KEY (lID) REFERENCES Listing(lID),
    FOREIGN KEY (userID) REFERENCES User(UserID)
);

CREATE TABLE Cancelled_Listings (
    lID int,
    userID VARCHAR(24),
    PRIMARY KEY (lID, userID),
    FOREIGN KEY (lID) REFERENCES Listing(lID),
    FOREIGN KEY (userID) REFERENCES User(UserID)
);

CREATE TABLE card (
    number bigint PRIMARY KEY,
    cvc int,
    expiry_date date
);

CREATE TABLE feedback_listing (
    lID int,
    Comment varchar(200),
    Rating int,
    UserID varchar(24),
    FOREIGN KEY (lID) REFERENCES listing(lID),
    FOREIGN KEY (UserID) REFERENCES user(UserID)
);

CREATE TABLE feedback_user (
    UserID varchar(24),
    HostID varchar(24),
    Comment varchar(200),
    Rating int,
    PRIMARY KEY (UserID, HostID),
    FOREIGN KEY (UserID) REFERENCES user(UserID),
    FOREIGN KEY (HostID) REFERENCES user(UserID)
);

CREATE TABLE payment (
    number bigint,
    userID varchar(24),
    FOREIGN KEY (number) REFERENCES card(number),
    FOREIGN KEY (userID) REFERENCES user(UserID),
    PRIMARY KEY (number, userID)
);

CREATE TABLE host (
    lID int NOT NULL,
    UserID varchar(24) NOT NULL,
    FOREIGN KEY (lID) REFERENCES listing(lID),
    FOREIGN KEY (UserID) REFERENCES user(UserID),
    PRIMARY KEY (lID, UserID)
);
