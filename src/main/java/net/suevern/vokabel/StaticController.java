package net.suevern.vokabel;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class StaticController {

    @RequestMapping(value = {"/", "/*", "/**"})
    public ResponseEntity<Resource> serve(HttpServletRequest request) throws IOException {
        String uri = request.getRequestURI();
        String path = uri.equals("/") || uri.isEmpty() ? "index.html" : uri.substring(1);
        ClassPathResource res = new ClassPathResource("static/" + path);
        if (!res.exists()) {
            res = new ClassPathResource("static/index.html");
        }
        String type = request.getServletContext().getMimeType(res.getFilename());
        MediaType mt = type != null ? MediaType.parseMediaType(type) : MediaType.TEXT_HTML;
        return ResponseEntity.ok().contentType(mt).body(res);
    }
}
