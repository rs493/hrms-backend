package com.hrms.backend.identity.bootstrap;

import java.util.List;

public final class InitialPermissionCatalog {

	public static final List<String> PERMISSION_CODES = List.of(
			"PROFILE_VIEW_SELF",
			"PROFILE_UPDATE_SELF",
			"ATTENDANCE_MARK",
			"LEAVE_APPLY",
			"TEAM_VIEW",
			"LEAVE_REVIEW_TEAM",
			"ADMIN_CREATE",
			"HR_CREATE",
			"MANAGER_CREATE",
			"EMPLOYEE_CREATE",
			"EMPLOYEE_VIEW",
			"EMPLOYEE_UPDATE",
			"SALARY_MANAGE",
			"ROLE_MANAGE");

	private InitialPermissionCatalog() {
	}
}
