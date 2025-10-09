-- ================================
-- Disable foreign key checks for clean reinitialization
-- ================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE response;
TRUNCATE TABLE survey_application;
TRUNCATE TABLE option_answer;
TRUNCATE TABLE question;
TRUNCATE TABLE company_survey;
TRUNCATE TABLE employee;
TRUNCATE TABLE survey;
TRUNCATE TABLE company;

SET FOREIGN_KEY_CHECKS = 1;

-- ================================
-- COMPANY
-- ================================
INSERT INTO company (id, name, tax_id, created_at) VALUES
(1, 'TechNova SA', 'TNX123456', NOW()),
(2, 'EcoWorld Ltd', 'ECO987654', NOW()),
(3, 'HealthCorp MX', 'HCM111222', NOW()),
(4, 'BuildIt Global', 'BIG333444', NOW()),
(5, 'SoftLogic Systems', 'SLS555666', NOW());

-- ================================
-- EMPLOYEE
-- ================================
INSERT INTO employee (id, company_id, name, email, position, department, seniority_years, gender, age, status) VALUES
(1, 1, 'Carlos Pérez', 'carlos.perez@technova.com', 'Developer', 'IT', 3, 'M', 28, 'activo'),
(2, 1, 'Laura Gómez', 'laura.gomez@technova.com', 'QA Engineer', 'IT', 2, 'F', 26, 'activo'),
(3, 2, 'Miguel Torres', 'miguel.torres@ecoworld.com', 'Analyst', 'Operations', 5, 'M', 34, 'activo'),
(4, 3, 'Sofía Ramírez', 'sofia.ramirez@healthcorp.com', 'HR Manager', 'HR', 6, 'F', 40, 'activo'),
(5, 3, 'Andrés López', 'andres.lopez@healthcorp.com', 'Nurse', 'Medical', 4, 'M', 30, 'activo'),
(6, 4, 'Elena Vargas', 'elena.vargas@buildit.com', 'Architect', 'Design', 7, 'F', 37, 'activo'),
(7, 4, 'Luis Fernández', 'luis.fernandez@buildit.com', 'Engineer', 'Production', 5, 'M', 32, 'activo'),
(8, 4, 'Paula Jiménez', 'paula.jimenez@buildit.com', 'Supervisor', 'Production', 8, 'F', 38, 'activo'),
(9, 5, 'Ricardo Soto', 'ricardo.soto@softlogic.com', 'Project Manager', 'PMO', 9, 'M', 41, 'activo'),
(10, 5, 'María Cruz', 'maria.cruz@softlogic.com', 'Developer', 'IT', 3, 'F', 27, 'activo');

-- ================================
-- SURVEY
-- ================================
INSERT INTO survey (id, title, description, guide_type, active, version, base_survey_id, created_at) VALUES
(1, 'Guía I - Condiciones de Trabajo', 'Evaluación de condiciones generales del entorno laboral.', 'I', TRUE, '1.0', NULL, NOW()),
(2, 'Guía II - Factores de Riesgo Psicosocial', 'Identificación de factores de riesgo psicosocial.', 'II', TRUE, '1.0', NULL, NOW()),
(3, 'Guía III - Entorno Organizacional', 'Evaluación del entorno organizacional y clima laboral.', 'III', TRUE, '1.0', NULL, NOW()),
(4, 'Encuesta de Satisfacción', 'Encuesta personalizada de satisfacción laboral.', 'Personalizado', TRUE, '2.0', NULL, NOW()),
(5, 'Encuesta de Comunicación Interna', 'Evaluación de canales y efectividad de comunicación.', 'Personalizado', TRUE, '2.1', NULL, NOW());

-- ================================
-- COMPANY_SURVEY
-- ================================
INSERT INTO company_survey (id, company_id, survey_id, assigned_at, due_date, company_version, status, completion_rate, notes) VALUES
(1, 1, 1, NOW(), '2025-12-01', 'v1', 'activo', 85.00, 'Primera aplicación del año'),
(2, 2, 2, NOW(), '2025-11-15', 'v1', 'activo', 70.00, 'Evaluación semestral'),
(3, 3, 3, NOW(), '2025-10-30', 'v2', 'activo', 90.00, 'Actualización de versión'),
(4, 4, 4, NOW(), '2025-12-31', 'v1', 'activo', 75.00, 'Primera aplicación'),
(5, 5, 5, NOW(), '2025-12-31', 'v1', 'activo', 80.00, 'Evaluación piloto');

