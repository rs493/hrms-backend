package com.hrms.backend.onboarding.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hrms.backend.onboarding.service.OnboardingMailService;
import com.hrms.backend.onboarding.serviceImpl.SmtpOnboardingMailService;

@Component
public class MailTransportStartupLogger implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(MailTransportStartupLogger.class);

	private final OnboardingMailService onboardingMailService;
	private final Environment environment;
	private final JavaMailSender mailSender;

	public MailTransportStartupLogger(
			OnboardingMailService onboardingMailService,
			Environment environment,
			JavaMailSender mailSender) {
		this.onboardingMailService = onboardingMailService;
		this.environment = environment;
		this.mailSender = mailSender;
	}

	@Override
	public void run(ApplicationArguments args) {
		boolean smtpBeanActive = onboardingMailService instanceof SmtpOnboardingMailService;
		String username = environment.getProperty("spring.mail.username", "");
		String host = environment.getProperty("spring.mail.host", "");
		boolean passwordPresent = StringUtils.hasText(environment.getProperty("spring.mail.password"));

		log.info(
				"Onboarding mail transport={} smtpActive={} host={} username={} passwordConfigured={}",
				onboardingMailService.getClass().getSimpleName(),
				smtpBeanActive,
				host,
				username,
				passwordPresent);

		if (!smtpBeanActive) {
			log.warn(
					"Onboarding emails will NOT be sent by SMTP. Ensure config/mail-secrets.yml is loaded "
							+ "(spring.mail.host + spring.mail.password), start the app from hrms-backend, "
							+ "then restart.");
			return;
		}

		if (mailSender instanceof JavaMailSenderImpl sender) {
			try {
				sender.testConnection();
				log.info("SMTP connection test succeeded for {}@{}:{}", username, host, sender.getPort());
			} catch (Exception exception) {
				Throwable root = NestedExceptionUtils.getMostSpecificCause(exception);
				log.error(
						"SMTP connection test FAILED for {}@{}:{} — {}: {}. "
								+ "Create a new Gmail App Password and update config/mail-secrets.yml.",
						username,
						host,
						sender.getPort(),
						root.getClass().getSimpleName(),
						root.getMessage());
			}
		}
	}
}
