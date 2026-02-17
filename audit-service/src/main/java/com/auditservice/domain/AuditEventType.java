package com.auditservice.domain;

public enum AuditEventType {
    CASE_STATUS_CHANGED,
    CASE_CREATED,
    CASE_ASSIGNED,
    AUTH_LOGIN_FAILED,
    ACCESS_DENIED,
    USER_REGISTERED, USER_PROMOTED, SECURITY_EVENT
}
