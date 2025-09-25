-- Drop tables if exist (for clean init)
DROP TABLE IF EXISTS employee_answer;
DROP TABLE IF EXISTS survey_response;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS survey;
DROP TABLE IF EXISTS employee;

-- Employee table
CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    position VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    hire_date DATE
);

-- Survey table
CREATE TABLE survey (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT
);

-- Question table
CREATE TABLE question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    text VARCHAR(500),
    type VARCHAR(50),
    options VARCHAR(500),
    answer_scores VARCHAR(500),
    survey_id BIGINT,
    CONSTRAINT fk_question_survey FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE
);

-- SurveyResponse table
CREATE TABLE survey_response (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT,
    survey_id BIGINT,
    risk_level VARCHAR(50),
    submitted_at DATETIME,
    CONSTRAINT fk_sr_employee FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    CONSTRAINT fk_sr_survey FOREIGN KEY (survey_id) REFERENCES survey(id) ON DELETE CASCADE
);

-- EmployeeAnswer table
CREATE TABLE employee_answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT,
    answer VARCHAR(255),
    survey_response_id BIGINT,
    CONSTRAINT fk_ea_question FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE,
    CONSTRAINT fk_ea_sr FOREIGN KEY (survey_response_id) REFERENCES survey_response(id) ON DELETE CASCADE
);

-- Fake Employees
INSERT INTO employee (name, department, position, email, hire_date) VALUES
('Juan Perez', 'Operations', 'Supervisor', 'juan.perez@example.com', '2022-01-15'),
('Maria Lopez', 'HR', 'Analyst', 'maria.lopez@example.com', '2021-05-20'),
('Carlos Sanchez', 'IT', 'Developer', 'carlos.sanchez@example.com', '2023-03-10');

-- Fake Survey
INSERT INTO survey (title, description) VALUES
('Cuestionario I', 'NOM-035 Cuestionario de ejemplo');

-- Fake Questions for Survey 1
INSERT INTO question (text, type, options, answer_scores, survey_id) VALUES
('¿Te sientes motivado en tu trabajo?', 'single-choice', 'A,B,C,D', '{\"A\":1,\"B\":2,\"C\":3,\"D\":4}', 1),
('¿Sientes estrés frecuente?', 'single-choice', 'Sí,No', '{\"Sí\":5,\"No\":0}', 1),
('¿Consideras tu ambiente laboral agradable?', 'single-choice', 'Sí,No', '{\"Sí\":0,\"No\":5}', 1);

-- Fake SurveyResponses
INSERT INTO survey_response (employee_id, survey_id, risk_level, submitted_at) VALUES
(1, 1, 'Medium', '2025-09-24 09:00:00'),
(2, 1, 'Low', '2025-09-24 10:00:00');

-- Fake EmployeeAnswers for SurveyResponse 1 (Juan Perez)
INSERT INTO employee_answer (question_id, answer, survey_response_id) VALUES
(1, 'B', 1),
(2, 'Sí', 1),
(3, 'No', 1);

-- Fake EmployeeAnswers for SurveyResponse 2 (Maria Lopez)
INSERT INTO employee_answer (question_id, answer, survey_response_id) VALUES
(1, 'A', 2),
(2, 'No', 2),
(3, 'Sí', 2);