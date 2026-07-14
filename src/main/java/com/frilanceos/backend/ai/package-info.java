/**
 * FR-15 (AI client report generation) and FR-16 (AI content ideas), both via
 * an external AI provider. Not implemented yet — when it is, keep the
 * provider call behind an interface in this package so the concrete
 * provider (Anthropic or otherwise) stays swappable, per NFR-17's cost-control
 * limit which must be enforced here before any provider call.
 */
package com.frilanceos.backend.ai;