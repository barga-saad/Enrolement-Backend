package org.bcp.dlmsenrolment.services.impl;

import org.bcp.dlmsenrolment.services.SecurityTokenService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityTokenServiceImpl implements SecurityTokenService {
    @Override
    public Optional<String> getCurrentBearerToken() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
//            return Optional.of(jwtAuthToken.getToken().getTokenValue());
//        }
//
//        return Optional.empty();
        return null;
    }
}
