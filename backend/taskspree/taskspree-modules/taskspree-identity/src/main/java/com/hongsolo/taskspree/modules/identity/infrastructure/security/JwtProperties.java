package com.hongsolo.taskspree.modules.identity.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Secret key for signing JWT tokens
     */
    private String secret;

    /**
     * Access token expiration time in milliseconds (default: 1 hour)
     */
    private long accessExpiration = 3600000;

    /**
     * Refresh token expiration time in milliseconds (default: 7 days)
     */
    private long refreshExpiration = 604800000;

    /**
     * Token issuer name
     */
    private String issuer = "taskspree";
}