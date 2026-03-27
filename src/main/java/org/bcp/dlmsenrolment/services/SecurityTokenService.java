package org.bcp.dlmsenrolment.services;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityTokenService {
    Optional<String> getCurrentBearerToken();

}
