package com.hrms.backend.onboarding.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hrms.backend.onboarding.dto.ActivationRequest;
import com.hrms.backend.onboarding.dto.ActivationResponse;
import com.hrms.backend.onboarding.dto.OnboardingRequest;
import com.hrms.backend.onboarding.dto.OnboardingResponse;
import com.hrms.backend.onboarding.service.OnboardingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class OnboardingController {

	private final OnboardingService onboardingService;

	public OnboardingController(OnboardingService onboardingService) {
		this.onboardingService = onboardingService;
	}

	@PostMapping("/onboarding")
	@ResponseStatus(HttpStatus.CREATED)
	public OnboardingResponse onboard(@Valid @RequestBody OnboardingRequest request) {
		return onboardingService.onboard(request);
	}

	@PostMapping("/onboarding/activate")
	@ResponseStatus(HttpStatus.OK)
	public ActivationResponse activate(@Valid @RequestBody ActivationRequest request) {
		return onboardingService.activate(request);
	}
}
