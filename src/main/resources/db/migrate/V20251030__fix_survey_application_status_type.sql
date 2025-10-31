-- Cambia el tipo de la columna 'status' en survey_application de ENUM a VARCHAR(32)
ALTER TABLE survey_application CHANGE status status VARCHAR(32);
