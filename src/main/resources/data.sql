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
-- Condiciones del ambiente de trabajo
(1, 1, '¿El espacio donde trabaja le permite realizar sus tareas de forma higiénica y segura?', 'likert', 1, 'Condiciones físicas', 'Ambiente'),
(2, 1, '¿Su trabajo le exige hacer mucho esfuerzo físico?', 'likert', 2, 'Esfuerzo físico', 'Ambiente'),
(3, 1, '¿Le preocupa sufrir un accidente en su trabajo como consecuencia del espacio donde lo realiza?', 'likert', 3, 'Seguridad', 'Ambiente'),
(4, 1, '¿Considera que en su trabajo se aplican las normas de seguridad y salud laboral?', 'likert', 4, 'Normas de seguridad', 'Ambiente'),

-- Carga de trabajo
(5, 1, '¿Debido al alto volumen de trabajo tiene que disponer de horas adicionales a su turno habitual?', 'likert', 5, 'Sobrecarga', 'Carga'),
(6, 1, '¿Debido al alto volumen de trabajo no puede disponer de una pausa durante su jornada habitual?', 'likert', 6, 'Pausas', 'Carga'),
(7, 1, '¿Cree que es necesario llevar un ritmo de trabajo mas acelerado para cumplir con sus actividades cotidianas?', 'likert', 7, 'Ritmo', 'Carga'),
(8, 1, '¿Su trabajo le exige que esta muy concentrado en sus labores?', 'likert', 8, 'Concentración', 'Carga'),
(9, 1, '¿Su trabajo requiere que memorice una gran cantidad de información?', 'likert', 9, 'Memoria', 'Carga'),
(10, 1, '¿En su trabajo tiene que tomar decisiones complicadas de forma rápida?', 'likert', 10, 'Decisiones', 'Carga'),
(11, 1, '¿Su trabajo le exige que atienda varios asuntos al mismo tiempo?', 'likert', 11, 'Multitarea', 'Carga'),
(12, 1, '¿En su trabajo usted es responsable de objetos de un alto valor monetario?', 'likert', 12, 'Responsabilidad económica', 'Carga'),
(13, 1, '¿Es responsable ante sus superiores de los resultados que obtenga todo su equipo o área de trabajo?', 'likert', 13, 'Responsabilidad grupal', 'Carga'),

-- Falta de control sobre el trabajo
(14, 2, '¿Considera que dentro de sus actividades diarias realiza alguna que sea fuera de su área de trabajo?', 'likert', 1, 'Actividades ajenas', 'Control'),
(15, 2, '¿Recibe ordenes contradictorias en su trabajo?', 'likert', 2, 'Órdenes contradictorias', 'Control'),
(16, 2, '¿Considera que realiza acciones innecesarias en el desarrollo de su trabajo?', 'likert', 3, 'Acciones innecesarias', 'Control'),

-- Jornada de trabajo
(17, 2, '¿Trabaja horas extraordinarias mas de tres veces por semana?', 'likert', 4, 'Horas extra', 'Jornada'),
(18, 2, '¿Su trabajo le exige laborar en días de descanso, festivos o fines de semana?', 'likert', 5, 'Días festivos', 'Jornada'),
(19, 2, '¿Considera que el tiempo de trabajo es excesivo y no le permite desarrollar su vida personal?', 'likert', 6, 'Equilibrio vida-trabajo', 'Jornada'),
(20, 2, '¿Atiende usted asuntos laborales cuando se encuentra en casa?', 'likert', 7, 'Trabajo en casa', 'Jornada'),
(21, 2, '¿Piensa en actividades personales cuando esta en el trabajo?', 'likert', 8, 'Distracción personal', 'Jornada'),
(22, 2, '¿Cree usted que sus responsabilidades familiares o personales afectan su trabajo?', 'likert', 9, 'Interferencia familiar', 'Jornada'),

