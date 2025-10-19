SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS response;
DROP TABLE IF EXISTS survey_application;
DROP TABLE IF EXISTS option_answer;
DROP TABLE IF EXISTS question;
DROP TABLE IF EXISTS company_survey;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS survey;
DROP TABLE IF EXISTS company;

SET FOREIGN_KEY_CHECKS = 1;

-- ========== COMPANY ==========
CREATE TABLE company (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    tax_id VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== EMPLOYEE ==========
CREATE TABLE employee (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    position VARCHAR(100),
    department VARCHAR(100),
    seniority_years INT,
    gender ENUM('M','F','Otro'),
    age INT,
    status ENUM('activo','inactivo') DEFAULT 'activo',
    PRIMARY KEY (id),
    CONSTRAINT fk_employee_company FOREIGN KEY (company_id)
        REFERENCES company(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== SURVEY ==========
CREATE TABLE survey (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    guide_type ENUM('I','II','III','Personalizado') DEFAULT 'Personalizado',
    active BOOLEAN DEFAULT TRUE,
    version VARCHAR(20),
    base_survey_id BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_survey_base FOREIGN KEY (base_survey_id)
        REFERENCES survey(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== COMPANY_SURVEY ==========
CREATE TABLE company_survey (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    survey_id BIGINT NOT NULL,
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    due_date DATE,
    company_version VARCHAR(20),
    status ENUM('activo','inactivo') DEFAULT 'activo',
    completion_rate DECIMAL(5,2) DEFAULT 0.00,
    notes TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_cs_company FOREIGN KEY (company_id)
        REFERENCES company(id) ON DELETE CASCADE,
    CONSTRAINT fk_cs_survey FOREIGN KEY (survey_id)
        REFERENCES survey(id) ON DELETE CASCADE,
    UNIQUE (company_id, survey_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== QUESTION ==========
CREATE TABLE question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    survey_id BIGINT NOT NULL,
    text VARCHAR(500) NOT NULL,
    response_type ENUM('likert','multiple_choice','open') DEFAULT 'likert',
    sort_order INT,
    risk_factor VARCHAR(100),
    category VARCHAR(100),
    guide_type VARCHAR(10),
    PRIMARY KEY (id),
    CONSTRAINT fk_question_survey FOREIGN KEY (survey_id)
        REFERENCES survey(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== OPTION_ANSWER ==========
CREATE TABLE option_answer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    text VARCHAR(200) NOT NULL,
    value INT,
    sort_order INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_option_question FOREIGN KEY (question_id)
        REFERENCES question(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== SURVEY_APPLICATION ==========
CREATE TABLE survey_application (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company_survey_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME,
    status ENUM('pendiente','en_progreso','completado') DEFAULT 'pendiente',
    score INT DEFAULT 0,
    risk_level ENUM('Bajo','Medio','Alto'),
    PRIMARY KEY (id),
    CONSTRAINT fk_sa_company_survey FOREIGN KEY (company_survey_id)
        REFERENCES company_survey(id) ON DELETE CASCADE,
    CONSTRAINT fk_sa_employee FOREIGN KEY (employee_id)
        REFERENCES employee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========== RESPONSE ==========
CREATE TABLE response (
    id BIGINT NOT NULL AUTO_INCREMENT,
    survey_application_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    option_answer_id BIGINT NULL,
    free_text VARCHAR(500),
    value INT,
    answered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_response_survey_application FOREIGN KEY (survey_application_id)
        REFERENCES survey_application(id) ON DELETE CASCADE,
    CONSTRAINT fk_response_question FOREIGN KEY (question_id)
        REFERENCES question(id) ON DELETE CASCADE,
    CONSTRAINT fk_response_option FOREIGN KEY (option_answer_id)
        REFERENCES option_answer(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;