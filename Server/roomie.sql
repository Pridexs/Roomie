DROP TABLE users;
DROP TABLE house;
DROP TABLE house_member;
DROP TABLE note;

CREATE TABLE users
(
  name varchar(50) NOT NULL,
  email VARCHAR(50) NOT NULL,
  password VARCHAR(80) NOT NULL,
  salt VARCHAR(10) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  api_key char(32),
  PRIMARY KEY(email)
);

CREATE TABLE house
(
  houseID INT NOT NULL AUTO_INCREMENT,
  password VARCHAR(80) NOT NULL,
  salt VARCHAR(10) NOT NULL,
  name VARCHAR(30) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_updated datetime NOT NULL,
  PRIMARY KEY(houseID)
);


CREATE TABLE house_member
(
  email VARCHAR(50) NOT NULL,
  houseID int NOT NULL,
  isAdmin int NOT NULL DEFAULT 0,
  FOREIGN KEY (email) REFERENCES users(email),
  FOREIGN KEY (houseID) REFERENCES house(houseID),
  PRIMARY KEY (email, houseID)
);

CREATE TABLE note
(
  noteID INT PRIMARY KEY AUTO_INCREMENT,
  createdBy VARCHAR(50) NOT NULL,
  houseID int NOT NULL,
  name VARCHAR(50),
  description VARCHAR(500),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
  was_deleted INT DEFAULT 0,
  FOREIGN KEY (houseID) REFERENCES house(houseID),
  FOREIGN KEY (createdBy) REFERENCES users(email)
);
