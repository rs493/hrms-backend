package com.hrms.backend.onboarding.service;

public interface OnboardingMailService {

	void sendOnboardingEmail(String email, String activationLink);
}
