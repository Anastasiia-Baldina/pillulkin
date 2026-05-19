CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_reference_medicine_name_trgm ON reference_medicine USING GIN (name gin_trgm_ops);
CREATE INDEX idx_reference_medicine_active_substance_trgm ON reference_medicine USING GIN (active_substance gin_trgm_ops);
CREATE INDEX idx_reference_medicine_indications_trgm ON reference_medicine USING GIN (indications gin_trgm_ops);