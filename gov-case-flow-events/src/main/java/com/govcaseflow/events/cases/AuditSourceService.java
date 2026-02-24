package com.govcaseflow.events.cases;

public enum AuditSourceService {
    AUTH_SERVICE("auth-service"),
    CASE_SERVICE("case-service"),
    NOTIFICATION_SERVICE("notification-service");

    private final String value;

    AuditSourceService(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
