drop database if exists BookLibrary;

create database BookLibrary;

use BookLibrary;

drop table if exists Authors;

create table Authors
(
    AuthorID SMALLINT UNSIGNED AUTO_INCREMENT,
    Title VARCHAR(20),
    FirstName VARCHAR(40),
    MiddleName VARCHAR(40),
    LastName VARCHAR(40) NOT NULL,
    SurTitle VARCHAR(40),
    PRIMARY KEY (AuthorID),
    FULLTEXT LastNameIDX (LastName)
);

drop table if exists Series;

create table Series
(
    SeriesID SMALLINT UNSIGNED AUTO_INCREMENT,
    Name VARCHAR(120) NOT NULL,
    PRIMARY KEY (SeriesID)
);

drop table if exists Books;

create table Books
(
    BookID SMALLINT UNSIGNED AUTO_INCREMENT,
    Title VARCHAR(120) NOT NULL,
    PublishYear YEAR,
    CoverType ENUM ('Hard Cover', 'Soft Cover'),
    PRIMARY KEY (BookID),
    FULLTEXT TitleIDX (Title)
);

drop table if exists BookAuthors;

create table BookAuthors
(
    BookID SMALLINT UNSIGNED NOT NULL,
    AuthorID SMALLINT UNSIGNED NOT NULL,
    PRIMARY KEY (BookID,AuthorID),
    FOREIGN KEY (BookID) REFERENCES Books (BookID),
    FOREIGN KEY (AuthorID) REFERENCES Authors (AuthorID)
);

drop table if exists BookSeries;

create table BookSeries
(
    BookID SMALLINT UNSIGNED NOT NULL,
    SeriesID SMALLINT UNSIGNED NOT NULL,
    SeriesNum VARCHAR(20),
    PRIMARY KEY (BookID,SeriesID),
    FOREIGN KEY (BookID) REFERENCES Books (BookID),
    FOREIGN KEY (AuthorID) REFERENCES Authors (AuthorID)
);


