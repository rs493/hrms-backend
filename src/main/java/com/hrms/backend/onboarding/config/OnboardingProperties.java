package com.hrms.backend.onboarding.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.onboarding")
public class OnboardingProperties {

	private String activationBaseUrl = "http://localhost:3000/activate";

	private int activationTokenExpirationMinutes = 60;

	private String mailFrom = "rangaram939@gmail.com";

	public String getActivationBaseUrl() {
		return activationBaseUrl;
	}

	public void setActivationBaseUrl(String activationBaseUrl) {
		this.activationBaseUrl = activationBaseUrl;
	}

	public int getActivationTokenExpirationMinutes() {
		return activationTokenExpirationMinutes;
	}

	public void setActivationTokenExpirationMinutes(int activationTokenExpirationMinutes) {
		this.activationTokenExpirationMinutes = activationTokenExpirationMinutes;
	}

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}
}
