package com.frilanceos.backend.contentplan;

/** FR-08 lifecycle: planned -> in progress -> shot/published, simplified for the dashboard's weekly strip. */
public enum PostStatus {
    DRAFT,
    REVIEW,
    READY,
    PUBLISHED
}