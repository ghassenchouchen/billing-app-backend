package com.example.billing.web.controller;

import com.example.billing.domain.repo.UserAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private final UserAccountRepository userAccountRepository;

    public AuthController(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping({"/checkuser", "/checkuser/"})
    public ResponseEntity<Map<String, Boolean>> checkUser(@RequestBody Map<String, String> body) {
        String userName = body.get("userName");
        String password = body.get("password");
        boolean exists = userAccountRepository.findByUserNameAndPassword(userName, password).isPresent();
        return ResponseEntity.ok(Map.of(
                "login", exists,
                "userName", userName != null,
                "password", password != null
        ));
    }
}
