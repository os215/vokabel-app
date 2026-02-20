package net.suevern.vokabel.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        Set<GrantedAuthority> mappedAuthorities = new HashSet<>(oidcUser.getAuthorities());

        // Extract roles from the token
        // Azure AD can provide roles in different claims: "roles", "groups", or custom claims
        List<String> roles = extractRoles(oidcUser);

        // Map roles to Spring Security authorities
        boolean hasPowerUserRole = false;
        boolean hasVisitorRole = false;

        for (String role : roles) {
            // Check if user has POWERUSER role
            if (role.equalsIgnoreCase("POWERUSER") ||
                role.equalsIgnoreCase("VokabelPowerUser") ||
                role.equalsIgnoreCase("PowerUser")) {
                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_POWERUSER"));
                hasPowerUserRole = true;
            }
            // Check if user has VISITOR role
            if (role.equalsIgnoreCase("VISITOR") ||
                role.equalsIgnoreCase("VokabelVisitor") ||
                role.equalsIgnoreCase("Visitor")) {
                mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_VISITOR"));
                hasVisitorRole = true;
            }
        }

        // Default to VISITOR if no role is assigned
        if (!hasPowerUserRole && !hasVisitorRole) {
            mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_VISITOR"));
        }

        return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    private List<String> extractRoles(OidcUser oidcUser) {
        List<String> roles = new ArrayList<>();

        // Try to extract from "roles" claim
        Object rolesClaim = oidcUser.getClaim("roles");
        if (rolesClaim instanceof Collection) {
            ((Collection<?>) rolesClaim).forEach(role -> roles.add(role.toString()));
        } else if (rolesClaim instanceof String) {
            roles.add((String) rolesClaim);
        }

        // Try to extract from "groups" claim (Azure AD groups)
        Object groupsClaim = oidcUser.getClaim("groups");
        if (groupsClaim instanceof Collection) {
            ((Collection<?>) groupsClaim).forEach(group -> roles.add(group.toString()));
        } else if (groupsClaim instanceof String) {
            roles.add((String) groupsClaim);
        }

        return roles;
    }
}

