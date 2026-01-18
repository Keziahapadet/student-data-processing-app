CREATE TABLE IF NOT EXISTS students (
    student_id BIGINT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    dob DATE,
    class VARCHAR(255),
    score INTEGER
);