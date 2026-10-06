package com.hrms.backend.identity.serviceImpl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hrms.backend.identity.service.AuthorizationService;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

	private static final String ROLE_PREFIX = "ROLE_";

	private static final String PERMISSION_PREFIX = "PERM_";

	@Override
	public boolean isAuthenticated() {
		Authentication authentication = currentAuthentication();
		return authentication != null && authentication.isAuthenticated();
	}

	@Override
	public boolean hasRole(String role) {
		if (!StringUtils.hasText(role) || !isAuthenticated()) {
			return false;
		}
		return hasAuthority(ROLE_PREFIX + role);
	}

	@Override
	public boolean hasPermission(String permission) {
		if (!StringUtils.hasText(permission) || !isAuthenticated()) {
			return false;
		}
		return hasAuthority(PERMISSION_PREFIX + permission);
	}

	@Override
	public void requireRole(String role) {
		if (!hasRole(role)) {
			throw new AccessDeniedException("Required role is missing: " + role);
		}
	}

	@Override
	public void requirePermission(String permission) {
		if (!hasPermission(permission)) {
			throw new AccessDeniedException("Required permission is missing: " + permission);
		}
	}

	@Override
	public Integer getCurrentUserId() {
		Authentication authentication = currentAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| !StringUtils.hasText(authentication.getName())) {
			throw new AuthenticationCredentialsNotFoundException("No authenticated user is available.");
		}
		try {
			return Integer.valueOf(authentication.getName());
		} catch (NumberFormatException exception) {
			throw new AuthenticationCredentialsNotFoundException("Authenticated user id is invalid.");
		}
	}

	private Authentication currentAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	private boolean hasAuthority(String authority) {
		Authentication authentication = currentAuthentication();
		if (authentication == null) {
			return false;
		}
		for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
			if (authority.equals(grantedAuthority.getAuthority())) {
				return true;
			}
		}
		return false;
	}
}