-- Interferencia en la relación trabajo-familia
(23, 2, '¿Su trabajo le permite que desarrolle nuevas habilidades?', 'likert', 10, 'Desarrollo de habilidades', 'Desarrollo'),
(24, 2, '¿En su trabajo puede aspirar a un mejor puesto?', 'likert', 11, 'Promoción', 'Desarrollo'),
(25, 2, '¿Durante su jornada de trabajo puede tomar pausas cuando lo necesite?', 'likert', 12, 'Pausas necesarias', 'Autonomía'),
(26, 2, '¿Puede decidir que trabajo realiza y que trabajo no durante su jornada?', 'likert', 13, 'Decisión de tareas', 'Autonomía'),
(27, 2, '¿Puede manejar la velocidad a la cual realiza su trabajo?', 'likert', 14, 'Velocidad de trabajo', 'Autonomía'),
(28, 2, '¿Puede decidir el orden en el cual realizar sus tareas cotidianas?', 'likert', 15, 'Orden de tareas', 'Autonomía'),

-- Liderazgo negativo y relaciones negativas en el trabajo
(29, 2, '¿Los cambios en el trabajo dificulta sus labores cotidianas?', 'likert', 16, 'Cambios laborales', 'Cambio'),
(30, 2, '¿Con frecuencia se presentan cambios repentinos los cuales afectan en la realización de sus actividades?', 'likert', 17, 'Cambios repentinos', 'Cambio'),
(31, 2, '¿Cuándo se presenta oportunidad de cambio en el trabajo, toman en cuenta sus ideas/aportaciones?', 'likert', 18, 'Participación en cambios', 'Participación'),

-- Funciones del puesto
(32, 3, '¿Le informan con claridad cuales son sus funciones?', 'likert', 1, 'Claridad de funciones', 'Funciones'),
(33, 3, '¿Se le explica con claridad los resultados que debe obtener en su trabajo?', 'likert', 2, 'Claridad de resultados', 'Funciones'),
(34, 3, '¿Se le explica con claridad el objetivo de su trabajo?', 'likert', 3, 'Claridad de objetivos', 'Funciones'),
(35, 3, '¿Le indican a quien debe dirigirse para resolver sus problemas o asuntos de trabajo?', 'likert', 4, 'Jerarquía clara', 'Funciones'),

-- Capacitación
(36, 3, '¿Le permiten asistir a capacitaciones relacionadas con su trabajo?', 'likert', 5, 'Acceso a capacitación', 'Capacitación'),
(37, 3, '¿Recibe capacitación útil para hacer bien su trabajo?', 'likert', 6, 'Utilidad de capacitación', 'Capacitación'),

-- Participación y manejo del cambio
(38, 3, '¿Su jefe le ayuda a organizar mejor su trabajo?', 'likert', 7, 'Organización del jefe', 'Liderazgo'),
(39, 3, '¿Su jefe tiene en cuenta sus puntos de vista y opiniones?', 'likert', 8, 'Consideración del jefe', 'Liderazgo'),
(40, 3, '¿Su jefe le comunica a tiempo la información relacionada con su trabajo?', 'likert', 9, 'Comunicación del jefe', 'Liderazgo'),
(41, 3, '¿Su jefe le da orientación suficiente para realizar su trabajo adecuadamente?', 'likert', 10, 'Orientación del jefe', 'Liderazgo'),
(42, 3, '¿Su jefe le ayuda a solucionar los problemas que se le presentan en el trabajo?', 'likert', 11, 'Apoyo del jefe', 'Liderazgo'),

-- Relaciones en el trabajo
(43, 3, '¿Confía en sus compañeros de trabajo?', 'likert', 12, 'Confianza en compañeros', 'Relaciones'),
(44, 3, '¿Entre compañeros logran solucionar los problemas de trabajo de una forma respetuosa?', 'likert', 13, 'Solución respetuosa', 'Relaciones'),
(45, 3, '¿En su trabajo le hacen sentir parte del grupo?', 'likert', 14, 'Pertenencia al grupo', 'Relaciones'),
(46, 3, '¿Recibe ayuda de sus compañeros cuando tiene que realizar un trabajo en equipo?', 'likert', 15, 'Ayuda en equipo', 'Relaciones'),
(47, 3, '¿Sus compañeros de trabajo le ayudan cuando tiene dificultades?', 'likert', 16, 'Ayuda en dificultades', 'Relaciones'),

-- Retroalimentación del desempeño
(48, 4, '¿Le informan sobre lo que hace bien o mal en el trabajo?', 'likert', 1, 'Retroalimentación', 'Desempeño'),
(49, 4, '¿La forma como le evalúan en su centro de trabajo le ayuda a mejorar su desempeño?', 'likert', 2, 'Evaluación constructiva', 'Desempeño'),

