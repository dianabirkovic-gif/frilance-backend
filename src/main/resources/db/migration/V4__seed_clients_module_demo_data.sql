-- Demo data for the Clients module (FR-05), matching the design-reference
-- prototype (clients.html) so the API returns the same picture it shows.
-- Also seeds real logins for the team members named there (Аліна, Оксана,
-- Наталя, Іван) — needed to exercise FR-05's RBAC rule ("SMM sees only
-- clients assigned to them") for real, without building FR-02's agency
-- login flow. Dev logins: <name>@example.com / password123 (same hash V2
-- used for the owner).

DO $$
DECLARE
    v_owner_id    UUID := '11111111-1111-1111-1111-111111111111';
    v_pwd_hash    VARCHAR := '$2b$10$N1ipAb/05lySzDbEgOWB6.BRlrL.X//PD922YDuk1/fMD0jTOZW8C';
    v_alina_id    UUID := gen_random_uuid();
    v_oksana_id   UUID := gen_random_uuid();
    v_natalia_id  UUID := gen_random_uuid();
    v_ivan_id     UUID := gen_random_uuid();
    v_client_muse       UUID;
    v_client_kochul     UUID;
    v_client_brookside  UUID;
    v_client_oliver     UUID;
    v_client_solar      UUID := gen_random_uuid();
    v_client_wedding    UUID := gen_random_uuid();
