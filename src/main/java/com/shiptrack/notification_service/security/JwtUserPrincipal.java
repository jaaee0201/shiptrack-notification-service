package com.shiptrack.notification_service.security;

public record JwtUserPrincipal(
        String username,
        String role
) {

}