-- Reconocimiento del desempeño
(50, 4, '¿En su centro de trabajo le pagan a tiempo su salario?', 'likert', 3, 'Pago puntual', 'Reconocimiento'),
(51, 4, '¿El pago que recibe es el que merece por el trabajo realizado?', 'likert', 4, 'Pago justo', 'Reconocimiento'),
(52, 4, '¿Al obtener los resultados esperados de su trabajo le reconocen o recompensan?', 'likert', 5, 'Recompensas', 'Reconocimiento'),
(53, 4, '¿Quién realiza bien el trabajo puede crecer laboralmente dentro de su centro de trabajo?', 'likert', 6, 'Crecimiento laboral', 'Reconocimiento'),

-- Insuficiente sentido de pertenencia e inestabilidad
(54, 4, '¿Considera que su trabajo es estable?', 'likert', 7, 'Estabilidad laboral', 'Pertenencia'),
(55, 4, '¿Existe continua rotación de personal en su trabajo?', 'likert', 8, 'Rotación de personal', 'Pertenencia'),
(56, 4, '¿Siente orgullo de laborar en este centro de trabajo?', 'likert', 9, 'Orgullo laboral', 'Pertenencia'),
(57, 4, '¿Se siente comprometido con su trabajo?', 'likert', 10, 'Compromiso laboral', 'Pertenencia'),

-- Violencia
(58, 5, '¿En su trabajo puede expresarse libremente sin interrupciones?', 'likert', 1, 'Libertad de expresión', 'Violencia'),
(59, 5, '¿Recibe constantemente críticas sobre su trabajo o sobre su persona?', 'likert', 2, 'Críticas constantes', 'Violencia'),
(60, 5, '¿Recibe burlas, calumnias, difamaciones o humillaciones en su trabajo?', 'likert', 3, 'Burlas y humillaciones', 'Violencia'),
(61, 5, '¿Ignoran su presencia o es excluido de las reuniones de trabajo y toma de decisiones?', 'likert', 4, 'Exclusión social', 'Violencia'),
(62, 5, '¿Se manipulan las situaciones de trabajo para hacerle parecer un mal trabajador?', 'likert', 5, 'Manipulación laboral', 'Violencia'),
(63, 5, '¿Sus éxitos laborales son ignorados y atribuidos a otros trabajadores?', 'likert', 6, 'Robo de méritos', 'Violencia'),
(64, 5, '¿Sus oportunidades de tener un ascenso o mejora laboral son bloqueadas o impedidas?', 'likert', 7, 'Bloqueo de ascensos', 'Violencia'),
(65, 5, '¿Ha presenciado actos de violencia en su centro de trabajo?', 'likert', 8, 'Violencia presenciada', 'Violencia'),

-- Factores propios de la actividad
(66, 5, '¿Atiende a clientes o usuarios muy enojados?', 'likert', 9, 'Clientes enojados', 'Actividad'),
(67, 5, '¿Su trabajo le exige atender a personas muy necesitadas de ayuda o enfermas?', 'likert', 10, 'Personas necesitadas', 'Actividad'),
(68, 5, '¿Para hacer su trabajo debe demostrar sentimientos distintos a los suyos?', 'likert', 11, 'Trabajo emocional', 'Actividad'),
(69, 5, '¿Su trabajo le exige atender situaciones de violencia?', 'likert', 12, 'Situaciones de violencia', 'Actividad'),

-- Liderazgo de los trabajadores
(70, 5, '¿Sus subalternos comunican tarde los asuntos de trabajo?', 'likert', 13, 'Comunicación tardía subalternos', 'Liderazgo trabajadores'),
(71, 5, '¿Sus subalternos dificultan el logro de los objetivos de trabajo?', 'likert', 14, 'Obstaculización objetivos', 'Liderazgo trabajadores'),
(72, 5, '¿Sus subalternos cooperan poco en las actividades cotidianas cuando se les requiere?', 'likert', 15, 'Falta de cooperación', 'Liderazgo trabajadores'),
(73, 5, '¿Sus subalternos ignoran las sugerencias que se le hacen para mejorar su trabajo?', 'likert', 16, 'Ignorar sugerencias', 'Liderazgo trabajadores');

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