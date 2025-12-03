package net.suevern.vokabel.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    private static final Log LOGGER = LogFactory.getLog(WebController.class);
    private final ApplicationContext applicationContext;

    public WebController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @GetMapping(value = "/")
    public String getHome() {
        return "index";
    }

    @GetMapping(value = "/user.html")
    public String getUserinfo(OAuth2AuthenticationToken token, Model model) {
        model.addAttribute("userAuthorities", token == null || token.getAuthorities() == null ? null
                : token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        if (token != null) {
            if (token.getPrincipal() != null) {
                model.addAttribute("userName", token.getPrincipal().getAttribute("name"));
                model.addAttribute("userMail", token.getPrincipal().getAttribute("email"));
            }
            if (token.getDetails() instanceof WebAuthenticationDetails details) {
                model.addAttribute("userRemoteAddress", details.getRemoteAddress());
            }
        }
        return "user";
    }
}
