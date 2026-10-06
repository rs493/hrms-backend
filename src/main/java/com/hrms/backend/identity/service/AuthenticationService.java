package com.hrms.backend.identity.service;

import com.hrms.backend.identity.dto.LoginRequest;
import com.hrms.backend.identity.dto.LoginResponse;

public interface AuthenticationService {

	LoginResponse login(LoginRequest request);
}
