-- Demo tenant matching the original static prototype (dashboard.html) so the
-- API returns recognizably the same picture as the design reference.
-- Dev login: diana@example.com / password123

DO $$
DECLARE
    v_owner_id UUID := '11111111-1111-1111-1111-111111111111';
    v_month_start DATE := date_trunc('month', CURRENT_DATE)::date;
    v_prev_month_start DATE := date_trunc('month', CURRENT_DATE - INTERVAL '1 month')::date;
    v_week_monday DATE := date_trunc('week', CURRENT_DATE)::date;
    v_client_muse UUID := gen_random_uuid();
    v_client_kochul UUID := gen_random_uuid();
    v_client_brookside UUID := gen_random_uuid();
    v_client_oliver UUID := gen_random_uuid();
BEGIN

    INSERT INTO user_account (id, email, password_hash, full_name, role, work_mode, agency_owner_id, created_at)
    VALUES (
        v_owner_id,
        'diana@example.com',
        '$2b$10$N1ipAb/05lySzDbEgOWB6.BRlrL.X//PD922YDuk1/fMD0jTOZW8C', -- password123
        'Діана М.',
        'OWNER',
        'AGENCY',
        v_owner_id,
        now()
    );

    INSERT INTO agency_config (owner_id, agency_name) VALUES (v_owner_id, 'Студія Діани');

    -- Clients ------------------------------------------------------------
    INSERT INTO client (id, owner_id, name, niche, assignee_id, status) VALUES
        (v_client_muse, v_owner_id, 'MUSE''23', 'Подієвий маркетинг', NULL, 'ACTIVE'),
        (v_client_kochul, v_owner_id, 'Kochul Interiors', 'Інтер''єри', NULL, 'ACTIVE'),
        (v_client_brookside, v_owner_id, 'Brookside Wellness', 'Wellness', NULL, 'ATTENTION'),
        (v_client_oliver, v_owner_id, 'Кав''ярня «Оливер»', 'HoReCa', NULL, 'ATTENTION');

    -- Team workload --------------------------------------------------------
    INSERT INTO team_member (id, owner_id, name, role, load_percent, client_count) VALUES
        (gen_random_uuid(), v_owner_id, 'Оксана', 'SMM', 85, 6),
        (gen_random_uuid(), v_owner_id, 'Іван', 'TARGETOLOGIST', 60, 4),
        (gen_random_uuid(), v_owner_id, 'Наталя', 'SMM', 40, 3),
        (gen_random_uuid(), v_owner_id, 'Аліна', 'PROJECT_MANAGER', 95, 12);

    -- Attention items --------------------------------------------------------
    INSERT INTO attention_item (id, owner_id, severity, title, subtitle, meta_label, meta_is_danger) VALUES
        (gen_random_uuid(), v_owner_id, 'HIGH', 'Кав''ярня «Оливер», Ужгород', 'Лід мовчить після кошторису', '5 днів', true),
        (gen_random_uuid(), v_owner_id, 'HIGH', 'MUSE''23', 'Рахунок не оплачено', '₴12 000', true),
        (gen_random_uuid(), v_owner_id, 'MID', 'Kochul Interiors', 'Контент-план на завтра не готовий', 'Завтра', false),
        (gen_random_uuid(), v_owner_id, 'MID', 'Brookside Wellness', 'Рекламний бюджет вичерпано на 90%', '90%', false);

    -- Content plan for the current week (Mon-Fri) ----------------------------
    INSERT INTO post (id, owner_id, client_id, client_name, title, scheduled_date, status) VALUES
        (gen_random_uuid(), v_owner_id, v_client_muse, 'MUSE''23', 'Reels', v_week_monday + 0, 'READY'),
        (gen_random_uuid(), v_owner_id, v_client_kochul, 'Kochul Interiors', 'карусель', v_week_monday + 1, 'DRAFT'),
        (gen_random_uuid(), v_owner_id, v_client_brookside, 'Brookside', 'сторіз', v_week_monday + 2, 'REVIEW'),
        (gen_random_uuid(), v_owner_id, v_client_oliver, 'Оливер Кафе', 'пост', v_week_monday + 3, 'DRAFT'),
        (gen_random_uuid(), v_owner_id, v_client_muse, 'MUSE''23', 'карусель', v_week_monday + 4, 'READY');

    -- Finance: current month income spread across several days for the
    -- revenue chart series, team payouts and tax reserve --------------------
    INSERT INTO finance_entry (id, owner_id, type, amount, currency, entry_date) VALUES
        (gen_random_uuid(), v_owner_id, 'INCOME', 18000, 'UAH', v_month_start + 1),
        (gen_random_uuid(), v_owner_id, 'INCOME', 22000, 'UAH', v_month_start + 4),
        (gen_random_uuid(), v_owner_id, 'INCOME', 25000, 'UAH', v_month_start + 7),
        (gen_random_uuid(), v_owner_id, 'INCOME', 20000, 'UAH', v_month_start + 11),
        (gen_random_uuid(), v_owner_id, 'INCOME', 28000, 'UAH', v_month_start + 14),
        (gen_random_uuid(), v_owner_id, 'INCOME', 24000, 'UAH', v_month_start + 18),
        (gen_random_uuid(), v_owner_id, 'INCOME', 26000, 'UAH', v_month_start + 21),
        (gen_random_uuid(), v_owner_id, 'INCOME', 21200, 'UAH', v_month_start + 24),
        (gen_random_uuid(), v_owner_id, 'PAYOUT', 52400, 'UAH', v_month_start + 20),
        (gen_random_uuid(), v_owner_id, 'PAYOUT', 44000, 'UAH', v_month_start + 22),
        (gen_random_uuid(), v_owner_id, 'TAX', 18420, 'UAH', v_month_start + 25),
        (gen_random_uuid(), v_owner_id, 'INCOME', 163900, 'UAH', v_prev_month_start + 10);

    INSERT INTO monthly_goal (id, owner_id, goal_month, revenue_goal, tax_reserve_goal) VALUES
        (gen_random_uuid(), v_owner_id, to_char(CURRENT_DATE, 'YYYY-MM'), 225000, 92100);

    -- Event ledger (most recent activity) ------------------------------------
    INSERT INTO event_log_entry (id, owner_id, occurred_at, actor_initials, actor_name, description, tag, amount) VALUES
        (gen_random_uuid(), v_owner_id, (CURRENT_DATE + TIME '14:32')::timestamptz, 'АЛ', 'Аліна', 'Оплатила рахунок MUSE''23', 'MONEY', 12000),
        (gen_random_uuid(), v_owner_id, (CURRENT_DATE + TIME '13:05')::timestamptz, 'ОК', 'Оксана', 'Опублікувала Reels для Kochul Interiors', 'CONTENT', NULL),
        (gen_random_uuid(), v_owner_id, (CURRENT_DATE + TIME '11:47')::timestamptz, 'ДМ', 'Ви', 'Додали нового клієнта «Brookside Wellness»', 'CLIENT', NULL),
        (gen_random_uuid(), v_owner_id, (CURRENT_DATE + TIME '10:20')::timestamptz, 'ІВ', 'Іван', 'Списано бюджет на таргет — MUSE''23', 'MONEY', -3400);

END $$;