package ma.safar.morocco.security.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.security.entity.AuditLog;
import ma.safar.morocco.security.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditService.getRecentLogs(100));
    }

    @GetMapping("/audit/recent")
    public ResponseEntity<List<AuditLog>> getRecentLogs(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(auditService.getRecentLogs(limit));
    }

    @GetMapping("/audit/failures")
    public ResponseEntity<List<AuditLog>> getFailedActions() {
        return ResponseEntity.ok(auditService.getFailedActions());
    }

    @GetMapping("/audit/user/{userId}")
    public ResponseEntity<List<AuditLog>> getLogsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.getUserLogs(userId));
    }

    @GetMapping("/audit/suspicious/{userId}")
    public ResponseEntity<Map<String, Boolean>> checkSuspiciousActivity(@PathVariable Long userId) {
        boolean isSuspicious = auditService.detectSuspiciousActivity(userId);
        return ResponseEntity.ok(Map.of("suspicious", isSuspicious));
    }
}