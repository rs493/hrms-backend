package com.hrms.backend.onboarding.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Evaluated when beans are created — after {@code config/mail-secrets.yml} has been loaded.
 * SMTP is enabled when host and password are both present.
 */
public class SmtpMailEnabledCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Environment environment = context.getEnvironment();
		boolean hostConfigured = StringUtils.hasText(environment.getProperty("spring.mail.host"));
		boolean passwordConfigured = StringUtils.hasText(environment.getProperty("spring.mail.password"));
		return hostConfigured && passwordConfigured;
	}
}
