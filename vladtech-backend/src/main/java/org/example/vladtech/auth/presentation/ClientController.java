package org.example.vladtech.auth.presentation;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    //demo can delete
    /*
    @PreAuthorize("hasAuthority('Client')")
    @GetMapping("/info")
    public String clientInfo() {
        return "Hello, Client User!";
    }

     */
}
