package com.anurag.labslot.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;

@RestController
public class PortalController {
    @GetMapping("/student")
    public ResponseEntity<Void> studentPortal() {
        return redirect("/?portal=student");
    }

    @GetMapping("/admin")
    public ResponseEntity<Void> adminPortal() {
        return redirect("/?portal=admin");
    }

    private ResponseEntity<Void> redirect(String location) {
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, URI.create(location).toString()).build();
    }
}