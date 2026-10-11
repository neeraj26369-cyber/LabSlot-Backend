INSERT INTO equipment (name, description, category, quantity, location, availability_status)
SELECT 'Digital Microscope', 'Digital microscope for observing and capturing laboratory specimens.', 'Microscopy', 2, 'Biology Lab', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM equipment WHERE name = 'Digital Microscope');

INSERT INTO equipment (name, description, category, quantity, location, availability_status)
SELECT 'Digital Oscilloscope', 'Two-channel oscilloscope for measuring and viewing electrical signals.', 'Electronics', 3, 'Electronics Lab', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM equipment WHERE name = 'Digital Oscilloscope');

INSERT INTO equipment (name, description, category, quantity, location, availability_status)
SELECT 'Laboratory Centrifuge', 'Bench-top centrifuge for separating samples in balanced tubes.', 'Sample Preparation', 2, 'Chemistry Lab', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM equipment WHERE name = 'Laboratory Centrifuge');

INSERT INTO equipment (name, description, category, quantity, location, availability_status)
SELECT 'Spectrophotometer', 'Visible-light spectrophotometer for absorbance measurements.', 'Analytical Instruments', 1, 'Chemistry Lab', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM equipment WHERE name = 'Spectrophotometer');

INSERT INTO equipment (name, description, category, quantity, location, availability_status)
SELECT 'Digital Vernier Caliper', 'Digital caliper for measuring internal, external, and depth dimensions.', 'Measurement Tools', 8, 'Mechanical Lab', 'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM equipment WHERE name = 'Digital Vernier Caliper');