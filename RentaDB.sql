DROP DATABASE IF EXISTS RentalDB;
CREATE DATABASE RentalDB;
USE RentalDB;

CREATE TABLE BRANCH (
    Branch_id           VARCHAR(10) PRIMARY KEY,
    City                VARCHAR(50),
    Street_name         VARCHAR(50),
    BPhone_number       VARCHAR(20),
    Branch_manager_id   VARCHAR(10) UNIQUE
);

CREATE TABLE EMPLOYEE (
    Employee_id          VARCHAR(10) PRIMARY KEY,
    Fname                VARCHAR(30) NOT NULL,
    Mname                VARCHAR(30),
    Lname                VARCHAR(30) NOT NULL,
    Eemail               VARCHAR(50) UNIQUE,
    Ephone_number        VARCHAR(20),
    Salary               DECIMAL(10,2),
    Employee_branch_id   VARCHAR(10),
    FOREIGN KEY (Employee_branch_id)
        REFERENCES BRANCH(Branch_id)
        ON DELETE SET NULL 
        ON UPDATE CASCADE
);

ALTER TABLE BRANCH
ADD CONSTRAINT FK_Manager 
FOREIGN KEY (Branch_manager_id)
REFERENCES EMPLOYEE(Employee_id)
ON DELETE SET NULL
ON UPDATE CASCADE;

CREATE TABLE EQUIPMENT (
    Equipment_id        VARCHAR(10) PRIMARY KEY,
    Qname               VARCHAR(50) NOT NULL,
    Type                VARCHAR(40),
    Description         VARCHAR(200),
    Price               DECIMAL(10,2),
    Rental_fee          DECIMAL(10,2),
    Available_quantity  INT
);

CREATE TABLE STOCK (
    Stock_branch_id     VARCHAR(10),
    Stock_equipment_id  VARCHAR(10),
    PRIMARY KEY (Stock_branch_id, Stock_equipment_id),
    FOREIGN KEY (Stock_branch_id)
        REFERENCES BRANCH(Branch_id)
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    FOREIGN KEY (Stock_equipment_id)
        REFERENCES EQUIPMENT(Equipment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE CUSTOMER (
    Customer_id      VARCHAR(10) PRIMARY KEY,
    Fname            VARCHAR(30) NOT NULL,
    Mname            VARCHAR(30),
    Lname            VARCHAR(30) NOT NULL,
    Instagram_user   VARCHAR(50) UNIQUE NOT NULL,
    City             VARCHAR(50),
    Street_name      VARCHAR(50),
    Cemail           VARCHAR(50) UNIQUE NOT NULL,
    Cphone_number    VARCHAR(20) UNIQUE NOT NULL
);

CREATE TABLE RENTAL (
    Rental_num          VARCHAR(10) PRIMARY KEY,
    Receive_datetime    DATETIME NOT NULL,
    Return_datetime     DATETIME,
    Rental_customer_id  VARCHAR(10) NOT NULL,
    FOREIGN KEY (Rental_customer_id)
        REFERENCES CUSTOMER(Customer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE BILL (
    Bill_id          VARCHAR(10) PRIMARY KEY,
    Bill_date        DATE NOT NULL,
    Payment_method   VARCHAR(20),
    Total_amount     DECIMAL(10,2),
    Bill_rental_num  VARCHAR(10) UNIQUE NOT NULL,
    FOREIGN KEY (Bill_rental_num)
        REFERENCES RENTAL(Rental_num)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE MAKES (
    Equipment_quantity INT NOT NULL,
    M_rental_number    VARCHAR(10),
    M_equipment_id     VARCHAR(10),
    M_customer_id      VARCHAR(10),
    PRIMARY KEY (M_rental_number, M_equipment_id, M_customer_id),
    FOREIGN KEY (M_rental_number)
        REFERENCES RENTAL(Rental_num)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (M_equipment_id)
        REFERENCES EQUIPMENT(Equipment_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (M_customer_id)
        REFERENCES CUSTOMER(Customer_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);