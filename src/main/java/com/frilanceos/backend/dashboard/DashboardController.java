package com.frilanceos.backend.dashboard;

import com.frilanceos.backend.common.security.SecurityUser;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getOverview(@AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(dashboardService.getOverview(currentUser));
    }
}