CREATE DATABASE IF NOT EXISTS language_platform;
USE language_platform;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'INSTRUCTOR', 'LEARNER') NOT NULL
);

CREATE TABLE lessons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    instructor_id INT,
    FOREIGN KEY (instructor_id) REFERENCES users(id)
);

CREATE TABLE progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    learner_id INT,
    lesson_id INT,
    completion_percent INT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_progress (learner_id, lesson_id),
    FOREIGN KEY (learner_id) REFERENCES users(id),
    FOREIGN KEY (lesson_id) REFERENCES lessons(id)
);

INSERT INTO users (name, email, password, role)
VALUES ('Admin User', 'admin@example.com', 'admin123', 'ADMIN');

INSERT INTO users (name, email, password, role)
VALUES ('Instructor John', 'inst@example.com', 'inst123', 'INSTRUCTOR');

INSERT INTO users (name, email, password, role)
VALUES ('Learner Aakashi', 'learn@example.com', 'learn123', 'LEARNER');