-- ================================
-- QUESTION
-- ================================
INSERT INTO question (id, survey_id, text, response_type, sort_order, risk_factor, category) VALUES
(1, 1, '¿Las condiciones de tu área de trabajo son adecuadas?', 'likert', 1, 'Condiciones físicas', 'Ambiente'),
(2, 1, '¿Cuentas con el equipo necesario para tus tareas?', 'likert', 2, 'Recursos', 'Ambiente'),
(3, 2, '¿Sientes presión excesiva por el trabajo?', 'likert', 1, 'Estrés', 'Psicosocial'),
(4, 2, '¿Tu jornada laboral afecta tu descanso?', 'likert', 2, 'Cansancio', 'Psicosocial'),
(5, 3, '¿Tu jefe reconoce tu esfuerzo?', 'likert', 1, 'Reconocimiento', 'Organizacional'),
(6, 3, '¿Existe buena comunicación interna?', 'likert', 2, 'Comunicación', 'Organizacional'),
(7, 4, '¿Estás satisfecho con tu trabajo actual?', 'likert', 1, 'Satisfacción', 'General'),
(8, 4, '¿Recomendarías la empresa a otros?', 'likert', 2, 'Recomendación', 'General'),
(9, 5, '¿Los canales de comunicación son efectivos?', 'likert', 1, 'Comunicación', 'Comunicación'),
(10, 5, '¿La información llega a tiempo?', 'likert', 2, 'Transparencia', 'Comunicación');

-- ================================
-- OPTION_ANSWER
-- ================================
INSERT INTO option_answer (id, question_id, text, value, sort_order) VALUES
(1, 1, 'Nunca', 1, 1),
(2, 1, 'Rara vez', 2, 2),
(3, 1, 'A veces', 3, 3),
(4, 1, 'Frecuentemente', 4, 4),
(5, 1, 'Siempre', 5, 5),

(6, 2, 'Nunca', 1, 1),
(7, 2, 'Rara vez', 2, 2),
(8, 2, 'A veces', 3, 3),
(9, 2, 'Frecuentemente', 4, 4),
(10, 2, 'Siempre', 5, 5),

(11, 3, 'Nunca', 1, 1),
(12, 3, 'Rara vez', 2, 2),
(13, 3, 'A veces', 3, 3),
(14, 3, 'Frecuentemente', 4, 4),
(15, 3, 'Siempre', 5, 5),

(16, 4, 'Nunca', 1, 1),
(17, 4, 'Rara vez', 2, 2),
(18, 4, 'A veces', 3, 3),
(19, 4, 'Frecuentemente', 4, 4),
(20, 4, 'Siempre', 5, 5);

-- ================================
-- SURVEY_APPLICATION (explicit IDs for consistency)
-- ================================
INSERT INTO survey_application (id, company_survey_id, employee_id, started_at, completed_at, status, score, risk_level) VALUES
(1, 1, 1, NOW(), NULL, 'pendiente', NULL, 'Medio'),
(2, 1, 2, NOW(), NOW(), 'completado', 85, 'Bajo'),
(3, 2, 3, NOW(), NULL, 'pendiente', NULL, 'Medio'),
(4, 3, 4, NOW(), NOW(), 'completado', 92, 'Bajo'),
(5, 3, 5, NOW(), NULL, 'pendiente', NULL, 'Alto'),
(6, 4, 7, NOW(), NOW(), 'completado', 88, 'Medio'),
(7, 4, 8, NOW(), NOW(), 'completado', 90, 'Bajo'),
(8, 5, 9, NOW(), NULL, 'pendiente', NULL, 'Medio'),
(9, 5, 10, NOW(), NOW(), 'completado', 95, 'Bajo');

-- ================================
-- RESPONSE
-- ================================
INSERT INTO response (survey_application_id, question_id, option_answer_id, value, free_text) VALUES
(2, 1, 4, 4, NULL),
(2, 2, 5, 5, NULL),
(4, 3, 4, 4, NULL),
(4, 4, 5, 5, NULL),
(6, 5, 5, 5, NULL),
(6, 6, 4, 4, NULL),
(7, 7, 5, 5, NULL),
(7, 8, 5, 5, NULL),
(9, 9, 4, 4, 'Equilibrio adecuado.');

