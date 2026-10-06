package com.hrms.backend.identity.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.hrms.backend.identity.serviceImpl.SystemDataBootstrapService;

@Component
@ConditionalOnProperty(prefix = "app.bootstrap", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SystemDataBootstrapRunner implements ApplicationRunner {

	private final SystemDataBootstrapService systemDataBootstrapService;

	public SystemDataBootstrapRunner(SystemDataBootstrapService systemDataBootstrapService) {
		this.systemDataBootstrapService = systemDataBootstrapService;
	}

	@Override
	public void run(ApplicationArguments args) {
		systemDataBootstrapService.bootstrap();
	}
}
