package com.hrms.backend.onboarding.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hrms.backend.onboarding.service.OnboardingMailService;

public class LoggingOnboardingMailService implements OnboardingMailService {

	private static final Logger log = LoggerFactory.getLogger(LoggingOnboardingMailService.class);

	@Override
	public void sendOnboardingEmail(String email, String activationLink) {
		log.info(
				"Onboarding email for {}: set your password using activation link {}",
				email,
				activationLink);
	}
}