BEGIN
    SELECT id INTO v_client_muse FROM client WHERE owner_id = v_owner_id AND name = 'MUSE''23';
    SELECT id INTO v_client_kochul FROM client WHERE owner_id = v_owner_id AND name = 'Kochul Interiors';
    SELECT id INTO v_client_brookside FROM client WHERE owner_id = v_owner_id AND name = 'Brookside Wellness';
    SELECT id INTO v_client_oliver FROM client WHERE owner_id = v_owner_id AND name = 'Кав''ярня «Оливер»';

    -- Real logins for the team members already shown on the dashboard's
    -- team-workload panel (team_member is a separate decorative read-model
    -- table, per backend CLAUDE.md's "Known simplifications" — these rows
    -- are additionally real, RBAC-checkable UserAccounts, not a rename).
    INSERT INTO user_account (id, email, password_hash, full_name, role, work_mode, agency_owner_id, created_at) VALUES
        (v_alina_id, 'alina@example.com', v_pwd_hash, 'Аліна', 'PROJECT_MANAGER', 'AGENCY', v_owner_id, now()),
        (v_oksana_id, 'oksana@example.com', v_pwd_hash, 'Оксана', 'SMM', 'AGENCY', v_owner_id, now()),
        (v_natalia_id, 'natalia@example.com', v_pwd_hash, 'Наталя', 'SMM', 'AGENCY', v_owner_id, now()),
        (v_ivan_id, 'ivan@example.com', v_pwd_hash, 'Іван', 'TARGETOLOGIST', 'AGENCY', v_owner_id, now());

    -- Two more clients from the prototype's 6-row table, not yet in V2 ------
    INSERT INTO client (id, owner_id, name, niche, assignee_id, status) VALUES
        (v_client_solar, v_owner_id, 'Сонячна станція ZK', 'Сонячна енергетика', v_ivan_id, 'NEW'),
        (v_client_wedding, v_owner_id, 'Салон весільної моди', 'Weddings', v_oksana_id, 'ARCHIVED');

    -- Full client-card fields (tariff plan, start date, service cost,
    -- contact person, cooperation stage) + real assignees for all 6 --------
    UPDATE client SET
        assignee_id = v_alina_id, tariff_plan = 'Стандарт',
        cooperation_start_date = CURRENT_DATE - INTERVAL '3 months',
        service_cost = 18000, contact_name = 'Роман Ковач', contact_role = 'Власник закладу',
        stage = 'PAYMENT'
        WHERE id = v_client_oliver;

    UPDATE client SET
        niche = 'Beauty-простір', assignee_id = v_oksana_id, tariff_plan = 'Преміум',
        cooperation_start_date = CURRENT_DATE - INTERVAL '8 months',
        service_cost = 32000, contact_name = 'Наталія Вовк', contact_role = 'Засновниця',
        stage = 'REPORT'
        WHERE id = v_client_muse;

    UPDATE client SET
        assignee_id = v_oksana_id, tariff_plan = 'Стандарт',
        cooperation_start_date = CURRENT_DATE - INTERVAL '5 months',
        service_cost = 24000, contact_name = 'Павло Кочул', contact_role = 'Власник майстерні',
        stage = 'REPORT'
        WHERE id = v_client_kochul;

    UPDATE client SET
        assignee_id = v_natalia_id, tariff_plan = 'Преміум',
        cooperation_start_date = CURRENT_DATE - INTERVAL '2 months',
        service_cost = 41000, contact_name = 'Юлія Стах', contact_role = 'Маркетинг-менеджер',
        stage = 'WORK_STARTED'
        WHERE id = v_client_brookside;

    UPDATE client SET
        cooperation_start_date = CURRENT_DATE - INTERVAL '2 days',
        service_cost = NULL, contact_name = 'Олег Гриців', contact_role = 'Директор',
        stage = 'BRIEF'
        WHERE id = v_client_solar;

    UPDATE client SET
        tariff_plan = 'Стандарт', cooperation_start_date = CURRENT_DATE - INTERVAL '11 months',
        service_cost = 0, contact_name = 'Марина Коваль', contact_role = 'Власниця салону',
        stage = 'REPORT'
        WHERE id = v_client_wedding;

    -- Next scheduled post for Kochul (mockup's table shows "Вт, карусель") --
    -- Muse/Brookside/Oliver's posts already exist in V2's this-week content
    -- plan and double as their "next post"; solar/wedding have none (mockup:
    -- "Не заплановано" / "—").
    INSERT INTO post (id, owner_id, client_id, client_name, title, scheduled_date, status) VALUES
        (gen_random_uuid(), v_owner_id, v_client_kochul, 'Kochul Interiors', 'карусель', CURRENT_DATE + 3, 'DRAFT');

    -- Per-client activity feed for the drawer's "Останні події" tab --------
    INSERT INTO event_log_entry (id, owner_id, client_id, occurred_at, actor_initials, actor_name, description, tag, amount) VALUES
        (gen_random_uuid(), v_owner_id, v_client_oliver, now() - INTERVAL '5 days', 'АЛ', 'Аліна', 'Надіслала кошторис', 'CLIENT', NULL),
        (gen_random_uuid(), v_owner_id, v_client_oliver, now() - INTERVAL '8 days', 'ДМ', 'Ви', 'Провели дзвінок-знайомство', 'CLIENT', NULL),
        (gen_random_uuid(), v_owner_id, v_client_oliver, now() - INTERVAL '9 days', 'РК', 'Роман', 'Залишив заявку на сайті', 'CLIENT', NULL),

        (gen_random_uuid(), v_owner_id, v_client_muse, now() - INTERVAL '2 hours', 'ОК', 'Оксана', 'Опублікувала Reels', 'CONTENT', NULL),
        (gen_random_uuid(), v_owner_id, v_client_muse, now() - INTERVAL '1 day', 'НВ', 'Наталія', 'Оплатила рахунок', 'MONEY', 32000),
        (gen_random_uuid(), v_owner_id, v_client_muse, now() - INTERVAL '3 days', 'ОК', 'Оксана', 'Надіслала контент-план на тиждень', 'CONTENT', NULL),

        (gen_random_uuid(), v_owner_id, v_client_kochul, now() - INTERVAL '1 day', 'ОК', 'Оксана', 'Підготувала карусель', 'CONTENT', NULL),
        (gen_random_uuid(), v_owner_id, v_client_kochul, now() - INTERVAL '3 days', 'ПК', 'Павло', 'Надіслав фото нової колекції', 'CLIENT', NULL),

        (gen_random_uuid(), v_owner_id, v_client_brookside, now() - INTERVAL '10 hours', 'НЛ', 'Наталя', 'Попередила про вичерпання бюджету', 'MONEY', NULL),
        (gen_random_uuid(), v_owner_id, v_client_brookside, now() - INTERVAL '2 days', 'ЮС', 'Юлія', 'Погодила нові креативи', 'CONTENT', NULL),

        (gen_random_uuid(), v_owner_id, v_client_solar, now() - INTERVAL '2 minutes', 'ОГ', 'Олег', 'Залишив заявку через сайт', 'CLIENT', NULL),

        (gen_random_uuid(), v_owner_id, v_client_wedding, now() - INTERVAL '2 months', 'ДМ', 'Ви', 'Закрили співпрацю', 'CLIENT', NULL),
        (gen_random_uuid(), v_owner_id, v_client_wedding, now() - INTERVAL '2 months' - INTERVAL '1 hour', 'ОК', 'Оксана', 'Передала фінальні матеріали', 'CLIENT', NULL);
END $$;
