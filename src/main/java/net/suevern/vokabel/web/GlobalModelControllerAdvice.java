package net.suevern.vokabel.web;

import java.security.Principal;
import java.text.MessageFormat;
import java.time.Instant;

import org.springframework.boot.info.BuildProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelControllerAdvice {

    private final BuildProperties buildProperties;

    public GlobalModelControllerAdvice(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @ModelAttribute("username")
    public String username(Principal principal) {
        return principal == null ? null : principal.getName();
    }

    @ModelAttribute("usermail")
    public String usermail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User principal = token.getPrincipal();
            if (principal != null) {
                Object email = principal.getAttribute("email");
                return email != null ? email.toString() : null;
            }
        } else if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    @ModelAttribute("buildVersion")
    public String buildVersion() {
        return MessageFormat.format("{0}", buildProperties.getVersion());
    }

    @ModelAttribute("buildDate")
    public Instant buildDate() {
        return buildProperties.getTime();
    }

    @ModelAttribute("isPowerUser")
    public Boolean isPowerUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_POWERUSER".equals(auth.getAuthority()));
        }
        return false;
    }

    @ModelAttribute("isVisitor")
    public Boolean isVisitor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_VISITOR".equals(auth.getAuthority()));
        }
        return false;
    }
}
