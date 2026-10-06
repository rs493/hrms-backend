package com.hrms.backend.identity.service;

public interface AuthorizationService {

	boolean isAuthenticated();

	boolean hasRole(String role);

	boolean hasPermission(String permission);

	void requireRole(String role);

	void requirePermission(String permission);

	Integer getCurrentUserId();
}
