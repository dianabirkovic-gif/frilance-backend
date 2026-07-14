-- Core schema for the dashboard vertical slice.
-- Every tenant-scoped table carries owner_id: an agency owner's user id for
-- agency data, or a freelancer's own user id (NFR-04 isolation key).

CREATE TABLE user_account (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    work_mode       VARCHAR(32)  NOT NULL,
    agency_owner_id UUID,
    created_at      TIMESTAMPTZ  NOT NULL
);

CREATE TABLE agency_config (
    owner_id    UUID PRIMARY KEY,
    agency_name VARCHAR(255) NOT NULL
);

CREATE TABLE client (
    id          UUID PRIMARY KEY,
    owner_id    UUID NOT NULL,
    name        VARCHAR(255) NOT NULL,
    niche       VARCHAR(255),
    assignee_id UUID,
    status      VARCHAR(32) NOT NULL
);
CREATE INDEX idx_client_owner_id ON client (owner_id);

CREATE TABLE post (
    id             UUID PRIMARY KEY,
    owner_id       UUID NOT NULL,
    client_id      UUID NOT NULL,
    client_name    VARCHAR(255) NOT NULL,
    title          VARCHAR(255) NOT NULL,
    scheduled_date DATE NOT NULL,
    status         VARCHAR(32) NOT NULL
);
CREATE INDEX idx_post_owner_id ON post (owner_id);
CREATE INDEX idx_post_owner_scheduled ON post (owner_id, scheduled_date);

CREATE TABLE finance_entry (
    id         UUID PRIMARY KEY,
    owner_id   UUID NOT NULL,
    type       VARCHAR(32) NOT NULL,
    amount     NUMERIC(14, 2) NOT NULL,
    currency   VARCHAR(8) NOT NULL,
    entry_date DATE NOT NULL
);
CREATE INDEX idx_finance_entry_owner_id ON finance_entry (owner_id);
CREATE INDEX idx_finance_entry_owner_date ON finance_entry (owner_id, entry_date);

CREATE TABLE monthly_goal (
    id               UUID PRIMARY KEY,
    owner_id         UUID NOT NULL,
    goal_month       VARCHAR(7) NOT NULL,
    revenue_goal     NUMERIC(14, 2) NOT NULL,
    tax_reserve_goal NUMERIC(14, 2) NOT NULL
);
CREATE UNIQUE INDEX idx_monthly_goal_owner_month ON monthly_goal (owner_id, goal_month);

CREATE TABLE team_member (
    id           UUID PRIMARY KEY,
    owner_id     UUID NOT NULL,
    name         VARCHAR(255) NOT NULL,
    role         VARCHAR(32) NOT NULL,
    load_percent INT NOT NULL,
    client_count INT NOT NULL
);
CREATE INDEX idx_team_member_owner_id ON team_member (owner_id);

CREATE TABLE event_log_entry (
    id             UUID PRIMARY KEY,
    owner_id       UUID NOT NULL,
    occurred_at    TIMESTAMPTZ NOT NULL,
    actor_initials VARCHAR(8) NOT NULL,
    actor_name     VARCHAR(255) NOT NULL,
    description    VARCHAR(500) NOT NULL,
    tag            VARCHAR(32) NOT NULL,
    amount         NUMERIC(14, 2)
);
CREATE INDEX idx_event_log_owner_occurred ON event_log_entry (owner_id, occurred_at DESC);

CREATE TABLE attention_item (
    id             UUID PRIMARY KEY,
    owner_id       UUID NOT NULL,
    severity       VARCHAR(16) NOT NULL,
    title          VARCHAR(255) NOT NULL,
    subtitle       VARCHAR(500) NOT NULL,
    meta_label     VARCHAR(64) NOT NULL,
    meta_is_danger BOOLEAN NOT NULL
);
CREATE INDEX idx_attention_item_owner_id ON attention_item (owner_id);