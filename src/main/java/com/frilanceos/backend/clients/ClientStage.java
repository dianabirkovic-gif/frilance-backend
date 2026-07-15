package com.frilanceos.backend.clients;

/**
 * Cooperation-stage timeline shown on the client card (design reference:
 * clients.html's Ledger Line timeline). Set explicitly by the user, like
 * {@link ClientStatus} — there is no automatic progression rule.
 */
public enum ClientStage {
    BRIEF,
    ESTIMATE,
    PAYMENT,
    WORK_STARTED,
    REPORT
}
