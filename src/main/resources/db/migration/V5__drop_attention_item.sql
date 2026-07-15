-- The dashboard's "Потребують уваги" panel is no longer backed by a
-- directly-seeded placeholder table (see attention/AttentionItem.java's
-- former Javadoc and backend CLAUDE.md "Known simplifications") — now that
-- the Clients module exists, DashboardService computes it directly from
-- Client rows with status = ATTENTION instead.
DROP TABLE attention_item;
