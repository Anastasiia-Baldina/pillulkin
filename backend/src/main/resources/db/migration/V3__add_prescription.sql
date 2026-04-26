CREATE TABLE prescription (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patient(id),
    reference_medicine_id BIGINT NOT NULL REFERENCES reference_medicine(id),
    dosage_instructions TEXT,
    frequency INTEGER,
    meal_timing VARCHAR(50),
    time_offset VARCHAR(50),
    custom_instructions TEXT,
    prescribed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_prescription_patient_id ON prescription(patient_id);
CREATE INDEX idx_prescription_status ON prescription(status);
