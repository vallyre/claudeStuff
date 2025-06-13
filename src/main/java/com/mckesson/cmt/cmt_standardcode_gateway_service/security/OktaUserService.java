package com.mckesson.cmt.cmt_standardcode_gateway_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

/**
 * Utility service for retrieving user information from the Okta JWT token
 */
@Service
public class OktaUserService {
    
    /**
     * Get the current user's JWT token
     * @return The JWT token or null if not authenticated
     */
    public Jwt getCurrentUserToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            return (Jwt) authentication.getPrincipal();
        }
        
        return null;
    }
    
    /**
     * Get the user ID from the JWT token
     * @return The user ID or null if not available
     */
    public String getCurrentUserId() {
        Jwt jwt = getCurrentUserToken();
        
        if (jwt != null) {
            return jwt.getSubject();
        }
        
        return null;
    }
    
    /**
     * Get the user's email from the JWT token
     * @return The user's email or null if not available
     */
    public String getCurrentUserEmail() {
        Jwt jwt = getCurrentUserToken();
        
        if (jwt != null && jwt.hasClaim("email")) {
            return jwt.getClaimAsString("email");
        }
        
        return null;
    }
    
    /**
     * Get the user's name from the JWT token
     * @return The user's name or null if not available
     */
    public String getCurrentUserName() {
        Jwt jwt = getCurrentUserToken();
        
        if (jwt != null && jwt.hasClaim("name")) {
            return jwt.getClaimAsString("name");
        }
        
        return null;
    }
    
    /**
     * Get all claims from the JWT token as a map
     * @return Map of claims or empty map if not authenticated
     */
    public Map<String, Object> getAllClaims() {
        Jwt jwt = getCurrentUserToken();
        
        if (jwt != null) {
            return jwt.getClaims();
        }
        
        return Collections.emptyMap();
    }
    
    /**
     * Get the scopes/authorities from the JWT token
     * @return Set of scopes or empty set if not authenticated
     */
    public Set<String> getScopes() {
        Jwt jwt = getCurrentUserToken();
        
        if (jwt != null && jwt.hasClaim("scp")) {
            Object scopeClaim = jwt.getClaim("scp");
            
            if (scopeClaim instanceof String) {
                return Collections.singleton(scopeClaim.toString());
            } else if (scopeClaim instanceof String[]) {
                Set<String> scopes = new HashSet<>();
                for (String scope : (String[]) scopeClaim) {
                    scopes.add(scope);
                }
                return scopes;
            } else if (scopeClaim instanceof List) {
                Set<String> scopes = new HashSet<>();
                List<?> scopeList = (List<?>) scopeClaim;
                for (Object scope : scopeList) {
                    scopes.add(scope.toString());
                }
                return scopes;
            } else if (scopeClaim instanceof Iterable) {
                Set<String> scopes = new HashSet<>();
                for (Object scope : (Iterable<?>) scopeClaim) {
                    scopes.add(scope.toString());
                }
                return scopes;
            }
        }
        
        return Collections.emptySet();
    }
}