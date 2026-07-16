-- FR-05: the client-creation form now requires a contact person's phone and
-- email alongside their name. Columns stay nullable at the DB level —
-- existing clients (and contact_name/contact_role before them) predate this
-- requirement, so it's enforced at the request-validation layer for new
-- writes (CreateClientRequest/UpdateClientRequest), not as a schema
-- constraint that would need a backfill.
ALTER TABLE client
    ADD COLUMN contact_phone VARCHAR(50),
    ADD COLUMN contact_email VARCHAR(255);
