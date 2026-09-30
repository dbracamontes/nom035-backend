ALTER TABLE company
    ADD COLUMN ciudad VARCHAR(100) NULL,
    ADD COLUMN codigo_postal VARCHAR(20) NULL,
    ADD COLUMN telefono VARCHAR(30) NULL,
    ADD COLUMN correo_electronico VARCHAR(254) NULL;