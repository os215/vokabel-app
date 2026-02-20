package net.suevern.vokabel.web;

import java.security.Principal;
import java.text.MessageFormat;
import java.time.Instant;

import org.springframework.boot.info.BuildProperties;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    public String usermail(OAuth2AuthenticationToken token) {
        return token == null || token.getPrincipal() == null ? null : token.getPrincipal().getAttribute("email");
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
    public Boolean isPowerUser(OAuth2AuthenticationToken token) {
        if (token != null) {
            return token.getAuthorities().stream()
                    .anyMatch(auth -> "ROLE_POWERUSER".equals(auth.getAuthority()));
        } else {
            return false;
        }
    }

    @ModelAttribute("isVisitor")
    public Boolean isVisitor(OAuth2AuthenticationToken token) {
        if (token != null) {
            return token.getAuthorities().stream().anyMatch(auth -> "ROLE_VISITOR".equals(auth.getAuthority()));
        } else {
            return false;
        }
    }
}
