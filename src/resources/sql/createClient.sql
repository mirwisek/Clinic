CREATE TABLE IF NOT EXISTS Client(
 PhoneNumber VARCHAR(11) PRIMARY KEY,
 ClientID VARCHAR(4) UNIQUE,
 ClientName VARCHAR(100) NOT NULL,
 Type VARCHAR(30) NOT NULL,
 Locked BOOLEAN DEFAULT FALSE
);