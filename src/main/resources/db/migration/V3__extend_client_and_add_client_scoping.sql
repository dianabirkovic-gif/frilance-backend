-- FR-05 (Clients module): the dashboard vertical slice only needed
-- name/niche/assignee/status on client (see Client.java's original
-- Javadoc); the full client card adds tariff plan, cooperation start date,
-- service cost, contact person and cooperation stage.
ALTER TABLE client
    ADD COLUMN tariff_plan            VARCHAR(255),
    ADD COLUMN cooperation_start_date DATE,
    ADD COLUMN service_cost           NUMERIC(14, 2),
    ADD COLUMN contact_name           VARCHAR(255),
    ADD COLUMN contact_role           VARCHAR(255),
    ADD COLUMN stage                  VARCHAR(32) NOT NULL DEFAULT 'BRIEF';

-- Lets the Clients module scope the shared event ledger to one client (for
-- the client card's activity feed) without touching the dashboard's
-- existing generic feed, which stays unfiltered by client_id.
ALTER TABLE event_log_entry ADD COLUMN client_id UUID;
CREATE INDEX idx_event_log_owner_client ON event_log_entry (owner_id, client_id, occurred_at DESC);
