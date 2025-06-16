package org.example.security2;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controllers {
    @GetMapping("/public")
    public String getPublic() {
        return "This is public!\n";
    }

    @GetMapping("/private")
    public String getPrivate(Authentication authentication) {
        return "This is private! " + authentication.getName() + "\n";
    }
}
