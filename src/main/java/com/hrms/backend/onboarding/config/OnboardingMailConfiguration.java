package com.hrms.backend.onboarding.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import com.hrms.backend.onboarding.service.OnboardingMailService;
import com.hrms.backend.onboarding.serviceImpl.LoggingOnboardingMailService;
import com.hrms.backend.onboarding.serviceImpl.SmtpOnboardingMailService;

@Configuration
@ConditionalOnClass(JavaMailSender.class)
public class OnboardingMailConfiguration {

	private static final Logger log = LoggerFactory.getLogger(OnboardingMailConfiguration.class);

	@Bean
	@Primary
	@Conditional(SmtpMailEnabledCondition.class)
	OnboardingMailService smtpOnboardingMailService(
			JavaMailSender mailSender,
			OnboardingProperties onboardingProperties) {
		normalizeAppPassword(mailSender);
		log.info("Registering SMTP onboarding mail service (from={})", onboardingProperties.getMailFrom());
		return new SmtpOnboardingMailService(mailSender, onboardingProperties);
	}

	@Bean
	@ConditionalOnMissingBean(OnboardingMailService.class)
	OnboardingMailService loggingOnboardingMailService() {
		log.warn("Registering logging onboarding mail service — SMTP is not active.");
		return new LoggingOnboardingMailService();
	}

	private static void normalizeAppPassword(JavaMailSender mailSender) {
		if (!(mailSender instanceof JavaMailSenderImpl sender)) {
			return;
		}
		String password = sender.getPassword();
		if (!StringUtils.hasText(password) || !password.contains(" ")) {
			return;
		}
		sender.setPassword(password.replace(" ", ""));
		log.info("Normalized spring.mail.password by removing spaces for SMTP authentication.");
	}
}
