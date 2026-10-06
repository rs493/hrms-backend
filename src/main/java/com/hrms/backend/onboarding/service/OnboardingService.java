package com.hrms.backend.onboarding.service;

import com.hrms.backend.onboarding.dto.ActivationRequest;
import com.hrms.backend.onboarding.dto.ActivationResponse;
import com.hrms.backend.onboarding.dto.OnboardingRequest;
import com.hrms.backend.onboarding.dto.OnboardingResponse;

public interface OnboardingService {

	OnboardingResponse onboard(OnboardingRequest request);

	ActivationResponse activate(ActivationRequest request);
}
