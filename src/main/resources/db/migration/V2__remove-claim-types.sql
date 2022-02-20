-- Move information whether an actor supports a cause from `claim_types` to `claims` table

ALTER TABLE claims ADD COLUMN cause_supports boolean;

UPDATE claims
SET cause_supports = claim_types.is_supporting
FROM claim_types
WHERE claims.type = claim_types.id;

ALTER TABLE claim_types ALTER COLUMN is_supporting SET NOT NULL;

-- Drop `claim_types` table. Remove `claims.type` column that referenced it.
-- `claim_reasons` table is unused, but has a foreign key relationship to `claim_types` too.

DROP TABLE claim_reasons;
ALTER TABLE claims DROP COLUMN type;
DROP TABLE claim_types;
