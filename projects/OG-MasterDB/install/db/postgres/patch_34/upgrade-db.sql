
BEGIN;
ALTER TABLE sec_fxoption ADD COLUMN exercise_type varchar(32);
UPDATE sec_fxoption SET exercise_type = 'European';    
ALTER TABLE sec_fxoption ALTER COLUMN exercise_type SET NOT NULL;    
COMMIT;
