CREATE TABLE IF NOT EXISTS Invoice(
 InvoiceID INT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY,
 ClientID VARCHAR(4),
 Entry DATETIME NOT NULL,
 TotalAmount DECIMAL(10,2) DEFAULT 0,
 Locked BOOLEAN DEFAULT FALSE,
 FOREIGN KEY (ClientID) REFERENCES Client(ClientID)
);