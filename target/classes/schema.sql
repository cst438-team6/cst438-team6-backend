DROP DATABASE IF EXISTS courses;
CREATE DATABASE courses;
USE courses;

CREATE TABLE term (
    term_id       INT PRIMARY KEY,
    tyear         INT         NOT NULL CHECK (tyear BETWEEN 2000 AND 2030),
    semester      VARCHAR(10) NOT NULL CHECK (semester IN ('Spring', 'Fall')),
    add_date      DATE        NOT NULL,
    add_deadline  DATE        NOT NULL,
    drop_deadline DATE        NOT NULL,
    start_date    DATE        NOT NULL,
    end_date      DATE        NOT NULL
);

CREATE TABLE course (
    course_id VARCHAR(10) PRIMARY KEY,
    title     VARCHAR(100) NOT NULL,
    credits   INT          NOT NULL CHECK (credits >= 0)
);

CREATE TABLE section (
    section_no       INT AUTO_INCREMENT PRIMARY KEY,
    course_id        VARCHAR(10) NOT NULL,
    sec_id           INT         NOT NULL,
    term_id          INT         NOT NULL,
    building         VARCHAR(10),
    room             VARCHAR(10),
    times            VARCHAR(25),
    instructor_email VARCHAR(50),
    FOREIGN KEY (course_id) REFERENCES course(course_id),
    FOREIGN KEY (term_id) REFERENCES term(term_id)
) AUTO_INCREMENT = 1000;

CREATE TABLE user_table (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(50)  NOT NULL,
    email    VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    type     VARCHAR(10)  NOT NULL CHECK (type IN ('STUDENT', 'ADMIN', 'INSTRUCTOR'))
) AUTO_INCREMENT = 7000;

CREATE TABLE enrollment (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    grade         VARCHAR(5),
    section_no    INT NOT NULL,
    user_id       INT NOT NULL,
    FOREIGN KEY (section_no) REFERENCES section(section_no),
    FOREIGN KEY (user_id) REFERENCES user_table(id)
) AUTO_INCREMENT = 10000;

CREATE TABLE assignment (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    section_no    INT          NOT NULL,
    title         VARCHAR(250) NOT NULL,
    due_date      DATE,
    FOREIGN KEY (section_no) REFERENCES section(section_no)
) AUTO_INCREMENT = 6000;

CREATE TABLE grade (
    grade_id      INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT NOT NULL,
    assignment_id INT NOT NULL,
    score         INT CHECK (score BETWEEN 0 AND 100),
    FOREIGN KEY (enrollment_id) REFERENCES enrollment(enrollment_id),
    FOREIGN KEY (assignment_id) REFERENCES assignment(assignment_id)
) AUTO_INCREMENT = 12000;
