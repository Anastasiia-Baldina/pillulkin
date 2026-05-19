ALTER TABLE patient_medicine ALTER COLUMN reference_medicine_id DROP NOT NULL;

ALTER TABLE patient_medicine ADD COLUMN IF NOT EXISTS medicine_name VARCHAR(500);
ALTER TABLE patient_medicine ADD COLUMN IF NOT EXISTS medicine_dosage VARCHAR(255);
ALTER TABLE patient_medicine ADD COLUMN IF NOT EXISTS medicine_form VARCHAR(100);
ALTER TABLE patient_medicine ADD COLUMN IF NOT EXISTS medicine_active_substance TEXT;

UPDATE patient_medicine SET medicine_name = rm.name,
                           medicine_dosage = rm.dosage,
                           medicine_form = rm.form,
                           medicine_active_substance = rm.active_substance
FROM reference_medicine rm
WHERE patient_medicine.reference_medicine_id = rm.id AND patient_medicine.medicine_name IS NULL;