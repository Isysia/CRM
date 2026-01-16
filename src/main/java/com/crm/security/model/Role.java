package com.crm.security.model;

public enum Role {
    ROLE_USER,      // Read-only access
    ROLE_MANAGER,   // Can create/edit customers, offers, tasks
    ROLE_ADMIN      // Full access to everything
}