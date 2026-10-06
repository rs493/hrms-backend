package com.hrms.backend.onboarding.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

import com.hrms.backend.onboarding.config.OnboardingProperties;
import com.hrms.backend.onboarding.service.OnboardingMailService;

public class SmtpOnboardingMailService implements OnboardingMailService {

	private static final Logger log = LoggerFactory.getLogger(SmtpOnboardingMailService.class);

	private final JavaMailSender mailSender;
	private final OnboardingProperties onboardingProperties;

	public SmtpOnboardingMailService(JavaMailSender mailSender, OnboardingProperties onboardingProperties) {
		this.mailSender = mailSender;
		this.onboardingProperties = onboardingProperties;
	}

	@Override
	public void sendOnboardingEmail(String email, String activationLink) {
		SimpleMailMessage message = new SimpleMailMessage();
		String from = onboardingProperties.getMailFrom();
		if (StringUtils.hasText(from)) {
			message.setFrom(from);
		}
		message.setTo(email);
		message.setSubject("HRMS account onboarding");
		message.setText("""
				Welcome to HRMS.

				Your HRMS account has been created.

				Please click the link below to create your password and activate your account:

				%s

				This activation link will expire after the configured period.
				""".formatted(activationLink));
		try {
			mailSender.send(message);
			log.info("Onboarding email sent via SMTP to {}", email);
		} catch (MailException exception) {
			Throwable root = NestedExceptionUtils.getMostSpecificCause(exception);
			log.error(
					"SMTP send failed for {}. rootCause={}: {}. "
							+ "Verify Gmail App Password for the SMTP username and that 2-Step Verification is on.",
					email,
					root.getClass().getSimpleName(),
					root.getMessage(),
					exception);
			// Do not rethrow — onboarding DB work is already committed; email can be retried later.
		}
	}
}
