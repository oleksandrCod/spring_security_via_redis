package karpiuk.test.controller;

import java.util.List;

import karpiuk.test.dto.LoggedInUserResponse;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<LoggedInUserResponse>> getAllUsers(Pageable pageable) {
        log.info("Received get all users request");
        return ResponseEntity.status(HttpStatus.FOUND).body(userService.getAllUsers(pageable));
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        log.info("Received ping request!");
        return ResponseEntity.ok("Pong!");
    }
}
