package net.suevern.vokabel.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping(value = {"/", "/index.html"})
    public String getHome() {
        return "index";
    }

    @GetMapping(value = "/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping(value = "/users")
    @PreAuthorize("hasRole('POWERUSER')")
    public String getUserManagement() {
        return "users";
    }

    @GetMapping(value = "/user.html")
    public String getUserinfo(Authentication authentication, Model model) {
        if (authentication == null) {
            return "user";
        }

        model.addAttribute("userAuthorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        // Handle OAuth2 authentication
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User principal = token.getPrincipal();
            Object name = principal.getAttribute("name");
            Object email = principal.getAttribute("email");
            model.addAttribute("userName", name != null ? name : "Unknown");
            model.addAttribute("userMail", email != null ? email : "Unknown");
            model.addAttribute("authType", "OAuth2 (Azure AD)");
        }
        // Handle form-based authentication
        else if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            model.addAttribute("userName", userDetails.getUsername());
            model.addAttribute("userMail", userDetails.getUsername());
            model.addAttribute("authType", "Form Login");
        }

        if (authentication.getDetails() instanceof WebAuthenticationDetails details) {
            model.addAttribute("userRemoteAddress", details.getRemoteAddress());
        }

        return "user";
    }
}
