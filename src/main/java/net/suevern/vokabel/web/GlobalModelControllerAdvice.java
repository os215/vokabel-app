package net.suevern.vokabel.web;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.Date;

import org.springframework.boot.info.BuildProperties;
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

    @ModelAttribute("buildVersion")
    public String buildVersion() {
        return MessageFormat.format("{0} ({1,date,short} {1,time,short})", buildProperties.getVersion(),
                Date.from(buildProperties.getTime()));
    }
}
