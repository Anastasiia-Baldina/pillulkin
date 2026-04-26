CREATE TABLE reference_medicine (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    dosage VARCHAR(255),
    form VARCHAR(100),
    active_substance TEXT,
    indications TEXT,
    contraindications TEXT,
    category VARCHAR(255)
);

CREATE TABLE patient (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE patient_profile (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL UNIQUE REFERENCES patient(id) ON DELETE CASCADE,
    name VARCHAR(255),
    age INTEGER,
    allergies TEXT,
    contraindications TEXT,
    notes TEXT
);

CREATE TABLE patient_symptom (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
    symptom VARCHAR(500) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE patient_medicine (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
    reference_medicine_id BIGINT NOT NULL REFERENCES reference_medicine(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(patient_id, reference_medicine_id)
);

CREATE TABLE doctor_access_code (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patient(id) ON DELETE CASCADE,
    code_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE doctor_access_session (
    id BIGSERIAL PRIMARY KEY,
    doctor_access_code_id BIGINT NOT NULL REFERENCES doctor_access_code(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patient_profile_patient_id ON patient_profile(patient_id);
CREATE INDEX idx_patient_symptom_patient_id ON patient_symptom(patient_id);
CREATE INDEX idx_patient_medicine_patient_id ON patient_medicine(patient_id);
CREATE INDEX idx_doctor_access_code_patient_id ON doctor_access_code(patient_id);
CREATE INDEX idx_doctor_access_code_status ON doctor_access_code(status);
CREATE INDEX idx_doctor_access_session_token ON doctor_access_session(token);
