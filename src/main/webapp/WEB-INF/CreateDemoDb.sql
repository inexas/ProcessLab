-- Script to set up demo database for ProcessLab visualizations
-- The script 

DROP VIEW ordersView IF EXISTS;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS lineitems;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS team;
DROP TABLE IF EXISTS inquiries;

-- P R O D U C T S

CREATE TABLE products(
    id INT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    quantity INT NOT NULL,
    price INT NOT NULL);

INSERT INTO products(id,name,quantity,price)VALUES(1,'TomTom XL IQ',150, 245);
INSERT INTO products(id,name,quantity,price)VALUES(2,'Navigon 4350 max',642, 319);
INSERT INTO products(id,name,quantity,price)VALUES(3,'Garmin Oregon 300',65, 409);
INSERT INTO products(id,name,quantity,price)VALUES(4,'Falk F12',322, 475);
INSERT INTO products(id,name,quantity,price)VALUES(5,'Blaupunkt TravelPilot 200',322, 229);

-- C U S T O M E R S

CREATE TABLE customers(
    id INT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    country VARCHAR(32) NOT NULL);

INSERT INTO customers(id,name,country)VALUES(1,'Travel Sure Inc','US');
INSERT INTO customers(id,name,country)VALUES(2,'Arthurs GPS Emporium','GB');
INSERT INTO customers(id,name,country)VALUES(3,'NavX','FR');
INSERT INTO customers(id,name,country)VALUES(4,'Gruezi GPS','CH');
INSERT INTO customers(id,name,country)VALUES(5,'Neverlost','AU');

-- O R D E R S

CREATE TABLE orders(
    id INT PRIMARY KEY,
    date DATE NOT NULL,
    employeeId INT NOT NULL,
    customerId INT NOT NULL);

-- L I N E I T E M S 

CREATE TABLE lineitems(
    orderId INT NOT NULL,
    productId INT NOT NULL,
    price INT NOT NULL,
    quantity INT NOT NULL);

-- E M P L O Y E E S

CREATE TABLE employees(
    id INT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    managerId INT,
    email VARCHAR(32) NOT NULL);

INSERT INTO employees(id,name,managerId,email)VALUES(1,'Alice',null,'alice@inexas.com');
INSERT INTO employees(id,name,managerId,email)VALUES(2,'Bob',1,'bob@inexas.com');
INSERT INTO employees(id,name,managerId,email)VALUES(3,'Charles',1,'charles@inexas.com');
INSERT INTO employees(id,name,managerId,email)VALUES(4,'Diane',2,'diane@inexas.com');
INSERT INTO employees(id,name,managerId,email)VALUES(5,'Ernie',2,'ernie@inexas.com');

-- O R D E R S V I E W

CREATE VIEW ordersView AS
    SELECT
        o.id as orderId,employeeId,customerId,date,
        l.productId,l.price,quantity,l.price*quantity as lineTotal,
        p.name AS productName,
        e.name AS employeeName,
        c.name AS customerName,c.country
    FROM
        orders o
        JOIN lineitems l ON o.id = l.orderId
        JOIN products p ON p.id = l.productId
        JOIN employees e ON e.id = o.employeeId
        JOIN customers c ON c.id = o.customerId;

-- T E A M
CREATE TABLE team(
    id INT PRIMARY KEY,
    firstName VARCHAR NOT NULL,
    lastName VARCHAR NOT NULL);

--  INQUIRIES
CREATE TABLE inquiries(
    id INT PRIMARY KEY,
    received DATE NOT NULL,
    category VARCHAR NOT NULL,
    state VARCHAR NOT NULL,
    customerName VARCHAR NOT NULL,
    assigneeId INT NOT NULL);

-- Test table to test data types
CREATE TABLE test(
    intValue INT,
    dateValue DATE,
    timeValue TIME,
    timestampValue TIMESTAMP,
    doubleValue DATE,
    varcharValue VARCHAR);
INSERT INTO test(intValue,dateValue,timeValue,timestampValue,doubleValue,varcharValue)
    VALUES(null,null,null,null,null,null);
